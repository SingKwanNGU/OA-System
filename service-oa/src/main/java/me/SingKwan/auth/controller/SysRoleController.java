package me.SingKwan.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.SingKwan.auth.service.SysRoleService;
import me.SingKwan.common.config.exception.CustomizeException;
import me.SingKwan.common.result.Result;
import me.SingKwan.model.system.SysRole;
import me.SingKwan.vo.system.SysRoleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.controller
 * @className: SysRoleController
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/2 22:35
 * @version: 1.0
 */
@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {

    //注入service
    @Autowired
    private SysRoleService sysRoleService;

    //查询所有角色
//    @GetMapping("/find")
//    public List<SysRole> findAll(){
//        List<SysRole> list = sysRoleService.list();
//        return list;
//    }
    @ApiOperation("查询所有的角色")
    @GetMapping("/find")
    public Result findAll(){
//        try {
//            int a= 10/0;
//        } catch (Exception e) {
//            throw new CustomizeException(202,"这里抛出一个自定义异常");
//        }
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }

    //条件分页查询
    //page 当前页 limit 每页显示记录数
    //sysRoleQueryVo 条件对象
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("条件分页查询")
    @GetMapping("/{page}/{limit}")
    public Result getPages(@PathVariable("page") Long page,
                           @PathVariable("limit") Long limit,
                           SysRoleQueryVo sysRoleQueryVo){
        //调用service的方法
        //1 常见Page对象，传递分页参数
        //page 当前页 limit 每页显示记录数
        Page<SysRole> pageParam=new Page<>(page,limit);
        //2 封装条件，需先进行非空判断，条件不为空即可开始封装|
        LambdaQueryWrapper<SysRole> wrapper=new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if (StringUtils.hasText(roleName)){
            //封装条件
            wrapper.like(SysRole::getRoleName,roleName);
        }
        //3 调用方法实现
        IPage<SysRole> iPage=sysRoleService.page(pageParam,wrapper);

        return Result.ok(iPage);
    }

    //添加角色
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole role){
        //调用service的方法
        boolean is_success = sysRoleService.save(role);
        if(is_success){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //修改角色-根据id查询角色
    @ApiOperation("修改角色-根据id查询角色")
    @GetMapping("/get/{id}")
    public Result getRoleById(@PathVariable("id") Long id){
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }

    //修改角色-最终修改
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色-最终修改")
    @PutMapping("/update")
    public Result update(@RequestBody SysRole role){
        boolean updated = sysRoleService.updateById(role);
        if(updated){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //根据id删除
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id删除")
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable("id") Long id){
        boolean removed = sysRoleService.removeById(id);
        if (removed){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //批量删除
    //需要从前端获取所要删除的id的集合，以数组格式传输过来
    //json与java的转换  json的map{k:v} --> java的对象{属性名：属性值}
    //json与java的转换  json的数组[]  --> java的List集合
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Long> idList){
        boolean removed = sysRoleService.removeByIds(idList);
        if (removed){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }







}
