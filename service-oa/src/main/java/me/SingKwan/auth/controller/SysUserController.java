package me.SingKwan.auth.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.SingKwan.auth.service.SysUserService;
import me.SingKwan.common.result.Result;
import me.SingKwan.common.utils.MD5;
import me.SingKwan.model.system.SysUser;
import me.SingKwan.vo.system.AssignRoleVo;
import me.SingKwan.vo.system.SysUserQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-07
 */
@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/admin/system/sysUser")
@CrossOrigin
public class SysUserController {

    @Autowired
    SysUserService sysUserService;


    @ApiOperation("用户条件分页查询")
    @GetMapping("/{page}/{limit}")
    public Result getUser(@PathVariable("page") Long page,
                          @PathVariable("limit") Long limit,
                          SysUserQueryVo sysUserQueryVo){

        //page 当前页 limit 每页显示记录数
        //1 创建Page对象， 传递分页参数
        Page<SysUser> pageParam=new Page<>(page,limit);

        //2 封装分页条件
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        String userName = sysUserQueryVo.getKeyword();
        String createTimeBegin = sysUserQueryVo.getCreateTimeBegin();
        String createTimeEnd = sysUserQueryVo.getCreateTimeEnd();
        if (StringUtils.hasText(userName)){
            wrapper.like(SysUser::getUsername,userName);
        }
        if (StringUtils.hasText(createTimeBegin)){
            wrapper.ge(SysUser::getCreateTime,createTimeBegin);
        }
        if (StringUtils.hasText(createTimeEnd)){
            wrapper.le(SysUser::getCreateTime,createTimeEnd);
        }

        IPage<SysUser> pageModel=sysUserService.page(pageParam,wrapper);

        return Result.ok(pageModel);
    }


    @ApiOperation("添加用户")
    @PostMapping("/save")
    public Result save(@RequestBody SysUser user){
        //密码进行加密，使用MD5
        String password = user.getPassword();
        String passwordMD5 = MD5.encrypt(password);
        user.setPassword(passwordMD5);
        sysUserService.save(user);

        return Result.ok();

    }

    @ApiOperation("删除用户")
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable("id") Long id){
        boolean is_success = sysUserService.removeById(id);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @ApiOperation("批量删除")
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Long> idList){
        boolean removed = sysUserService.removeByIds(idList);
        if(removed){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    @ApiOperation("修改用户-根据id查询用户")
    @GetMapping("/get/{id}")
    public Result getUserById(@PathVariable("id") Long id){
        SysUser user = sysUserService.getById(id);
        return Result.ok(user);
    }

    @ApiOperation("修改用户-最终修改")
    @PutMapping("/update")
    public Result update(@RequestBody SysUser user){
        boolean updated = sysUserService.updateById(user);
        if(updated){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }


    //1 获取所有角色 和 当前用户所属角色
    @ApiOperation("获取当前用户所有角色")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable("userId") Long userId){
        Map<String,Object> map=sysUserService.findRoleDataByUserId(userId);
        return Result.ok(map);
    }


    //2 为用户分配角色 和 修改状态使用户可否使用
    @ApiOperation("根据用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssignRoleVo assignRoleVo){
        sysUserService.doAssign(assignRoleVo);
        return Result.ok();
    }

    //3 实现修改状态使用户是否可用
    @ApiOperation("更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable("id") Long id,
                               @PathVariable("status")Integer status){
        sysUserService.updateStatus(id,status);
        return Result.ok();
    }

    //获取当前用户信息
    @ApiOperation("获取当前用户信息")
    @GetMapping("/getCurrentUser")
    public Result getCurrentUser(){
        Map<String,Object> map=sysUserService.getCurrentUser();
        return Result.ok(map);
    }
}

