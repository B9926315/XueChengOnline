package com.xuecheng.api;

import com.xuecheng.dto.CourseCategoryTreeDto;
import com.xuecheng.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-25 - 22:24
 */
@Slf4j
@RestController
public class CourseCategoryController {
    @Autowired
    private CourseCategoryService courseCategoryService;
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        return courseCategoryTreeDtos;
    }
}
