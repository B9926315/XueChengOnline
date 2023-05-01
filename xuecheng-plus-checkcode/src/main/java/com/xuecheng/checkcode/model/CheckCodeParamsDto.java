package com.xuecheng.checkcode.model;

import lombok.Data;

/**
 * @Author planck
 * @version 1.0
 * @Description 验证码生成参数类
 * @Date 2023/4/29 15:48
 */
@Data
public class CheckCodeParamsDto {

    /**
     * 验证码类型:pic、sms、email等
     */
    private String checkCodeType;

    /**
     * 业务携带参数
     */
    private String param1;
    private String param2;
    private String param3;
}
