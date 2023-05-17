package com.xuecheng.learning.api;

import com.xuecheng.baseModel.RestResponse;
import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @Author Planck
 * @description 我的学习接口
 * @date 2023/05/16 8:59
 */
@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
@Slf4j
@RestController
public class MyLearningController {

    @Autowired
    private LearningService learningService;

    @ApiOperation("获取视频")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getvideo(@PathVariable("courseId") Long courseId,
                                         @PathVariable("teachplanId") Long teachplanId,
                                         @PathVariable("mediaId") String mediaId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (Objects.isNull(user)){
            XueChengPlusException.cast("用户未登录！");
        }
        String userId = user.getId();
        RestResponse<String> videoUrl = learningService.getVideo(userId, courseId, teachplanId, mediaId);
        return videoUrl;
    }
}