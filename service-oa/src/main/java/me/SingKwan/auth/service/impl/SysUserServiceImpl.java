package me.SingKwan.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.SingKwan.auth.service.SysRoleService;
import me.SingKwan.auth.service.SysUserRoleService;
import me.SingKwan.model.system.SysRole;
import me.SingKwan.model.system.SysUser;
import me.SingKwan.auth.mapper.SysUserMapper;
import me.SingKwan.auth.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.SingKwan.model.system.SysUserRole;
import me.SingKwan.security.custom.LoginUserInfoHelper;
import me.SingKwan.vo.system.AssignRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-07
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleService sysRoleService;

    @Override//查询所有角色，查询用户所属的角色
    public Map<String, Object> findRoleDataByUserId(Long userId) {
        //1 查询所有角色， 返回list集合
        List<SysRole> allRolesList = sysRoleService.getBaseMapper().selectList(null);
        //2 根据userId查询 角色用户关系表`sys_user_role` ,查询userId对应的角色id
        LambdaQueryWrapper<SysUserRole> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> sysUserRoles = sysUserRoleService.getBaseMapper().selectList(wrapper);

        List<Long> sysUserRolesList = sysUserRoles.stream().map(item -> {
            return item.getRoleId();
        }).collect(Collectors.toList());
        //至此获取到用户所属的所有角色id

        //3 根据角色id查询所有角色list，找到对应的角色信息
        //根据角色id到所有的角色的list集合进行比较,如果角色id在list中，说明用户有此角色
        List<SysRole> assignRoleList = allRolesList.stream().map(item -> {
            if (sysUserRolesList.contains(item.getId())) {
                return item;
            } else {
                return null;
            }
        }).collect(Collectors.toList());

        //4 把所得到的两部分数据封装成map集合，返回
        Map<String,Object> roleMap=new HashMap<>();
        roleMap.put("assignRoleList",assignRoleList);
        roleMap.put("allRolesList",allRolesList);
        return roleMap;

    }



    @Transactional
    @Override//为用户分配角色
    public void doAssign(AssignRoleVo assignRoleVo) {
        //把用户之前分配角色数据删除
        LambdaQueryWrapper<SysUserRole> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,assignRoleVo.getUserId());
        sysUserRoleService.getBaseMapper().delete(wrapper);
        //重新进行分配
        //如果roleId为空，continue跳出本轮循环，如果roleId不为空，封装新sysUserRole进入数据库
        List<Long> roleIdList = assignRoleVo.getRoleIdList();
        for (Long roleId : roleIdList) {
            if (StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole sysUserRole=new SysUserRole();
            sysUserRole.setRoleId(roleId);
            sysUserRole.setUserId(assignRoleVo.getUserId());
            sysUserRoleService.getBaseMapper().insert(sysUserRole);
        }


    }

    @Transactional
    @Override//实现修改状态使用户是否可用
    public void updateStatus(Long id, Integer status) {
        SysUser sysUser = this.getBaseMapper().selectById(id);
        if (status.intValue() == 1){
            sysUser.setStatus(status);
        }else {
            sysUser.setStatus(0);
        }
        this.getBaseMapper().updateById(sysUser);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = this.getBaseMapper().selectOne(wrapper);
        return sysUser;

    }

    @Override
    public Map<String, Object> getCurrentUser() {
        SysUser sysUser = this.getBaseMapper().selectById(LoginUserInfoHelper.getUserId());
        Map<String,Object> map=new HashMap<>();
        map.put("name",sysUser.getName());
        map.put("phone",sysUser.getPhone());
        return map;
    }
}
