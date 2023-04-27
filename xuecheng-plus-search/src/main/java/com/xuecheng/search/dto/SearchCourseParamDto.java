package com.xuecheng.search.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @description 搜索课程参数dtl
 * @Author planck
 * @Date 2023/4/24 22:36
 
 */
 @Data
 @ToString
public class SearchCourseParamDto {

  //关键字
  private String keywords;

  //大分类
  private String mt;

  //小分类
  private String st;
  //难度等级
  private String grade;




}
