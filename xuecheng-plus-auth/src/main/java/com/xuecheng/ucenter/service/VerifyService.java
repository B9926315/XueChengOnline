package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.RegisterDto;

/**
 * @Author Planck
 * @Date 2023-05-02 - 10:05
 * 用户注册与找回密码
 */
public interface VerifyService {
    void register(RegisterDto registerDto);
    void modifyPassword(RegisterDto registerDto);
}
