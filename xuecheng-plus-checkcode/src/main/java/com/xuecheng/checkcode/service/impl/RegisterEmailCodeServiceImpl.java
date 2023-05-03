package com.xuecheng.checkcode.service.impl;

import com.xuecheng.checkcode.service.RegisterEmailCodeService;
import com.xuecheng.checkcode.utils.SendEmailUtil;
import com.xuecheng.checkcode.utils.ValidateCodeUtil;
import com.xuecheng.exception.XueChengPlusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author Planck
 * @Date 2023-05-03 - 15:59
 */
@Service
public class RegisterEmailCodeServiceImpl implements RegisterEmailCodeService {
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Autowired
    private SendEmailUtil sendEmailUtil;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 向指定的邮箱发送验证码，包括注册与找回密码
     * @param toEmail 目标邮箱
     * @param type 用于区别是注册还是找回密码,找回密码type=findpassword，注册type=null
     */
    @Override
    public void CodeAndCache(String toEmail,String type) {
        toEmail=toEmail.trim();
        //校验邮箱合法性
        boolean matches = toEmail.matches("^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$");
        if (!matches){
            XueChengPlusException.cast("邮箱格式不正确！");
        }
        //生成验证码
        Integer code = ValidateCodeUtil.generateValidateCode(6);
        String emailTitle="XueCheng-Online";
        String emailContext="【Planck】 您的验证码是"+code+"，该验证码5分钟内有效，请勿泄露于他人";
        //发送验证码
        sendEmailUtil.sendMail(fromEmail,toEmail,emailTitle,emailContext);
        String redisKey;
        //将验证码保存在Redis中
        if (Objects.nonNull(type) && "findpassword".equals(type)){
            redisKey=type+":code:"+toEmail;
        }else {
            redisKey="register:code:"+toEmail;
        }
        stringRedisTemplate.opsForValue().set(redisKey,code.toString(),5, TimeUnit.MINUTES);
    }
}
