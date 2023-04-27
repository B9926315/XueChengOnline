package com.xuecheng.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author Planck
 * @Date 2023-04-16 - 17:06
 * 课程预览数据模型
 */
@Data
@ToString
public class CoursePreviewDto {
    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息暂时不加...

}
