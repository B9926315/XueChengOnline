package com.xuecheng.service;

import com.xuecheng.dto.*;

import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-29 - 21:56
 */
public interface TeachplanService {
    /**
     * 根据课程ID查询课程计划
     * @param courseId 课程ID
     * @return 课程计划
     */
    List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 保存课程计划
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 根据课程计划ID删除
     * @param teachPlanId 课程计划ID
     */
    void deleteTeachPlan(Long teachPlanId);

    /**
     * 对课程计划的章与节进行上移下移
     * @param moveType 字符串，moveup上移，movedown下移
     * @param teachPlanId 课程计划ID
     */
    void orderByTeachPlan(String moveType, Long teachPlanId);

    /**
     * 课程计划与媒资信息绑定
     * @param bindTeachPlanMediaDto 模型类
     */
    void associationMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto);
}
