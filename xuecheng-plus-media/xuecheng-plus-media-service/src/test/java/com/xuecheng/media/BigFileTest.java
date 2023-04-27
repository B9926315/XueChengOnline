package com.xuecheng.media;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

/**
 * @Author Planck
 * @Date 2023-04-03 - 23:15
 */
public class BigFileTest {
    /**
     * 测试大文件断点
     */
    @Test
    void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("D:\\1临时文件\\视频\\BYX.mp4");
        //分块文件存储路径
        String chunkFilePath = "D:\\1临时文件\\视频\\测试\\";
        //被分块后单个文件大小
        long chunkSize = 1024 * 1024 * 6;
        //计算文件将被分成多少块
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流从源文件读取数据，向分块文件夹中写数据
        RandomAccessFile rafRead = new RandomAccessFile(sourceFile, "r");
        //缓冲区大小
        byte[] bytes = new byte[1024];
        for (long i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
        //将分块文件写入流
            RandomAccessFile rafReadWrite = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = rafRead.read(bytes)) != -1) {
                rafReadWrite.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            rafReadWrite.close();
        }
        rafRead.close();
    }

    /**
     * 测试大文件断点后合并
     */
    @Test
    void testMerge() throws IOException {
        //分块文件目录
        File chunkFilePath=new File("D:\\1临时文件\\视频\\测试\\");
        //源文件
        File sourceFile=new File("D:\\1临时文件\\视频\\BYX.mp4");
        //合并后的文件
        File mergeFile=new File("D:\\1临时文件\\视频\\BYX1.mp4");
        //取出所有分块文件
        File[] files = chunkFilePath.listFiles();
        assert files != null;
        //将数组转为list后方便排序
        List<File> fileList = Arrays.asList(files);
        //为list排序
        fileList.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
        //向合并文件写的流
        RandomAccessFile rafReadWrite = new RandomAccessFile(mergeFile, "rw");
        //缓冲区
        byte[] bytes=new byte[1024];
        //开始写
        for (File file : fileList) {
            //读分块文件的流
            RandomAccessFile rafRead = new RandomAccessFile(file, "r");
            int len=-1;
            while ((len=rafRead.read(bytes))!=-1){
                rafReadWrite.write(bytes,0,len);
            }
            rafRead.close();
        }
        rafReadWrite.close();
    }
}
