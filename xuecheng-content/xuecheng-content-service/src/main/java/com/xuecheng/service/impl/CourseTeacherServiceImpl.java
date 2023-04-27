package com.xuecheng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.exception.CommonError;
import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.mapper.CourseTeacherMapper;
import com.xuecheng.pojo.CourseTeacher;
import com.xuecheng.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-31 - 16:11
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    /**
     * 查询教师信息
     * @param courseId 课程ID
     * @return 教师信息列表
     */
    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        if (courseId==null){
            //请求参数为空
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }
        LambdaQueryWrapper<CourseTeacher> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }
    /**
     * 添加或修改教师信息接口
     * @param courseTeacher 教师对象
     * @return 教室详细信息
     */
    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getId();
        if (id==null){
            //ID为null，新增教师
            courseTeacher.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(courseTeacher);
            if (insert<=0){
                XueChengPlusException.cast("添加教师失败，请重试！");
            }
            return getCourseTeacher(courseTeacher);
        }else {
            //ID不为空，修改教师信息
            int flag = courseTeacherMapper.updateById(courseTeacher);
            if (flag <= 0)
                XueChengPlusException.cast("修改失败");
            return getCourseTeacher(courseTeacher);
        }
    }

    private CourseTeacher getCourseTeacher(CourseTeacher courseTeacher) {
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    /**
     * 根据课程ID删除关联的教师
     * @param courseId 课程ID
     * @param teacherId 教师ID
     */
    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        int flag = courseTeacherMapper.delete(queryWrapper);
        if (flag < 0) {
            XueChengPlusException.cast("删除教师失败");
        }
    }
}
