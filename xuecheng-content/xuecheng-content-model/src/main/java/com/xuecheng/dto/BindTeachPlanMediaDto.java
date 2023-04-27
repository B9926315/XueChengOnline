package com.xuecheng.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @Author Planck
 * @Date 2023-04-14 - 13:32
 * 绑定媒资与课程计划模型类
 */
@Data
@ToString
@ApiModel(value = "BindTeachPlanMediaDto",description = "教学计划-媒资绑定提交数据")
public class BindTeachPlanMediaDto {
    @ApiModelProperty(value = "媒资文件ID",required = true)
    private String mediaId;
    @ApiModelProperty(value = "媒资文件名称",required = true)
    private String fileName;
    @ApiModelProperty(value = "课程计划ID",required = true)
    private Long teachplanId;
}
