package me.SingKwan.process.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import me.SingKwan.common.result.Result;
import me.SingKwan.model.process.ProcessTemplate;
import me.SingKwan.process.service.OaProcessTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 审批模板 前端控制器
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-20
 */
@Api(value = "审批模板管理", tags = "审批模板管理")
@RestController
@RequestMapping(value = "/admin/process/processTemplate")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OaProcessTemplateController {
    @Autowired
    private OaProcessTemplateService processTemplateService;

    //@PreAuthorize("hasAuthority('bnt.processTemplate.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<ProcessTemplate> pageParam = new Page<>(page, limit);
        //分页查询审批模板，把审批类型对应名称查询
        IPage<ProcessTemplate> pageModel = processTemplateService.selectPageProcessTemplate(pageParam);
        return Result.ok(pageModel);
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.list')")
    @ApiOperation(value = "获取")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id) {
        ProcessTemplate processTemplate = processTemplateService.getById(id);
        return Result.ok(processTemplate);
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "新增")
    @PostMapping("/save")
    public Result save(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.save(processTemplate);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "修改")
    @PutMapping("/update")
    public Result updateById(@RequestBody ProcessTemplate processTemplate) {
        processTemplateService.updateById(processTemplate);
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.processTemplate.remove')")
    @ApiOperation(value = "删除")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id) {
        processTemplateService.removeById(id);
        return Result.ok();
    }


//    @PreAuthorize("hasAuthority('bnt.processTemplate.templateSet')")
    @ApiOperation(value = "上传流程定义")
    @PostMapping("/uploadProcessDefinition")
    public Result uploadProcessDefinition(MultipartFile file) throws FileNotFoundException {
        //获取classes目录位置
        String path = new File(ResourceUtils.getURL("classpath:")
                .getPath())
                .getAbsolutePath();
        //设置上传文件夹
        File tempFile=new File(path+"/processes/");
        //判断文件是否存在
        if(!tempFile.exists()){
            //创建目录
            tempFile.mkdirs();
        }
        //创建空文件，用于文件写入
        String fileName = file.getOriginalFilename();
        File zipFile=new File(path+"/processes/"+fileName);
        //保存文件流到本地
        try {
            file.transferTo(zipFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("上传失败");
        }

        Map<String, Object> map=new HashMap<>();
        //根据上传地址后续部署流程定义，文件名称为流程定义的默认key
        map.put("processDefinitionPath","processes/"+fileName);
        map.put("processDefinitionKey",fileName.substring(0,fileName.lastIndexOf(".")));
        return Result.ok(map);

    }


//    @PreAuthorize("hasAuthority('bnt.processTemplate.publish')")
    @ApiOperation(value = "发布")
    @GetMapping("/publish/{id}")
    public Result publish(@PathVariable("id")Long id){
        //修改模板发布状态 1表示已经发布
        //流程定义部署
        processTemplateService.publish(id);
        return Result.ok();
    }


}

