package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTableService;
import com.xuecheng.pojo.CoursePublish;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @Author Planck
 * @Date 2023-05-09 - 17:09
 */
@Slf4j
@Service
public class MyCourseTableServiceImpl implements MyCourseTableService {
    @Autowired
    private XcChooseCourseMapper chooseCourseMapper;
    @Autowired
    private XcCourseTablesMapper courseTablesMapper;
    @Autowired
    private ContentServiceClient contentServiceClient;
    /**
     * 添加选课
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        XcChooseCourse xcChooseCourse;
        /*
        调用内容管理模块查询课程收费规则
         */
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (Objects.isNull(coursepublish)){
            XueChengPlusException.cast("课程不存在");
        }
        //获取收费情况
        String charge = coursepublish.getCharge();
        /*
        课程免费，向选课记录表、我的课程表写入数据。
        课程收费，只向选课记录表写入数据。
         */
        if ("201000".equals(charge)){
            //免费课程
            xcChooseCourse = addFreeCourse(userId, coursepublish);
            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse);
        }else {
            //收费课程
            xcChooseCourse = addChargeCourse(userId, coursepublish);
        }
        /*
        判断学生的学习资格
         */
        XcCourseTablesDto learningStatus = getLearningStatus(userId, courseId);
        //构建返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(learningStatus.getLearnStatus());

        return xcChooseCourseDto;
    }
    /**
     * 判断学习资格
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if(xcCourseTables==null){
            XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
            //没有选课或选课后没有支付
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        //是否过期,true过期，false未过期
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if(!isExpires){
            //正常学习
            xcCourseTablesDto.setLearnStatus("702001");
            return xcCourseTablesDto;

        }else{
            //已过期
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
    }

    /**
     * 向选课记录表添加免费课程
     * @param userId 用户ID
     * @param coursePublish 课程发布信息
     */
    private XcChooseCourse addFreeCourse(String userId,CoursePublish coursePublish){
        //获取课程ID
        Long courseId = coursePublish.getId();
        //判断，如果存在免费的选课记录且选课状态为成功，即可直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(XcChooseCourse::getUserId,userId);
        queryWrapper.eq(XcChooseCourse::getCourseId,courseId);
        queryWrapper.eq(XcChooseCourse::getOrderType,"700001");//免费课程
        queryWrapper.eq(XcChooseCourse::getStatus,"701001");//选课成功
        List<XcChooseCourse> xcChooseCourseList = chooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourseList.size()>0){
            //该课程已经被选过了，无需插入数据库
            return xcChooseCourseList.get(0);
        }
        //向选课记录表写入数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setStatus("701001");//选课成功
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());//有效期开始时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));//有效期结束时间
        int insert = chooseCourseMapper.insert(xcChooseCourse);
        if (insert<=0){
            XueChengPlusException.cast("插入选课记录失败");
        }
        return xcChooseCourse;
    }

    /**
     * 向我的课程表添加信息
     * @param xcChooseCourse 选课信息
     */
    private XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse){
        //选课成功才能向选课表添加数据
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)){
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (Objects.nonNull(xcCourseTables)){
            return xcCourseTables;
        }
        xcCourseTables=new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse,xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());//选课类型
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int insert = courseTablesMapper.insert(xcCourseTables);
        if (insert<=0){
            XueChengPlusException.cast("向课程表写入数据失败");
        }
        return xcCourseTables;
    }

    /**
     * 课程收费，向选课记录表写数据
     * @param userId 用户ID
     * @param coursePublish 课程发布信息
     */
    private XcChooseCourse addChargeCourse(String userId,CoursePublish coursePublish){
        //获取课程ID
        Long courseId = coursePublish.getId();
        //判断，如果存在收费的选课记录且选课状态为待支付，即可直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(XcChooseCourse::getUserId,userId);
        queryWrapper.eq(XcChooseCourse::getCourseId,courseId);
        queryWrapper.eq(XcChooseCourse::getOrderType,"700002");//收费课程
        queryWrapper.eq(XcChooseCourse::getStatus,"701002");//待支付
        List<XcChooseCourse> xcChooseCourseList = chooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourseList.size()>0){
            return xcChooseCourseList.get(0);
        }
        //向选课记录表写入数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701002");//待支付
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());//有效期开始时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));//有效期结束时间
        int insert = chooseCourseMapper.insert(xcChooseCourse);
        if (insert<=0){
            XueChengPlusException.cast("插入选课记录失败");
        }
        return xcChooseCourse;
    }
    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @return com.xuecheng.learning.model.po.XcCourseTables
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;
    }
}
