package com.xuecheng.api;

import com.xuecheng.baseModel.PageParams;
import com.xuecheng.baseModel.PageResult;
import com.xuecheng.dto.*;
import com.xuecheng.exception.ValidationGroups;
import com.xuecheng.pojo.CourseBase;
import com.xuecheng.service.CourseBaseInfoService;
import com.xuecheng.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")//指定权限标识符
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        //得到当前登录的用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (Objects.isNull(user)){
            throw new RuntimeException("登录用户信息为空!");
        }
        String companyIdString = user.getCompanyId();
        Long companyId=null;
        if (StringUtils.isNotBlank(companyIdString)){
            companyId=Long.parseLong(companyIdString);
        }
        return courseBaseInfoService.queryCourseBaseList(companyId,pageParams,queryCourseParams);
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
        SecurityUtil.XcUser user = SecurityUtil.getUser();
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