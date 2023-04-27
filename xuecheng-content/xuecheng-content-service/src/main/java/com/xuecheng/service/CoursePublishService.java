package com.xuecheng.service;

import com.xuecheng.dto.CoursePreviewDto;

import java.io.File;

/**
 * @Author Planck
 * @Date 2023-04-16 - 17:21
 * 课程发布与预览
 */
public interface CoursePublishService {
    /**
     * 为课程预览模板页面提供数据
     * @param courseId 课程ID
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);
    /**
     * 将课程提交审核
     * @param courseId 课程ID
     */
    void commitAudit(Long companyId,Long courseId);

    /**
     * 审核通过后，进行课程发布
     * 需要做三件事：Redis缓存、elasticsearch搜索索引、将课程静态页面存进MinIO
     * @param companyId 机构ID
     * @param courseId 课程ID
     */
    void coursePublish(Long companyId,Long courseId);
    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     */
    File generateCourseHtml(Long courseId);
    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     */
    void  uploadCourseHtml(Long courseId,File file);
}
