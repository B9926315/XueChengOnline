package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.service.VerifyService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author planck
 * @Description 用户注册、找回密码等操作
 * @Date 2023/4/27 17:25
 */
@Slf4j
@RestController
public class UserController {
    @Autowired
    private VerifyService verifyService;

    @ApiOperation(value = "注册",tags = "注册")
    @PostMapping("/register")
    public void register(@RequestBody RegisterDto registerDto){
        verifyService.register(registerDto);
    }
    @ApiOperation(value = "修改密码",tags = "修改密码")
    @PostMapping("/findpassword")
    public void findPassword(@RequestBody RegisterDto registerDto){
        verifyService.modifyPassword(registerDto);
    }
}
