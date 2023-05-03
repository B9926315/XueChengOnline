package com.xuecheng.service;

import com.xuecheng.baseModel.PageParams;
import com.xuecheng.baseModel.PageResult;
import com.xuecheng.dto.*;
import com.xuecheng.pojo.CourseBase;

/**
 * @Author Planck
 * @Date 2023-03-24 - 14:59
 * 课程信息管理接口
 */
public interface CourseBaseInfoService {
    /**
     * 根据条件分页查询课程信息
     * @param pageParams 此模型里面包含当前页码和每页记录数
     * @param queryCourseParamsDto 查询条件
     * @return 课程信息
     */
    PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程,要向两张表写数据，分别是Course_Market与course_base
     * @param companyId  机构ID，用于用户登录后展示该公司课程用
     * @param addCourseDto 接受到的课程模型
     * @return 新增完成后返回基本课程信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);

    /**
     * 根据课程ID查询课程信息
     * @param courseId 课程ID
     * @return 课程详细信息
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程信息
     * @param companyId 机构ID
     * @param editCourseDto 模型
     * @return 课程修改完成信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto editCourseDto);

    /**
     * 删除课程
     * @param companyId 机构ID
     * @param courseId 课程ID
     */
    void deleteCourse(Long companyId, Long courseId);
}
