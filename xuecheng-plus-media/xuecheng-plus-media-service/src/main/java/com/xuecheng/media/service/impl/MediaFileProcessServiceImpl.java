package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.*;
import com.xuecheng.media.model.po.*;
import com.xuecheng.media.service.MediaFileProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Planck
 * @Date 2023-04-11 - 20:22
 */
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Autowired
    private MediaProcessMapper mediaProcessMapper;
    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;
    /**
     * 查询要处理的任务
     * @param shardTotal 分片任务数
     * @param shardIndex 分片序号
     * @param count 每次查询最大数
     * @return 任务队列
     */
    @Override
    public List<MediaProcess> selectListByShardIndex(int shardTotal, int shardIndex, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal,shardIndex,count);
    }
    /**
     * 通过数据库方式设计一个分布式锁
     * @param id 任务ID
     * @return 更新记录数
     */
    @Override
    public boolean startTask(long id) {
        return mediaProcessMapper.startTask(id) > 0;
    }
    /**
     *  每次处理完保存任务结果
     * @param taskId 任务ID
     * @param status 任务状态
     * @param fileId 文件ID
     * @param url 处理完的URL
     * @param errorMsg 如果出错的错误信息
     */
    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查询出要处理的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess==null) {
            return;
        }
        /*
        任务执行失败，更新MediaProcess表状态
         */
        if ("3".equals(status)){
            mediaProcess.setErrormsg(errorMsg);
            mediaProcess.setStatus(status);
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
        /*
        任务执行成功，更新另一张表Media_file的URL；更新MediaProcess表状态，将MediaProcess表记录插入到
        MediaProcessHistory表；从MediaProcess表中删除当前任务
         */
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        //更新MediaProcess表,2代表成功
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);
        //将MediaProcess表记录插入到MediaProcessHistory表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //从MediaProcess表中删除当前任务
        mediaProcessMapper.deleteById(taskId);
    }
}
