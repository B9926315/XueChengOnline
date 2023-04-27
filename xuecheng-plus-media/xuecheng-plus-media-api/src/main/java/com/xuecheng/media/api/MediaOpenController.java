package com.xuecheng.media.api;

import com.xuecheng.baseModel.RestResponse;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @Author Planck
 * @Date 2023-04-19 - 17:07
 * 媒资公开接口，不登录亦能访问
 */
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {
    @Autowired
    private MediaFileService mediaFileService;

    /**
     * 在课程视频播放页面，为播放的视频提供URL路径
     * @param mediaId 媒资文件ID
     * @return 媒资文件URL
     */
    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId){
        MediaFiles mediaFiles = mediaFileService.getMediaFileById(mediaId);
        if (Objects.isNull(mediaFiles) || StringUtils.isBlank(mediaFiles.getUrl())){
            return RestResponse.validfail("该视频正在处理中，请稍后重试");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }
}
