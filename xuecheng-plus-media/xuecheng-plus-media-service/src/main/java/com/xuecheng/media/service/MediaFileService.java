package com.xuecheng.media.service;

import com.xuecheng.baseModel.*;
import com.xuecheng.media.model.dto.*;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * @Description 媒资文件管理业务类
 * @Author Planck
 * @Date 2023/4/2 8:55
 */
public interface MediaFileService {
    MediaFiles getMediaFileById(String MediaId);

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @Description 媒资文件查询方法
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件
     *
     * @param companyId           机构ID
     * @param uploadFileParamsDto 上传文件的基本信息
     * @param localFilePath       待上传文件的本地路径
     * @return 文件存储后详细信息
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName);

    /**
     * @param fileMd5 文件的md5
     * @Description 检查文件是否存在
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @Description 检查分块是否存在
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 /**
  * 上传分块
  * @param fileMD5 文件MD5值
  * @param chunk 分块序号
  * @param localChunkFilePath 分块文件本地路径
  */
    RestResponse uploadChunk(String fileMD5,int chunk,String localChunkFilePath);

    /**
     * 在MinIO中合并分块
     * @param companyId 机构ID
     * @param fileMd5 文件MD5值
     * @param chunkTotal 分块总数
     * @param uploadFileParamsDto 文件详细信息
     */
    RestResponse mergeChunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);
    MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);

    File downloadFromMinIO(String bucket, String objectName);
    boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName);
}
