package me.SingKwan.auth.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.SingKwan.auth.service.SysMenuService;
import me.SingKwan.common.result.Result;
import me.SingKwan.model.system.SysMenu;
import me.SingKwan.model.system.SysRoleMenu;
import me.SingKwan.vo.system.AssignMenuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单表 前端控制器
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-09
 */
@Api(tags = "菜单管理接口")
@RestController
@RequestMapping("/admin/system/sysMenu")
public class SysMenuController {

    @Autowired
    SysMenuService sysMenuService;

    //查询所有菜单和角色分配的菜单
    @ApiOperation("查询所有菜单和角色分配的菜单")
    @GetMapping("/toAssign/{roleId}")
    public Result toAssign(@PathVariable("roleId")Long roleId){
      List<SysMenu> list=sysMenuService.findMenuByRoleId(roleId);
      return Result.ok(list);
    }

    //给角色分配菜单
    @ApiOperation("给角色分配菜单")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssignMenuVo assignMenuVo){
        sysMenuService.doAssign(assignMenuVo);

        return Result.ok();
    }









    //菜单列表接口
    @ApiOperation("菜单列表")
    @GetMapping("/findNodes")
    public Result findNodes(){
        List<SysMenu> list=sysMenuService.findNodes();
        return Result.ok(list);
    }

    @ApiOperation(value = "新增菜单")
    @PostMapping("save")
    public Result save(@RequestBody SysMenu permission) {
        sysMenuService.save(permission);
        return Result.ok();
    }

    @ApiOperation(value = "修改菜单")
    @PutMapping("update")
    public Result updateById(@RequestBody SysMenu permission) {
        sysMenuService.updateById(permission);
        return Result.ok();
    }

    @ApiOperation(value = "删除菜单")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        sysMenuService.removeMenuById(id);
        return Result.ok();
    }
}

