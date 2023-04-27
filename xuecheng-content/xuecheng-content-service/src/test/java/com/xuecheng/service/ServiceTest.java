package com.xuecheng.service;

import com.xuecheng.baseModel.PageParams;
import com.xuecheng.baseModel.PageResult;
import com.xuecheng.dto.*;
import com.xuecheng.mapper.CourseCategoryMapper;
import com.xuecheng.mapper.TeachplanMapper;
import com.xuecheng.pojo.CourseBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-24 - 14:03
 */
@SpringBootTest
public class ServiceTest {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private CourseCategoryService courseCategoryService;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Test
    void testCourse() {
        PageParams pageParams = new PageParams(1L, 3L);
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java");
        PageResult<CourseBase> list = courseBaseInfoService.queryCourseBaseList(pageParams, courseParamsDto);
        System.out.println(list.getItems());
    }

    @Test
    void testCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }

    @Test
    void testCategoryService() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }

    @Test
    void testTeachPlan() {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachplanDtos);
    }
}
