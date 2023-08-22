package me.SingKwan.auth.service.impl;

import me.SingKwan.auth.service.SysMenuService;
import me.SingKwan.auth.service.SysUserService;
import me.SingKwan.model.system.SysUser;
import me.SingKwan.security.custom.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.service.impl
 * @className: UserDetailsServiceImpl
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/13 22:35
 * @version: 1.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getUserByUsername(username);
        if(null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }

        if(sysUser.getStatus().intValue() == 0) {
            throw new RuntimeException("账号已停用");
        }
        //根据userId查询用户的操作权限数据
        List<String> userPermsList = sysMenuService.findUserPermsByUserId(sysUser.getId());
        //创建list集合，封装最终权限数据
        // 把userPermsList转成最终权限数据要求的类型Collection<? extends GrantedAuthority>
        //即GrantedAuthority的子类或者它自己本身，类上限是GrantedAuthority本身，但他是接口，只能封装成其子类
        List<SimpleGrantedAuthority> authList=new ArrayList<>();
        //遍历userPermsList，封装进authList中
        for (String perm : userPermsList) {
            authList.add(new SimpleGrantedAuthority(perm.trim()));
        }

        return new CustomUser(sysUser, authList);
    }
}
