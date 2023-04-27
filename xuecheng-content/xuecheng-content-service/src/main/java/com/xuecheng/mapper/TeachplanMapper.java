package com.xuecheng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.dto.TeachplanDto;
import com.xuecheng.pojo.Teachplan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    List<TeachplanDto> selectTreeNodes(Long courseId);
}
