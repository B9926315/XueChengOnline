package com.xuecheng;
import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Author Planck
 * @Date 2023-03-23 - 19:42
 */
@SpringBootApplication
//开启Swagger接口文档
@EnableSwagger2Doc
@EnableFeignClients(basePackages ="com.xuecheng.feignclient" )
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}
