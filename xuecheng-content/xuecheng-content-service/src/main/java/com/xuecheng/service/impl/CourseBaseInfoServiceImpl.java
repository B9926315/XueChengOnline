package com.xuecheng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.baseModel.PageParams;
import com.xuecheng.baseModel.PageResult;
import com.xuecheng.dto.*;
import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.mapper.*;
import com.xuecheng.pojo.*;
import com.xuecheng.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-24 - 15:03
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    /**
     * 根据条件分页查询课程信息
     *
     * @param pageParams      此模型里面包含当前页码和每页记录数
     * @param courseParamsDto 查询条件
     * @return 课程信息
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根据名称模糊查询,在sql中拼接 course_base.name like '%值%'
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        //根据课程审核状态查询 course_base.audit_status = ?
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
        //按课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()), CourseBase::getStatus, courseParamsDto.getPublishStatus());
        queryWrapper.eq(CourseBase::getCompanyId,companyId);
        //创建page分页参数对象，参数：当前页码，每页记录数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //开始进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //数据列表
        List<CourseBase> items = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();

        //List<T> items, long counts, long page, long pageSize
        return new PageResult<CourseBase>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * 新增课程,要向两张表写数据，分别是Course_Market与course_base
     *
     * @param companyId    机构ID，用于用户登录后展示该公司课程用
     * @param addCourseDto 接受到的课程模型
     * @return 新增完成后返回基本课程信息
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        //合法性校验
        /*if (StringUtils.isBlank(addCourseDto.getName())) {
            // throw new RuntimeException("课程名称为空");
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
            // throw new RuntimeException("课程分类为空");
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
            // throw new RuntimeException("");
            XueChengPlusException.cast("课程分类为空");
        }


        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            // throw new RuntimeException("");
            XueChengPlusException.cast("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            // throw new RuntimeException("");
            XueChengPlusException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            // throw new RuntimeException("");
            XueChengPlusException.cast("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            // throw new RuntimeException("");
            XueChengPlusException.cast("收费规则为空");
        }*/
        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象,只要属性名称一致就可以复制
        BeanUtils.copyProperties(addCourseDto, courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0) {
            throw new RuntimeException("新增课程基本信息失败");
        }
//向课程营销表保存课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        //复制对象属性
        BeanUtils.copyProperties(addCourseDto, courseMarketNew);
        //课程ID
        Long courseId = courseBaseNew.getId();
        courseMarketNew.setId(courseId);
        //保存营销信息
        int saveCourseMarket = saveCourseMarket(courseMarketNew);
//查询课程基本信息及营销信息并返回
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }


    /**
     * 保存课程营销信息
     *
     * @param courseMarketNew 课程营销信息
     * @return 数字>0即代表成功
     */
    private int saveCourseMarket(CourseMarket courseMarketNew) {
        //收费规则
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isBlank(charge)) {
            throw new RuntimeException("收费规则没有选择");
        }
        //收费规则为收费
        if (charge.equals("201001")) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue() <= 0) {
                // throw new RuntimeException("");
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if (courseMarketObj == null) {
            //插入数据库
            return courseMarketMapper.insert(courseMarketNew);
        } else {
            //更改数据库
            return courseMarketMapper.updateById(courseMarketNew);
        }
    }

    /**
     * 根据课程id查询课程基本信息，包括基本信息和营销信息
     *
     * @param courseId 课程ID
     * @return 课程信息
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //查询分类名称
        //大分类
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        //小分类
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;
    }

    /**
     * 修改课程信息
     *
     * @param companyId     机构ID
     * @param editCourseDto 模型
     * @return 课程修改完成信息
     */
    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        //课程id
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }

        //校验本机构只能修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        //封装基本信息的数据
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        //更新课程基本信息
        int i = courseBaseMapper.updateById(courseBase);
        if (i <= 0) {
            XueChengPlusException.cast("课程修改失败，请稍后重试");
        }
        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        saveCourseMarket(courseMarket);
        //查询课程信息
        return this.getCourseBaseInfo(courseId);
    }
    /**
     * 删除课程
     * @param companyId 机构ID
     * @param courseId 课程ID
     */
    @Transactional
    @Override
    public void deleteCourse(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId()))
            XueChengPlusException.cast("只允许删除本机构的课程");
        // 删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(teacherLambdaQueryWrapper);
        // 删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);
        // 删除营销信息
        courseMarketMapper.deleteById(courseId);
        // 删除课程基本信息
        courseBaseMapper.deleteById(courseId);
        //删除视频信息
        LambdaQueryWrapper<TeachplanMedia> mediaQueryWrapper=new LambdaQueryWrapper<>();
        mediaQueryWrapper.eq(TeachplanMedia::getCourseId,courseId);
        teachplanMediaMapper.delete(mediaQueryWrapper);
    }

}
