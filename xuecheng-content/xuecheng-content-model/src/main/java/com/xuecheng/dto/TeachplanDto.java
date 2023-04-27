package com.xuecheng.dto;

import com.xuecheng.pojo.Teachplan;
import com.xuecheng.pojo.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-29 - 19:52
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {
    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    //子结点
    List<TeachplanDto> teachPlanTreeNodes;
}
