package me.SingKwan.process.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import me.SingKwan.common.result.Result;
import me.SingKwan.model.process.ProcessType;
import me.SingKwan.process.service.OaProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-20
 */
@RestController
@RequestMapping("/admin/process/processType")
public class OaProcessTypeController {

    @Autowired
    private OaProcessTypeService processTypeService;

    //查询所有审批分类
    @ApiOperation("查询所有审批分类")
    @GetMapping("/findAll")
    public Result findAll(){
        List<ProcessType> list = processTypeService.list();
        return Result.ok(list);
    }


//    @PreAuthorize("hasAuthority('bnt.processType.list')")
    @ApiOperation("获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(@PathVariable("page")Long page,
                        @PathVariable("limit")Long limit){
        Page<ProcessType> pageParam=new Page<>(page,limit);
        IPage<ProcessType> pageModel = processTypeService.page(pageParam);
        return Result.ok(pageModel);
    }

//    @PreAuthorize("hasAuthority('bnt.processType.list')")
    @ApiOperation("获取")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable("id") Long id ){
        ProcessType processType = processTypeService.getById(id);
        return Result.ok(processType);
    }

//    @PreAuthorize("hasAuthority('bnt.processType.add')")
    @ApiOperation("新增")
    @PostMapping("/save")
    public Result save(@RequestBody ProcessType processType){
        processTypeService.save(processType);
        return Result.ok();
    }

//    @PreAuthorize("hasAuthority('bnt.processType.update')")
    @ApiOperation("修改")
    @PutMapping("/update")
    public Result updateById(@RequestBody ProcessType processType){
        processTypeService.updateById(processType);
        return Result.ok();
    }

//    @PreAuthorize("hasAuthority('bnt.processType.remove')")
    @ApiOperation("删除")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable("id")Long id){
        processTypeService.removeById(id);
        return Result.ok();
    }


}

