package com.xuecheng.search.service;

import com.xuecheng.search.po.CourseIndex;

/**
 * @Author planck
 
 * @Description 课程索引service
 * @Date 2023/4/24 22:40
 */
public interface IndexService {

    /**
     * @param indexName 索引名称
     * @param id 主键
     * @param object 索引对象
     * @return Boolean true表示成功,false失败
     * @Description 添加索引
     * @Author planck
     * @Date 2023/4/24 22:57
     */
    public Boolean addCourseIndex(String indexName,String id,Object object);


    /**
     * @Description 更新索引
     * @param indexName 索引名称
     * @param id 主键
     * @param object 索引对象
     * @return Boolean true表示成功,false失败
     * @Author planck
     * @Date 2023/4/25 7:49
    */
    public Boolean updateCourseIndex(String indexName,String id,Object object);

    /**
     * @Description 删除索引
     * @param indexName 索引名称
     * @param id  主键
     * @return java.lang.Boolean
     * @Author planck
     * @Date 2023/4/25 9:27
    */
    public Boolean deleteCourseIndex(String indexName,String id);

}
