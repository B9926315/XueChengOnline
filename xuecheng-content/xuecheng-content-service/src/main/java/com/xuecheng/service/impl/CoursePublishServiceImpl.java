package com.xuecheng.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.config.MultipartSupportConfig;
import com.xuecheng.dto.*;
import com.xuecheng.exception.CommonError;
import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.feignclient.MediaServiceClient;
import com.xuecheng.mapper.*;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.pojo.*;
import com.xuecheng.service.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Author Planck
 * @Date 2023-04-16 - 17:40
 * 课程预览与发布、审核
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    private MediaServiceClient mediaServiceClient;
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private TeachplanService teachplanService;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    private CoursePublishMapper coursePublishMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private MqMessageService mqMessageService;

    /**
     * 为课程预览模板页面提供数据
     * @param courseId 课程ID
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //查询课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }
    /**
     * 将课程提交审核。
     * 教学机构提交审核时，系统将所有课程信息存储在课程预发布表供审核人员审核，此表教学机构无权修改
     * 课程在审核期间，教学机构不能再此提交审核(如此会导致课程预发布表数据不一致)，必须等审核通过或失败后从能再次提交审核
     * @param courseId 课程ID
     */
    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        /*
        如果课程审核状态为已提交则不允许再次提交
         */
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (Objects.isNull(courseBaseInfo)){
            XueChengPlusException.cast("课程不见了！");
        }
        String auditStatus = courseBaseInfo.getAuditStatus();
        String coursePicture = courseBaseInfo.getPic();
        if ("202003".equals(auditStatus)){
            XueChengPlusException.cast("该课程正在审核中，请稍后再提交！");
        }
        if (!Objects.equals(companyId, courseBaseInfo.getCompanyId())){
            XueChengPlusException.cast("只能向自己所属机构中添加课程");
        }
        /*
        课程图片、教学计划等信息校验，不完整则不允许提交
         */
        if (StringUtils.isBlank(coursePicture)){
            XueChengPlusException.cast("请上传课程图片");
        }
        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (Objects.isNull(teachplanTree) || teachplanTree.size()==0){
            XueChengPlusException.cast("请填写课程计划后再提交");
        }
        /*
        查询到课程基本信息、营销信息、课程计划等，并将其插入到课程预发布表
         */
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        coursePublishPre.setCompanyId(companyId);
        //确保机构ID一致

        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //将其转换为JSON字符串
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //教学计划
        String teachPlanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachPlanTreeJson);
        //课程预发布表状态为已提交
        coursePublishPre.setStatus("202003");
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //查询课程预发布表中是否含有该条记录，没有则新增，有则修改
        CoursePublishPre coursePublishPreObject = coursePublishPreMapper.selectById(courseId);
        if (Objects.isNull(coursePublishPreObject)){
            //数据库中没有此记录，执行插入操作
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            //数据库中已经有该记录，执行修改操作
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        /*
        更新课程基本信息表的审核状态为已提交
         */
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }
    /**
     * 审核通过后，进行课程发布
     * 需要做三件事：Redis缓存、elasticsearch搜索索引、将课程静态页面存进MinIO
     * @param companyId 机构ID
     * @param courseId 课程ID
     */
    @Transactional
    @Override
    public void coursePublish(Long companyId, Long courseId) {
        /*
        查询课程预发布表，若审核不通过，则不允许发布
         */
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);

        if (Objects.isNull(coursePublishPre)){
            XueChengPlusException.cast("课程不见了");
        }
        if (!Objects.equals(coursePublishPre.getCompanyId(),companyId)){
            XueChengPlusException.cast("只允许发布本机构的课程！");
        }
        String status = coursePublishPre.getStatus();
        if (!"202004".equals(status)){
            XueChengPlusException.cast("课程审核未通过，禁止发布！");
        }
        /*
        向课程发布表写入数据
         */
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        CoursePublish coursePublishObject = coursePublishMapper.selectById(courseId);
        //先查询，有则更新，无则插入
        if (Objects.isNull(coursePublishObject)){
            //插入
            coursePublishMapper.insert(coursePublish);
        }else {
            //更新
            coursePublishMapper.updateById(coursePublish);
        }
        /*
        向消息表写入数据，提醒部件去做Redis、es等操作
         */
        MqMessage course_publish = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (Objects.isNull(course_publish)){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
        /*
        删除课程预发布表该条数据
         */
        coursePublishPreMapper.deleteById(courseId);
    }
    /**
     * @Description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     */
    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile  = null;
        FileOutputStream outputStream=null;
        InputStream inputStream=null;
        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = Objects.requireNonNull(this.getClass().getResource("/")).getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("courseInfo.ftl");
            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.info("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
            if (inputStream!=null){
                inputStream.close();
            }
            if (outputStream!=null){
                outputStream.close();
            }
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }
        return htmlFile;
    }
    /**
     * @Description 上传课程静态化页面
     * @param file  静态化文件
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/"+courseId+".html");
        if(course==null){
            XueChengPlusException.cast("上传静态文件异常");
        }
    }
    /**
     * 查询课程发布信息
     * @param courseId 课程ID
     */
    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }
}
