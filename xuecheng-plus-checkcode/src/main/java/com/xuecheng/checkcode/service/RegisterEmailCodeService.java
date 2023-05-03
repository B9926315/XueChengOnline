package com.xuecheng.checkcode.service;

/**
 * @Author Planck
 * @Date 2023-05-03 - 15:58
 */
public interface RegisterEmailCodeService {
    void CodeAndCache(String toEmail,String type);
}
