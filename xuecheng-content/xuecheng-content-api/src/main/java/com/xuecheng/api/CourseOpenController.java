package com.xuecheng.api;

import com.xuecheng.dto.CoursePreviewDto;
import com.xuecheng.service.CourseBaseInfoService;
import com.xuecheng.service.CoursePublishService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Planck
 * @Date 2023-04-18 - 22:41
 * 用户不用登录就可以访问的接口
 */
@Api(value = "课程公开查询接口",tags = "课程公开查询接口")
@RestController
@RequestMapping("/open")
public class CourseOpenController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable Long courseId){
        //获取课程预览信息，用于播放视频时右侧目录栏
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
