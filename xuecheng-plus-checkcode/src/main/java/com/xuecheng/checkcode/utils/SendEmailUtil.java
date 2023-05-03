package com.xuecheng.checkcode.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @Author Planck
 * @Date 2023-05-03 - 13:33
 */
@Slf4j
@Component
public class SendEmailUtil{
    @Autowired
    private JavaMailSender javaMailSender;
    public void sendMail(String fromEmail,String ReceiveEamil,String title,String context){
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(fromEmail+"(XueCheng-Online)");
        mailMessage.setTo(ReceiveEamil);
        mailMessage.setSubject(title);
        mailMessage.setText(context);
        javaMailSender.send(mailMessage);
    }
}