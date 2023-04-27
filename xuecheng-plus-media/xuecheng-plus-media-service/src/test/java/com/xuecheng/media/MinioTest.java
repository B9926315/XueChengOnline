package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author Planck
 * @Date 2023-04-02 - 11:53
 */
public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://43.143.246.214:9000")
                    .credentials("planck", "xuanyunyi645134")
                    .build();

    @Test
    void testUpload() throws Exception {
        //通过扩展名得到媒体资源类型
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("video/BYX.mp4")
                        .filename("D:\\1临时文件\\视频\\BYX.mp4")
                        .contentType(mimeType)
                        .build());
    }

    @Test
    void testDelete() throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket("testbucket").object("video/BYX.mp4").build());
    }

    @Test
    void testGetFile() throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("video/BYX.mp4").build();
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        //指定输出流
        FileOutputStream outputStream = new FileOutputStream("D:\\1临时文件\\BYX.mp4");
        IOUtils.copy(inputStream, outputStream);
        //校验文件完整性(利用MD5加密算法)
        //MinIO中的MD5值
        String source_md5 = DigestUtils.md5Hex(new FileInputStream("D:\\1临时文件\\视频\\BYX.mp4"));
        //下载到本地的MD5值
        String local_md5 = DigestUtils.md5Hex(new FileInputStream("D:\\1临时文件\\BYX.mp4"));
        if (source_md5.equals(local_md5)) {
            System.out.println("下载成功");
        }
    }

    /**
     * 上传分块
     */
    @Test
    void uploadChunkFile() throws Exception{
        for (int i = 0; i < 2; i++) {
            UploadObjectArgs uploadObjectArgs=UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("D:\\1临时文件\\视频\\测试\\"+i)
                    .object("chunk1/"+i)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("分块 "+i+" 上传成功");
        }
    }

    /**
     * 合并上传的分块
     */
    @Test
    void testMergeFile() throws Exception{
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(2)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk1/".concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //指定合并后的文件信息
        ComposeObjectArgs composeObjectArgs=ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("video/BYX1.mp4")
                .sources(sources)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }
}
