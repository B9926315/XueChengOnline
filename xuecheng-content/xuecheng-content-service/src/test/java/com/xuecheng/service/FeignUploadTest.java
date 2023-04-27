package com.xuecheng.service;

import com.xuecheng.config.MultipartSupportConfig;
import com.xuecheng.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Author Planck
 * @Date 2023-04-25 - 15:34
 */
@SpringBootTest
public class FeignUploadTest {
    @Autowired
    MediaServiceClient mediaServiceClient;

    /**
     * 测试用Feign调用media-api接口上传课程静态页面
     */
    @Test
    void testUpload() {
        File file = new File("D:\\1临时文件\\01.png");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        mediaServiceClient.uploadFile(multipartFile,"/course/02.png");
    }
}
