package com.xuecheng.checkcode.service;

import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;

/**
 * @Author planck
 * @Description 验证码接口
 * @Date 2023/4/29 15:59
 */
public interface CheckCodeService {
    /**
     * @Description 生成验证码
     * @param checkCodeParamsDto 生成验证码参数
     * @return com.xuecheng.checkcode.model.CheckCodeResultDto 验证码结果
     * @Author planck
     * @Date 2023/4/29 18:21
    */
     CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

     /**
      * @Description 校验验证码
      * @param key
      * @param code
      * @return boolean
     */
    boolean verify(String key, String code);

    /**
     * 注册校验验证码
     * @param code  验证码
     * @param key Redis Key
     */
    boolean verifyRegisterCode(String key,String code);
    /**
     * @Description 验证码生成器
     * @Author planck
     * @Date 2023/4/29 16:34
    */
    interface CheckCodeGenerator{
        /**
         * 验证码生成
         * @return 验证码
         */
        String generate(int length);
    }

    /**
     * @Description key生成器
     * @Author planck
     * @Date 2023/4/29 16:34
     */
    interface KeyGenerator{
        /**
         * key生成
         * @return 验证码
         */
        String generate(String prefix);
    }

    /**
     * @Description 验证码存储
     * @Author planck
     */
    interface CheckCodeStore{
        /**
         * @Description 向缓存设置key
         * @param key key
         * @param value value
         * @param expire 过期时间,单位秒
        */
        void set(String key, String value, Integer expire);
        String get(String key);
        void remove(String key);
    }
}
