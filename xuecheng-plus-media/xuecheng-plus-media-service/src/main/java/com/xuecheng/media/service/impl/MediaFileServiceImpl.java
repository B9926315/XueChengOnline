package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.baseModel.*;
import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.*;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Planck
 * @Date 2023/4/2 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MediaFileService mediaFileService;
    @Autowired
    private MediaProcessMapper mediaProcessMapper;
    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucketFiles;
    //视频文件桶
    @Value("${minio.bucket.videofiles}")
    private String bucketVideo;

    @Override
    public MediaFiles getMediaFileById(String MediaId) {
        return mediaFilesMapper.selectById(MediaId);
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(queryMediaParamsDto.getFilename()),MediaFiles::getFilename,queryMediaParamsDto.getFilename());
        queryWrapper.eq(StringUtils.isNotBlank(queryMediaParamsDto.getAuditStatus()),MediaFiles::getAuditStatus,queryMediaParamsDto.getAuditStatus());
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());

    }

    /**
     * 根据扩展名获取mimeType
     *
     * @param extension 文件扩展名
     * @return mimeType
     */
    private String getMimeType(String extension) {
        if (extension == null)
            extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 将文件上传到minIO
     *
     * @param localFilePath 本地文件路径
     * @param mimeType      文件的ContentType
     * @param bucket        桶
     * @param objectName    MinIO中的路径
     * @return 是否成功
     */
    @Override
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(testbucket);
            log.info("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket, objectName, e.getMessage(), e);
        }
        return false;
    }

    /**
     * 继承自接口的方法
     *
     * @param companyId           机构ID
     * @param uploadFileParamsDto 上传文件的基本信息
     * @param localFilePath       待上传文件的本地路径
     * @param objectName MinIo路径名，兼具上传图片与课程静态化HTML页面功能。如果为空，按照年月日路径存储
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }
        //文件名称
        String filename = uploadFileParamsDto.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //文件mimeType
        String mimeType = getMimeType(extension);
        //文件的md5值
        String fileMd5 = getFileMd5(file);
        //文件的默认目录
        String defaultFolderPath = getDefaultFolderPath();
        if (StringUtils.isBlank(objectName)){
            //使用默认的年月日存储
            //存储到minio中的对象名(带目录)
            objectName = defaultFolderPath + fileMd5 + extension;
        }
        //将文件上传到minio
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucketFiles, objectName);
        if (!result) {
            XueChengPlusException.cast("文件上传失败，请重试！");
        }
        //文件大小
        uploadFileParamsDto.setFileSize(file.length());
        //将文件信息存储到数据库,使用代理对象，防止事务失效
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketFiles, objectName);
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件上传后保存信息失败");
        }
        //准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;

    }

    /**
     * @param fileMd5 文件的md5
     * @description 检查文件是否存在
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查询数据库中是否有这条记录
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //数据库中有记录,查询MinIO中是否有记录
            String objectName = mediaFiles.getFilePath();
            String bucket = mediaFiles.getBucket();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    //MinIO中存在该文件
                    inputStream.close();
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("文件不存在");
            }
        }
        //文件不存在
        return RestResponse.success(false);
    }

    /**
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     *                   MD5的前两位分别作为两级子目录，然后下面是chunk目录，存储分块文件
     * @description 检查分块是否存在
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //拿到MinIO中文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //构建GetObjectArgs
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketVideo)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null) {
                //要查询的分块文件已存在
                inputStream.close();
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            //分块文件不存在
            e.printStackTrace();
        }
        //文件不存在
        return RestResponse.success(false);
    }

    /**
     * 上传分块
     *
     * @param fileMD5            文件MD5值
     * @param chunk              分块序号
     * @param localChunkFilePath 分块文件本地路径
     */
    @Override
    public RestResponse uploadChunk(String fileMD5, int chunk, String localChunkFilePath) {
        //MinIO中分块文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMD5) + chunk;
        //获取MimeType
        String mimeType = getMimeType(null);
        //将分块文件上传到MinIO
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucketVideo, chunkFilePath);
        if (!b) {
            return RestResponse.validfail(false, "上传分块文件失败，请稍后重试");
        }
        return RestResponse.success(true);
    }

    /**
     * 在MinIO中合并分块
     *
     * @param companyId           机构ID
     * @param fileMd5             文件MD5值
     * @param chunkTotal          分块总数
     * @param uploadFileParamsDto 文件详细信息
     */
    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String objectName = getFilePathByMd5(fileMd5, uploadFileParamsDto.getFilename().substring(uploadFileParamsDto.getFilename().lastIndexOf(".")));
        //找到分块文件调用MinIO进行合并
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucketVideo)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //指定合并后的文件信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucketVideo)
                .object(objectName)
                .sources(sources)
                .build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错！ objectName:{},bucket:{},errorMessage:{}", objectName, bucketVideo, e.getMessage());
            return RestResponse.validfail(false, "合并文件出错");
        }
        //校验合并后的文件是否与源文件一致，方法是先下载再比较
        //下载合并后的文件
        File fileFromMinIO = downloadFromMinIO(bucketVideo, objectName);
        try (FileInputStream fileInputStream = new FileInputStream(fileFromMinIO)) {
            //计算合并后文件MD5值
            String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);
            //比较原始MD5与下载后文件的MD5值
            if (!mergeFileMd5.equals(fileMd5)) {
                //MD5不一致
                log.error("合并文件与原始文件MD5值不一致，合并后文件：{},原始文件：{}", mergeFileMd5, fileMd5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            //设置文件大小
            uploadFileParamsDto.setFileSize(fileFromMinIO.length());
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        }
        //将文件信息入库
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketVideo, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }
        //清理MinIO中分块文件
        clearChunkFiles(chunkFileFolderPath,chunkTotal);
        return RestResponse.success(true);
    }

    /**
     * 清理MinIO中分块文件
     * @param chunkFileFolderPath 分块文件在MinIO中的路径
     * @param chunkTotal 要清理的总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal) {
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());
            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucketVideo).objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            //要想真正删除，必须遍历一遍,否则无效
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }

    /**
     * 文件上传MinIO成功后，将文件信息保存到数据库
     */
    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles);
                XueChengPlusException.cast("保存文件信息失败");
            }
            //记录待处理任务
            addWaitingTask(mediaFiles);
            log.debug("保存文件信息到数据库成功,{}", mediaFiles);
            return mediaFiles;

        }
        return mediaFiles;

    }
    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //文件名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String exension = filename.substring(filename.lastIndexOf("."));
        //文件mimeType
        String mimeType = getMimeType(exension);
        //如果是avi视频添加到视频待处理表
        if("video/x-msvideo".equals(mimeType)){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");//未处理
            mediaProcess.setFailCount(0);//失败次数默认为0
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    /**
     * 从MinIO下载文件
     *
     * @param bucket     桶
     * @param objectName 文件在MinIO中的路径
     * @return 下载的文件
     */
    public File downloadFromMinIO(String bucket, String objectName) {
        File minIoFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minIoFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minIoFile);
            IOUtils.copy(inputStream, outputStream);
            return minIoFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/") + "/";
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据MD5值得到分块文件路径
     *
     * @param fileMd5 文件MD5值
     * @return 分块文件路径
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 得到MinIO中合并后文件地址路径
     *
     * @param fileMd5 文件MD5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
