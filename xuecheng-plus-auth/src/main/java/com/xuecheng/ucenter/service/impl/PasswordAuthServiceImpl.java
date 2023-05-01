package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author Planck
 * @Date 2023-04-29 - 14:45
 * 账号名密码认证方式
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CheckCodeClient checkCodeClient;
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //取出账号
        String username = authParamsDto.getUsername();
        /*
        校验验证码
         */
        //前端输入的验证码
        String checkcode = authParamsDto.getCheckcode();
        //获取验证码对应的key
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (StringUtils.isBlank(checkcodekey) || StringUtils.isBlank(checkcode)){
            throw new RuntimeException("验证码为空");
        }
        //调用Feign的接口进行比对
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (Objects.isNull(verify)|| !verify){
            throw new RuntimeException("验证码输入错误");
        }
        //根据username查询数据库
        LambdaQueryWrapper<XcUser> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getUsername,username);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (Objects.isNull(xcUser)) {
            throw new RuntimeException("账号不存在");
        }
        //数据库中经过加密的密码
        String passwordDb = xcUser.getPassword();
        //用户输入的密码
        String passwordForm = authParamsDto.getPassword();
        //校验密码正确性,未加密密码在前，加密后密码在后
        boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }
}
