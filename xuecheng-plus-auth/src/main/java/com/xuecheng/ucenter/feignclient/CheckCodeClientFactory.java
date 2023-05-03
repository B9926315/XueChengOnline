package com.xuecheng.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author Planck
 * @Date 2023-04-29 - 17:55
 */
@Slf4j
@Component
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("调用验证码服务熔断异常:{}", throwable.getMessage());
                return null;
            }

            @Override
            public Boolean verifyRegisterCode(String key, String code) {
                log.error("调用验证码服务熔断异常:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
