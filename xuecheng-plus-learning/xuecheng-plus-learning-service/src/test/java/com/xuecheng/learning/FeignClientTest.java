package com.xuecheng.learning;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.pojo.CoursePublish;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author Planck
 * @Date 2023/5/9 20:14
 */
@SpringBootTest
public class FeignClientTest {

    @Autowired
    ContentServiceClient contentServiceClient;


    @Test
    public void testContentServiceClient() {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(1L);
        Assertions.assertNotNull(coursepublish);
    }
}