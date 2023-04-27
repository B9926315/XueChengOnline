package com.xuecheng.media.service.jobhandler;

import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.utils.Mp4VideoUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * XxlJob开发示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、任务开发：在Spring Bean实例中，开发Job方法；
 * 2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 * 4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Slf4j
@Component
public class VideoJob {
    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    /**
     * 2、视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器序号,从零开始
        int shardTotal = XxlJobHelper.getShardTotal();//执行器总数
        log.info("shardIndex={},shardTotal={}", shardIndex, shardTotal);
        //获取CPU核心数
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        //查询待处理的任务
        List<MediaProcess> mediaProcessesList = mediaFileProcessService
                .selectListByShardIndex(shardTotal, shardIndex, availableProcessors);
        //实际任务数可能小于CPU核心数
        int processSize = mediaProcessesList.size();
        log.info("取出的视频处理任务数：" + processSize);
        if (processSize <= 0) {
            return;
        }
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(processSize);
        //线程计数器
        CountDownLatch countDownLatch = new CountDownLatch(processSize);
        mediaProcessesList.forEach(mediaProcess -> {
            //将任务加入线程池
            executorService.execute(() -> {
                try {
                    //开启任务
                    boolean startTaskFlag = mediaFileProcessService.startTask(mediaProcess.getId());//使用乐观锁
                    if (!startTaskFlag) {
                        //任务抢占失败，退出下次重试
                        log.info("任务抢占失败,任务ID={}", mediaProcess.getId());
                        return;
                    }
                    //源avi视频的路径
                    //先下载视频
                    File file = mediaFileService.downloadFromMinIO(mediaProcess.getBucket(), mediaProcess.getFilePath());
                    if (Objects.isNull(file)) {
                        log.error("下载视频出错，任务ID={},桶={}，ObjectName={}", mediaProcess.getId(), mediaProcess.getBucket(), mediaProcess.getFilePath());
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "下载视频到本地出错");
                        return;
                    }
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称，文件的ID就是名称
                    String mp4_name = mediaProcess.getFileId() + ".mp4";
                    File tempFile;
                    try {
                        tempFile = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.error("创建临时文件异常，{}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "创建临时文件异常");
                        return;
                    }
                    //转换后mp4文件的路径
                    String mp4_path = tempFile.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String s = videoUtil.generateMp4();
                    if (!"success".equals(s)) {
                        log.error("视频转码失败，result: {},bucket={},objectName={}", s, mediaProcess.getBucket(), mediaProcess.getFilePath());
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "视频转码失败," + s);
                    }
                    //成功，上传视频到MinIO
                    boolean b = mediaFileService.addMediaFilesToMinIO(tempFile.getAbsolutePath(), "video/mp4", mediaProcess.getBucket(), mediaProcess.getFileId());
                    if (!b) {
                        log.error("视频上传到MinIO失败，taskId={}，bucket={},objectName={}", mediaProcess.getId(), mediaProcess.getBucket(), mediaProcess.getFilePath());
                        mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", mediaProcess.getFileId(), null, "上传文件到MinIO失败");
                        return;
                    }
                    String fileUrl = getFilePathByMd5(mediaProcess.getFileId(), ".mp4");
                    //更新任务状态
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", mediaProcess.getFileId(), fileUrl, "");
                } finally {
                    //计数器减一
                    countDownLatch.countDown();
                }
            });
        });
        //指定最大限度等待时间
        countDownLatch.await(30,TimeUnit.MINUTES);
    }

    /**
     * 确认上传到MinIO文件的路径
     *
     * @param fileExt 文件扩展名
     * @return 路径
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
