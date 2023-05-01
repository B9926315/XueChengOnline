package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @Author Planck
 * @Date 2023-04-30 - 21:17
 * 微信扫码接入
 */
public interface WxAuthService {
    /**
     * 微信扫码登录逻辑
     * 1.申请令牌
     * 2.携带令牌查询用户信息
     * 3.保存用户信息到数据库
     * @param code 微信下发的授权码
     * @return User
     */
    XcUser wxAuth(String code);
}
