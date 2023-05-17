package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.baseModel.RestResponse;
import com.xuecheng.dto.TeachplanDto;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTableService;
import com.xuecheng.pojo.CoursePublish;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author Planck
 * @Date 2023-05-16 - 21:55
 */
@Service
public class LearningServiceImpl implements LearningService {
    @Autowired
    private MyCourseTableService myCourseTableService;
    @Autowired
    private ContentServiceClient contentServiceClient;
    @Autowired
    private MediaServiceClient mediaServiceClient;
    /**
     * 获取视频
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param teachplanId 教学计划ID
     * @param mediaId 媒资ID
     */
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (Objects.isNull(coursepublish)){
            return RestResponse.validfail("课程不存在！");
        }
        //获取学习资格
        if (StringUtils.isNotBlank(userId)){
            XcCourseTablesDto learningStatus = myCourseTableService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if ("702002".equals(learnStatus)){
                return RestResponse.validfail("未选课或选课后未支付");
            }else if ("702003".equals(learnStatus)){
                return RestResponse.validfail("课程已过期，需要重新支付");
            }else {
                //有资格学习,返回播放地址
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMediaId;
            }
        }
        //课程是否支持试学
        String teachplan = coursepublish.getTeachplan();
        TeachplanDto teachplanDto = JSON.parseObject(teachplan, TeachplanDto.class);
        String isPreview = teachplanDto.getIsPreview();
        if ("1".equals(isPreview)){
            //支持试学
            RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMediaId;
        }
        //用户未登录
        //取出收费规则
        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)){
            //有资格学习，返回视频播放地址
            RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMediaId;
        }
        return RestResponse.validfail("该课程需要购买");
    }
}
