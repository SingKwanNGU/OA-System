package me.SingKwan.process.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import me.SingKwan.auth.service.SysUserService;
import me.SingKwan.common.result.Result;
import me.SingKwan.model.process.Process;
import me.SingKwan.model.process.ProcessTemplate;
import me.SingKwan.model.process.ProcessType;
import me.SingKwan.process.service.OaProcessService;
import me.SingKwan.process.service.OaProcessTemplateService;
import me.SingKwan.process.service.OaProcessTypeService;
import me.SingKwan.vo.process.ApprovalVo;
import me.SingKwan.vo.process.ProcessFormVo;
import me.SingKwan.vo.process.ProcessVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.process.controller.api
 * @className: ProcessController
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/22 22:22
 * @version: 1.0
 */
@Api(tags = "审批流管理")
@RestController
@RequestMapping(value="/admin/process")
@CrossOrigin  //跨域
public class ProcessController {

    @Autowired
    private OaProcessTypeService processTypeService;

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private OaProcessService processService;


    @ApiOperation("待处理")
    @GetMapping("/findPending/{page}/{limit}")
    public  Result findPending(
            @ApiParam(name="page",value = "当前页码",required = true)
            @PathVariable("page")Long page,
            @ApiParam(name="limit",value = "每页记录数",required = true)
            @PathVariable("limit")Long limit){
        Page<Process> pageParam=new Page<>(page,limit);
        IPage<ProcessVo> pageModel=processService.findPending(pageParam);
        return Result.ok(pageModel);

    }

    @ApiOperation(value = "获取审批模板")
    @GetMapping("/getProcessTemplate/{processTemplateId}")
    public Result get(@PathVariable("processTemplateId") Long processTemplateId) {
        ProcessTemplate processTemplate = processTemplateService.getById(processTemplateId);
        return Result.ok(processTemplate);
    }

    //查询所有审批分类和每个分类下所有的审批模板
    @ApiOperation("查询所有审批分类和每个分类下所有的审批模板")
    @GetMapping("/findProcessType")
    public Result findProcessType(){
        List<ProcessType> list=processTypeService.findProcessType();
        return Result.ok(list);
    }

    //启动流程实例
    @ApiOperation("启动流程实例")
    @PostMapping("/startUp")
    public Result startUp(@RequestBody ProcessFormVo processFormVo){
        processService.startUp(processFormVo);
        return Result.ok();
    }

    //查看审批详情信息
    @ApiOperation("查看审批详情信息")
    @GetMapping("/show/{id}")
    public Result show(@PathVariable("id")Long id){
        Map<String,Object> map=processService.show(id);
        return Result.ok(map);
    }

    //审批
    @ApiOperation("审批")
    @PostMapping("/approve")
    public Result approve(@RequestBody ApprovalVo approvalVo){
        processService.approve(approvalVo);
        return Result.ok();
    }


    //已处理
    @ApiOperation("已处理")
    @GetMapping("/findProcessed/{page}/{limit}")
    public Result findProcessed(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable("page") Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable("limit") Long limit) {
        Page<Process> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel=processService.findProcessed(pageParam);
        return Result.ok(pageModel);
    }

    //已发起
    @ApiOperation( "已发起")
    @GetMapping("/findStarted/{page}/{limit}")
    public Result findStarted(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable("page") Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable("page") Long limit){
        Page<ProcessVo> pageParam=new Page<>(page,limit);
        IPage<ProcessVo> pageModel=processService.findStarted(pageParam);
        return Result.ok(pageModel);
    }




}
