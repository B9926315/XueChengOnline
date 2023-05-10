package com.xuecheng.api;

import com.xuecheng.dto.CoursePreviewDto;
import com.xuecheng.pojo.CoursePublish;
import com.xuecheng.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author Planck
 * @Date 2023-04-15 - 17:44
 * 利用FreeMaker模板引擎返回课程接口
 */
@Controller
public class CoursePublishController {
    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.addObject("model",coursePreviewInfo);
        //根据视图名和配置文件自动加.ftl,然后找到模板
        modelAndView.setViewName("courseInfo");
        return modelAndView;
    }
    /**
     * 将课程提交审核
     * @param courseId 课程ID
     */
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable Long courseId){
        coursePublishService.commitAudit(1232141425L,courseId);
    }

    /**
     *  审核通过后，进行课程发布
     *  需要做三件事：Redis缓存、elasticsearch搜索索引、将课程静态页面存进MinIO
     * @param courseId 课程ID
     */
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable Long courseId){
        Long companyId=1232141425L;
        coursePublishService.coursePublish(companyId,courseId);
    }
    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId){
        return coursePublishService.getCoursePublish(courseId);
    }
}
