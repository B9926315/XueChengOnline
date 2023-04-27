package com.xuecheng.dto;

import com.xuecheng.pojo.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-25 - 22:21
 * 课程套餐分类DTO
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    List<CourseCategoryTreeDto> childrenTreeNodes;
}
