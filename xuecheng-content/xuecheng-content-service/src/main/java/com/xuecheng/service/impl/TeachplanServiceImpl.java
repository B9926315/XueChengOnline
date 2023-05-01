package com.xuecheng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.dto.*;
import com.xuecheng.exception.CommonError;
import com.xuecheng.exception.XueChengPlusException;
import com.xuecheng.mapper.TeachplanMapper;
import com.xuecheng.mapper.TeachplanMediaMapper;
import com.xuecheng.pojo.Teachplan;
import com.xuecheng.pojo.TeachplanMedia;
import com.xuecheng.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @Author Planck
 * @Date 2023-03-29 - 22:01
 */
@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;
    /**
     * 根据课程ID查询课程计划
     * @param courseId 课程ID
     * @return 课程计划
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * 新增/修改/保存课程计划
     */
    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //课程计划id
        Long id = teachplanDto.getId();
        //修改课程计划
        if(id!=null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            //新增课程计划
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(count+1);
            BeanUtils.copyProperties(teachplanDto,teachplanNew);
            teachplanMapper.insert(teachplanNew);
        }

    }
    /**
     * 根据课程计划ID删除
     * @param teachPlanId 课程计划ID
     * 正常删除返回1，异常返回2
     */
    @Transactional
    @Override
    public void deleteTeachPlan(Long teachPlanId) {
        if (teachPlanId==null) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }
        //直接判断当前课程计划下是否有小节，有则抛异常，无则删除
        //如果是“章”，有小节则不能删除，抛异常；如果是小节，同理
        LambdaQueryWrapper<Teachplan> queryWrapper=new LambdaQueryWrapper<>();
        // select * from teachplan where parentid = {当前章计划id}
        queryWrapper.eq(Teachplan::getParentid,teachPlanId);
        //获取以teachPlanId为父节点的条目数，大于零说明该章节下有小节
        Integer count = teachplanMapper.selectCount(queryWrapper);
        if (count>0){
            XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            return;
        }
        //课程计划下无小节，直接删除
        teachplanMapper.deleteById(teachPlanId);
        //删除媒资数据
        LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper=new LambdaQueryWrapper<>();
        mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId,teachPlanId);
        teachplanMediaMapper.delete(mediaLambdaQueryWrapper);
    }
    /**
     * 对课程计划的章与节进行上移下移,核心：交换orderBy字段值
     * @param moveType 字符串，moveup上移，movedown下移
     * @param teachPlanId 课程计划ID
     */
    @Transactional
    @Override
    public void orderByTeachPlan(String moveType, Long teachPlanId) {
        if (StringUtils.isBlank(moveType) || teachPlanId==null){
            //请求参数为空
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        //获取当前课程的层级与orderBy字段值，章节移动与小节移动处理方式不同
        Integer grade = teachplan.getGrade();
        Integer orderby = teachplan.getOrderby();
        //章节移动，比较同一课程ID下的orderBy
        Long courseId = teachplan.getCourseId();
        //小节移动，比较同意章节ID下的orderBy,查询getParentid即表示该对象是小节，查询该小节的章节ID
        Long parentid = teachplan.getParentid();
        if (grade==null || orderby==null || courseId==null || parentid==null){
            //参数为不完整
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
            log.error("通过课程计划ID查询出的对象层级字段或者排序字段为空！");
        }
        //进行向上移动
        if ("moveup".equals(moveType)){
            if (grade==1){
// 章节上移，找到上一个章节的orderby，然后与其交换orderby
// SELECT * FROM teachplan WHERE courseId = 117 AND grade = 1  AND orderby < 1 ORDER BY orderby DESC LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getGrade,1)
                            .eq(Teachplan::getCourseId,courseId)
                            .lt(Teachplan::getOrderby,orderby)
                            .orderByDesc(Teachplan::getOrderby)
                            .last("limit 1");
                //查询出排在它上面的对象
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                //自定义交换函数
                exchangeOrderby(teachplan, tmp);
            }else if (grade==2){
//小节上移
// SELECT * FROM teachplan WHERE parentId = 268 AND orderby < 5 ORDER BY orderby DESC LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);
            }
        }else if ("movedown".equals(moveType)){
            if (grade == 1) {
                // 章节下移
                // SELECT * FROM teachplan WHERE courseId = 117 AND grade = 1 AND orderby > 1 ORDER BY orderby ASC LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getCourseId, courseId)
                        .eq(Teachplan::getGrade, grade)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);
            } else if (grade == 2) {
                // 小节下移
                // SELECT * FROM teachplan WHERE parentId = 268 AND orderby > 1 ORDER BY orderby ASC LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);
            }
        }

    }

    /**
     * 课程计划与媒资信息绑定
     * @param bindTeachPlanMediaDto 模型类
     */
    @Transactional
    @Override
    public void associationMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto) {
        //先拿到课程计划ID
        Long teachplanId = bindTeachPlanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (Objects.isNull(teachplan)) {
            XueChengPlusException.cast("课程计划不存在！");
        }
        //先删除原有记录，根据课程计划ID删除绑定的媒资
        LambdaQueryWrapper<TeachplanMedia> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,bindTeachPlanMediaDto.getTeachplanId());
        teachplanMediaMapper.delete(queryWrapper);
        //添加新纪录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachPlanMediaDto,teachplanMedia);
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachPlanMediaDto.getFileName());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    /**
     * 交换两个Teachplan的orderby
     * @param teachplan 交换对象
     * @param tmp 被交换对象
     */
    private void exchangeOrderby(Teachplan teachplan, Teachplan tmp) {
        if (tmp==null){
            //tmp为空，说明teachplan已经在顶端或者末端，无需移动
            XueChengPlusException.cast("已经到头啦！不能再移了");
        }
        Integer tmpOrderby = tmp.getOrderby();
        Integer teachplanOrderby = teachplan.getOrderby();
        if (tmpOrderby==null ||teachplanOrderby==null){
            XueChengPlusException.cast("出错了，请稍后重试");
            log.error("交换字段为null");
        }
        tmp.setOrderby(teachplanOrderby);
        teachplan.setOrderby(tmpOrderby);
        teachplanMapper.updateById(tmp);
        teachplanMapper.updateById(teachplan);
    }

    /**
     * @Description 获取最新的排序号
     * @param courseId  课程id
     * @param parentId  父课程计划id
     * @return int 最新排序号
     */
    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }
}
