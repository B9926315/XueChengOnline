package com.xuecheng.learning.service;

import com.xuecheng.baseModel.RestResponse;

/**
 * 在线学习相关接口
 *
 * @Author Planck
 * @Date 2023-05-16 - 21:51
 */
public interface LearningService {
    /**
     * 获取视频
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param teachplanId 教学计划ID
     * @param mediaId 媒资ID
     */
    RestResponse<String> getVideo(String userId,Long courseId,Long teachplanId,String mediaId);
}
