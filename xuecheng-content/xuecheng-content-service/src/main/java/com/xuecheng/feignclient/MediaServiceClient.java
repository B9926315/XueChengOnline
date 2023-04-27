package com.xuecheng.feignclient;

import com.xuecheng.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Planck
 * @Date 2023-04-22 - 22:06
 * 课程发布后需要将课程封面的静态化页面上传到MinIO，所以用Feign调用媒资服务的接口
 */
//value是指定使用哪个接口,configuration是加载自定义配置文件
@FeignClient(value = "media-api",configuration = MultipartSupportConfig.class,
             fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {
    @RequestMapping(value = "/media/upload/coursefile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("filedata") MultipartFile upload,
                      @RequestParam(value = "objectName",required = false) String objectName);
}
