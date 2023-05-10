package com.xuecheng.learning.feignclient;

import com.xuecheng.baseModel.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/10/27 9:04
 * @version 1.0
 */
 @FeignClient(value = "media-api",fallbackFactory = MediaServiceClientFallbackFactory.class)
 @RequestMapping("/media")
 public interface MediaServiceClient {

  @GetMapping("/open/preview/{mediaId}")
  RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId);
 }
