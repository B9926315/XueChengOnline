package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @Author Planck
 * @Date 2023-04-11 - 20:16
 * 媒体任务处理
 */
public interface MediaFileProcessService {
    /**
     * 查询要处理的任务
     * @param shardTotal 分片任务数
     * @param shardIndex 分片序号
     * @param count 每次查询最大数
     * @return 任务队列
     */
    List<MediaProcess> selectListByShardIndex(int shardTotal, int shardIndex,int count);
    /**
     * 通过数据库方式设计一个分布式锁
     * @param id 任务ID
     * @return 更新记录数
     */
    boolean startTask(long id);

    /**
     *  每次处理完保存任务结果
     * @param taskId 任务ID
     * @param status 任务状态
     * @param fileId 文件ID
     * @param url 处理完的URL
     * @param errorMsg 如果出错的错误信息
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
