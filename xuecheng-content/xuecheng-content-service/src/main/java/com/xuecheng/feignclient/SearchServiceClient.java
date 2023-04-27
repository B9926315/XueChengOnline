package com.xuecheng.feignclient;

import com.xuecheng.pojo.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
/**
 * @Author Planck
 * @Date 2023-04-26 - 13:28
 */
@FeignClient(value = "search",fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {

    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);
}
