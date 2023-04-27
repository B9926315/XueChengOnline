package com.xuecheng.service;

import com.xuecheng.dto.CoursePreviewDto;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

/**
 * @Author Planck
 * @Date 2023-04-22 - 20:09
 */
@SpringBootTest
public class FreemakerTest {
    @Autowired
    private CoursePublishService coursePublishService;
    /**
     * 测试freemaker模板生成静态页面
     */
    @Test
    void testGenerateHtmlByTemplate() throws Exception{
        //获取FreeMaker版本
        Configuration configuration = new Configuration(Configuration.getVersion());
        //拿到classpath路径
        String classpath = Objects.requireNonNull(getClass().getResource("/")).getPath();
        //拿取指定模板的目录
        configuration.setDirectoryForTemplateLoading(new File(classpath+"/templates/"));
        //指定编码
        configuration.setDefaultEncoding("UTF-8");
        //得到模板
        Template template = configuration.getTemplate("courseInfo.ftl");
        //准备数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(1L);
        HashMap<String, CoursePreviewDto> CoursePreviewDtoHashMap = new HashMap<>();
        CoursePreviewDtoHashMap.put("model",coursePreviewInfo);
        String courseHtml = FreeMarkerTemplateUtils.processTemplateIntoString(template, CoursePreviewDtoHashMap);
        //输入流
        InputStream inputStream = IOUtils.toInputStream(courseHtml, "UTF-8");
        //输出文件
        FileOutputStream outputStream = new FileOutputStream("D:\\1临时文件\\1.html");
        //写出
        IOUtils.copy(inputStream,outputStream);
        inputStream.close();
        outputStream.close();
    }
}
