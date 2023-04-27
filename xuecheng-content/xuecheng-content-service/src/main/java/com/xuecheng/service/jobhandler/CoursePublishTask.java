package com.xuecheng.service.jobhandler;

import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.feignclient.SearchServiceClient;
import com.xuecheng.mapper.CoursePublishMapper;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.pojo.CourseIndex;
import com.xuecheng.pojo.CoursePublish;
import com.xuecheng.service.CoursePublishService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

/**
 * @Author Planck
 * @Date 2023-04-21 - 21:33
 * 课程发布任务类
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {
    @Autowired
    private CoursePublishService coursePublishService;
    @Autowired
    private SearchServiceClient searchServiceClient;
    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @XxlJob("coursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        //分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();//执行器总数
        //调用抽象类方法执行任务
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    /**
     * 执行器
     *
     * @param mqMessage 执行任务内容
     * @return 全部任务执行成功，返回true
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        //拿取课程ID
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        generateCourseHtml(mqMessage, courseId);
        saveCourseIndex(mqMessage, courseId);
        return true;
    }

    /**
     * 生成课程静态化页面并上传MinIO
     *
     * @param mqMessage 消息
     * @param courseId  课程ID
     */
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();
        /*
        任务幂等性处理，查询数据库该阶段的执行状态
         */
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.info("任务已处理");
            return;
        }
        /*
        进行课程页面静态化
         */
        File file = coursePublishService.generateCourseHtml(courseId);
        if (Objects.isNull(file)) {
            XueChengPlusException.cast("生成的静态页面为空");
        }
        coursePublishService.uploadCourseHtml(courseId, file);
        /*
        任务处理完成，更新状态
         */
        mqMessageService.completedStageOne(taskId);
    }

    /**
     * 向Elasticsearch保存索引数据
     *
     * @param mqMessage 消息
     * @param courseId  课程ID
     */
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.info("任务已处理");
            return;
        }
        /*
        进行业务操作
         */
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add) {
            XueChengPlusException.cast("将课程文档添加到ES失败！");
            log.error("将课程文档添加到ES失败！课程ID：{}",courseId);
        }
        mqMessageService.completedStageTwo(taskId);
    }
}
