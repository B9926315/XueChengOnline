package com.xuecheng.learning.service;

import com.xuecheng.baseModel.PageResult;
import com.xuecheng.learning.model.dto.*;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @Author Planck
 * @Date 2023-05-09 - 17:08
 */
public interface MyCourseTableService {
    /**
     * 添加选课
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    XcChooseCourseDto addChooseCourse(String userId,Long courseId);

    /**
     * 判断学习资格
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 查询我的课程表
     */
    PageResult<XcCourseTables> myCourseTables(MyCourseTableParams params);
}
