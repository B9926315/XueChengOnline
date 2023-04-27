package com.xuecheng.api;

import com.xuecheng.baseModel.PageParams;
import com.xuecheng.baseModel.PageResult;
import com.xuecheng.dto.*;
import com.xuecheng.exception.ValidationGroups;
import com.xuecheng.pojo.CourseBase;
import com.xuecheng.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Planck
 * @Date 2023-03-23 - 19:34
 */
@Api(value = "课程信息编辑接口", tags = "课程信息编辑")
@RestController
public class CourseBaseInfoController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @ApiOperation("课程信息查询")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        return courseBaseInfoService.queryCourseBaseList(pageParams,queryCourseParams);
    }

    /**
     * 注解Validated: 激活JSR303校验
     */
    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto){
        Long companyId=1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }
    @ApiOperation("根据课程ID查询课程信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
        //获取当前用户身份
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }
    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,editCourseDto);
    }
    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        Long companyId = 1232141425L;
        courseBaseInfoService.deleteCourse(companyId,courseId);
    }
}
