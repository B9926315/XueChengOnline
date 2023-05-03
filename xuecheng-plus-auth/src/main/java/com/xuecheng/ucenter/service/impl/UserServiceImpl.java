package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Planck
 * @Date 2023-04-28 - 15:17
 */
@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {
    //注入Spring容器
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private XcMenuMapper xcMenuMapper;

    /**
     * @param s 自定义JSON用户信息
     * @return org.springframework.security.core.userdetails
     * 查询不到返回空即可，密码比对由SpringSecurity完成
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合要求");
        }
        //取出认证类型
        String authType = authParamsDto.getAuthType();
        //根据认证类型从Spring容器中获取Bean
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //调用方法，完成认证
        XcUserExt user = authService.execute(authParamsDto);
        //封装用户信息，根据UserDetails生成令牌
        return getUserPrincipal(user);
    }

    /*
    查询用户信息
     */
    protected UserDetails getUserPrincipal(XcUserExt xcUser) {
        String password = xcUser.getPassword();
        String[] authorities={"test"};
        //根据用户ID查询用户权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        if (xcMenus.size()>0){
            List<String> permission=new ArrayList<>();
            xcMenus.forEach(m->{
                permission.add(m.getCode());
            });
            //这里的new String[0]目的是申明类型，长度置零即可
            authorities=permission.toArray(new String[0]);
        }
        /*
        扩展用户身份，将用户的信息转成JSON后存储在withUsername中
         */
        //密码属敏感信息，置空再转JSON
        xcUser.setPassword(null);
        //用户信息转JSON字符串
        String xcUserJsonString = JSON.toJSONString(xcUser);
        return User.withUsername(xcUserJsonString)
                .password(password)//这里传入的密码不得为空
                .authorities(authorities)
                .build();
    }
}