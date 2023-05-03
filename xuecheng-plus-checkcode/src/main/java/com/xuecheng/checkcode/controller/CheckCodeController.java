package com.xuecheng.checkcode.controller;

import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.service.CheckCodeService;
import com.xuecheng.checkcode.service.RegisterEmailCodeService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author planck
 * @Description 验证码服务接口
 * @Date 2023/4/29 18:39
 */
@Api(value = "验证码服务接口")
@RestController
@RefreshScope
public class CheckCodeController {
    @Autowired
    private RegisterEmailCodeService registerEmailCodeService;
    @Resource(name = "PicCheckCodeService")
    private CheckCodeService picCheckCodeService;

    @ApiOperation(value = "生成验证信息", notes = "生成验证信息")
    @PostMapping(value = "/pic")
    public CheckCodeResultDto generatePicCheckCode(CheckCodeParamsDto checkCodeParamsDto) {
        return picCheckCodeService.generate(checkCodeParamsDto);
    }

    @ApiOperation(value = "校验", notes = "校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "业务名称", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "key", value = "验证key", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String", paramType = "query")
    })
    //登录验证码
    @PostMapping(value = "/verify")
    public Boolean verify(String key, String code) {
        return picCheckCodeService.verify(key, code);
    }

    @PostMapping(value = "/verifyregistercode")
    public Boolean verifyRegisterCode(String key, String code){
        return picCheckCodeService.verifyRegisterCode(key,code);
    }

    /**
     * 用户注册向邮箱发送验证码
     * @param param1 邮箱
     */
    @PostMapping("/phone")
    public void sendCheckCodeByAliyun(@RequestParam("param1") String param1,
                                      @RequestParam(value = "type",required = false) String type) {
        registerEmailCodeService.CodeAndCache(param1,type);
    }
}
