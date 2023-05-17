package com.xuecheng.learning.feignclient;

import com.xuecheng.pojo.CoursePublish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Planck
 * @version 1.0
 * @description 内容管理远程接口
 * @date 2022/10/25 9:13
 */
@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {

    @ResponseBody
    @GetMapping("/content/r/coursepublish/{courseId}")
    CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);
}
