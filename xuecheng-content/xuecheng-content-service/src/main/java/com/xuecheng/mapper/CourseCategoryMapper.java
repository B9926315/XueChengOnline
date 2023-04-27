package com.xuecheng.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.dto.CourseCategoryTreeDto;
import com.xuecheng.pojo.CourseCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    /**
     * 使用递归查询课程套餐分类
     * @param id 节点ID
     * @return 分类结果
     */
    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
