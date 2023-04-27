package com.xuecheng.service;

import com.xuecheng.pojo.CourseTeacher;

import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-31 - 16:10
 */
public interface CourseTeacherService {
    /**
     * 查询教师信息
     * @param courseId 课程ID
     * @return 教师信息列表
     */
    List<CourseTeacher> getCourseTeacherList(Long courseId);

    /**
     * 添加或修改教师信息接口
     * @param courseTeacher 教师对象
     * @return 教室详细信息
     */
    CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 根据课程ID删除关联的教师
     * @param courseId 课程ID
     * @param teacherId 教师ID
     */
    void deleteCourseTeacher(Long courseId, Long teacherId);
}
