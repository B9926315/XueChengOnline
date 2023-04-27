package com.xuecheng.baseModel;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.*;

/**
 * @Author Planck
 * @Date 2023-03-23 - 19:20
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {
    @ApiModelProperty("当前页码")
    //当前页码
    private Long pageNo = 1L;
    @ApiModelProperty("每页记录数")
    //每页记录数默认值
    private Long pageSize =10L;
}
