package com.xuecheng.service;

import com.xuecheng.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-26 - 11:57
 */
public interface CourseCategoryService {
    /**
     * 课程分类树形结构查询
     *
     * @return
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
