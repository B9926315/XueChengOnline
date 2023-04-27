package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @Author Planck
 */
@Mapper
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    /**
     * 查询要处理的任务
     * @param shardTotal 分片任务数
     * @param shardIndex 分片序号
     * @param count 每次查询最大数
     * @return 任务队列
     */
    @Select("select * from xuecheng_media.media_process t where t.id % #{shardTotal} = #{shardIndex}and (t.status=1 or t.status=3) and t.fail_count<3 limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

    /**
     * 通过数据库方式设计一个分布式锁
     * @param id 任务ID
     * @return 更新记录数
     */
    @Update("update xuecheng_media.media_process m set m.status='4' where (m.status='1' or m.status='3' ) and m.fail_count<3 and m.id=#{id}")
    int startTask(long id);
}
