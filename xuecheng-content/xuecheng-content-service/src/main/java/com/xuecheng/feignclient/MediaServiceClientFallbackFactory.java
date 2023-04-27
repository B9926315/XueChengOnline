package com.xuecheng.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Planck
 * @Date 2023-04-25 - 16:51
 * Feign的熔断降级。如果A服务调用B服务，而B服务无法正常工作，等待时间过后，就需要熔断降级，走Fallback路径，防止服务雪崩
 * FallbackFactory的 泛型需要指定要继承的接口名
 * 在接口处也要指定此 MediaServiceClientFallbackFactory
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            //这里重写了MediaServiceClient的方法
            @Override
            public String uploadFile(MultipartFile upload, String objectName) {
                //降级处理逻辑
                log.error("调用媒资服务上传文件发生降级熔断，ErrorMessage：{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
