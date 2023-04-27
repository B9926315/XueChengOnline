package com.xuecheng.feignclient;

import com.xuecheng.pojo.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author Planck
 * @Date 2023-04-26 - 13:29
 */
@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {

        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                throwable.printStackTrace();
                log.error("调用搜索发生熔断走降级方法,索引信息：{}。\r\n熔断异常:{}",
                        courseIndex, throwable.getMessage(),throwable);
                return false;
            }
        };
    }
}
