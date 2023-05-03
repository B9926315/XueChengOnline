package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.VerifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * @Author Planck
 * @Date 2023-05-02 - 10:09
 */
@Slf4j
@Service
public class VerifyServiceImpl implements VerifyService {
    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private XcUserRoleMapper xcUserRoleMapper;
    @Autowired
    private CheckCodeClient checkCodeClient;

    @Transactional
    @Override
    public void register(RegisterDto registerDto) {
        String email = registerDto.getEmail().trim();
        String cellphone = registerDto.getCellphone();
        String password = registerDto.getPassword();
        String confirmpwd = registerDto.getConfirmpwd();
        boolean matches = cellphone.matches("^1[3-9]\\d{9}$");
        if (!matches) {
            throw new RuntimeException("手机号填写错误");
        }
        if (!Objects.equals(password,confirmpwd)){
            throw new RuntimeException("密码不一致");
        }
        String redisKey="register:code:"+email.trim();
        Boolean aBoolean = checkCodeClient.verifyRegisterCode(redisKey, registerDto.getCheckcode());
        if (Objects.isNull(aBoolean)){
            throw new RuntimeException("服务出现异常，请稍后重试！");
        }else if (!aBoolean){
            throw new RuntimeException("验证码错误");
        }
        //查询该用户是否已经注册过
        LambdaQueryWrapper<XcUser> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(email),XcUser::getEmail,email);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (Objects.nonNull(xcUser)){
            throw new RuntimeException("该用户已存在，一个邮箱只能注册一个账号！");
        }
        String userId = UUID.randomUUID().toString();
        xcUser=new XcUser();
        //密码进行加密
        xcUser.setPassword(new BCryptPasswordEncoder().encode(password));
        xcUser.setId(userId);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");
        xcUser.setNickname(registerDto.getNickname().trim());
        xcUser.setName(registerDto.getNickname().trim());
        xcUser.setCreateTime(LocalDateTime.now());
        xcUser.setCellphone(cellphone);
        xcUser.setEmail(email);
        xcUser.setUsername(registerDto.getUsername());
        int insert = xcUserMapper.insert(xcUser);

        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");
        xcUserRole.setId(userId);
        xcUserRole.setCreateTime(LocalDateTime.now());
        int insert1 = xcUserRoleMapper.insert(xcUserRole);
        if (insert<=0 || insert1<=0){
            log.error("新增用户信息失败！用户信息：{}",xcUser);
            throw new RuntimeException("新增用户信息失败！");
        }
    }

    /**
     * 修改密码
     */
    @Override
    public void modifyPassword(RegisterDto registerDto) {
        String email = registerDto.getEmail().trim();
        String password = registerDto.getPassword();
        String confirmpwd = registerDto.getConfirmpwd();
        if (!Objects.equals(password,confirmpwd) || Objects.isNull(password) || Objects.isNull(confirmpwd)){
            throw new RuntimeException("密码不一致");
        }
        String redisKey="findpassword:code:"+email.trim();
        Boolean aBoolean = checkCodeClient.verifyRegisterCode(redisKey, registerDto.getCheckcode().trim());
        if (Objects.isNull(aBoolean)){
            throw new RuntimeException("服务出现异常，请稍后重试！");
        }else if (!aBoolean){
            throw new RuntimeException("验证码错误");
        }
        //修改密码
        LambdaQueryWrapper<XcUser> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(email),XcUser::getEmail,email);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (Objects.isNull(xcUser)){
            throw new RuntimeException("用户不存在！");
        }
        xcUser.setPassword(new BCryptPasswordEncoder().encode(password));
        xcUserMapper.updateById(xcUser);
    }
}