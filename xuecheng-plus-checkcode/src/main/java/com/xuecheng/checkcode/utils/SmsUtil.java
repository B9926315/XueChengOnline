package com.xuecheng.checkcode.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
/**
 * @Author Planck
 * @Date 2023-05-02 - 10:20
 * 短信发送工具类
 */
@Slf4j
public class SmsUtil {
    /**
     * 发送短信
     * @param signName 签名
     * @param templateCode 模板
     * @param phoneNumbers 手机号
     * @param param 参数
     */

    public void sendMessage(String signName, String templateCode,String phoneNumbers,String param,String accessKeyId,String secret){
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, secret);
        IAcsClient client = new DefaultAcsClient(profile);
        SendSmsRequest request = new SendSmsRequest();
        request.setSysRegionId("cn-hangzhou");
        request.setPhoneNumbers(phoneNumbers);
        request.setSignName(signName);
        request.setTemplateCode(templateCode);
        request.setTemplateParam("{\"code\":\""+param+"\"}");
        try {
            SendSmsResponse response = client.getAcsResponse(request);
            log.info("短信发送成功");
            log.info(response.getMessage());
        }catch (ClientException e) {
            e.printStackTrace();
        }
    }
}