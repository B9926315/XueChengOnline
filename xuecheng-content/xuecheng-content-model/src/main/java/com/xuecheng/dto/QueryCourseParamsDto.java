package com.xuecheng.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @Author Planck
 * @Date 2023-03-23 - 19:24
 * 课程查询参数DTO
 */
@Data
@ToString
public class QueryCourseParamsDto {
    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;
}
