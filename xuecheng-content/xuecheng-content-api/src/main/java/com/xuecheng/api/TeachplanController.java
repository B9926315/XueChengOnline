package com.xuecheng.api;

import com.xuecheng.dto.*;
import com.xuecheng.service.TeachplanService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author Planck
 * @Date 2023-03-29 - 19:54
 */
@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
public class TeachplanController {
    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }
    //正常删除返回1，异常返回2
    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{teachPlanId}")
    public void deleteTeachPlan(@PathVariable Long teachPlanId, HttpServletResponse response){
       teachplanService.deleteTeachPlan(teachPlanId);
    }
    @ApiOperation("对课程计划的章与节进行上移下移")
    @PostMapping("/teachplan/{moveType}/{teachPlanId}")
    public void orderByTeachPlan(@PathVariable String moveType,@PathVariable Long teachPlanId){
        teachplanService.orderByTeachPlan(moveType,teachPlanId);
    }
    @ApiOperation(value = "课程计划与媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachPlanMediaDto bindTeachPlanMediaDto){
        teachplanService.associationMedia(bindTeachPlanMediaDto);
    }
}
