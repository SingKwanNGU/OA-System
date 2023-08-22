package me.SingKwan.process.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import me.SingKwan.common.result.Result;
import me.SingKwan.process.service.OaProcessService;
import me.SingKwan.vo.process.ProcessFormVo;
import me.SingKwan.vo.process.ProcessQueryVo;
import me.SingKwan.vo.process.ProcessVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-22
 */
@RestController
@RequestMapping("/admin/process")
public class OaProcessController {

    @Autowired
    private OaProcessService processService;


    //获取分页列表
    @ApiOperation("获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(@PathVariable("page")Long page,
                        @PathVariable("limit")Long limit,
                        ProcessQueryVo processQueryVo){
        Page<ProcessVo> pageParam=new Page<>(page,limit);
        IPage<ProcessVo> pageModel=processService.selectPage(pageParam,processQueryVo);
        return Result.ok(pageModel);
    }






}

