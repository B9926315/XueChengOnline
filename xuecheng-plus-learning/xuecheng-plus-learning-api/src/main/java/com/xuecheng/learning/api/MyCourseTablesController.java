package com.xuecheng.learning.api;

import com.xuecheng.baseModel.PageResult;
import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.learning.model.dto.*;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTableService;
import com.xuecheng.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @version 1.0
 * @Author Planck
 * @description 我的课程表接口
 * @date 2022/10/25 9:40
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {
    @Autowired
    private MyCourseTableService myCourseTableService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        //拿到用户ID
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (Objects.isNull(user)) {
            XueChengPlusException.cast("用户未登录");
        }
        String userId = user.getId();
        XcChooseCourseDto xcChooseCourseDto = myCourseTableService.addChooseCourse(userId, courseId);
        return xcChooseCourseDto;
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        //拿到用户ID
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (Objects.isNull(user)) {
            XueChengPlusException.cast("用户未登录");
        }
        String userId = user.getId();
        return myCourseTableService.getLearningStatus(userId, courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        //拿到用户ID
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (Objects.isNull(user)) {
            XueChengPlusException.cast("用户未登录");
        }
        String userId = user.getId();
        params.setUserId(userId);
        PageResult<XcCourseTables> xcCourseTablesPageResult = myCourseTableService.myCourseTables(params);
        return xcCourseTablesPageResult;
    }
}