package me.SingKwan.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import me.SingKwan.auth.mapper.SysRoleMapper;
import me.SingKwan.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth
 * @className: TestMPDemo1
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/2 17:15
 * @version: 1.0
 */

@SpringBootTest
public class TestMPDemo1 {

    //注入mapper
    @Autowired
    private SysRoleMapper sysRoleMapper;


    //查询所有系统角色
    @Test
    public void getall(){
        List<SysRole> sysRoles = sysRoleMapper.selectList(null);
        sysRoles.forEach(System.out::println);
    }

    //添加系统角色
    @Test
    public void add(){
        SysRole role=new SysRole();
        role.setRoleName("角色管理员");
        role.setRoleCode("3");
        role.setDescription("角色管理员");
        int insert = sysRoleMapper.insert(role);
        System.out.println(insert);
        System.out.println(role.getId());
        System.out.println(role);
    }

    //修改系统角色
    @Test
    public void update(){
        //根据id查询
        SysRole role = sysRoleMapper.selectById(12);
        //正常来说需要获取id,可以当做参数传来。

        //设置修改值
        role.setDescription("希儿角色管理员");

        //调用方法实现最终修改
        sysRoleMapper.updateById(role);
    }

    //删除系统角色
    @Test
    public void deleteId(){
        int rows=sysRoleMapper.deleteById(12);

    }

    //批量删除系统角色
    @Test
    public void  deleteBatch(){
        int i = sysRoleMapper.deleteBatchIds(Arrays.asList(9,10));
        System.out.println(i);
    }


    //条件查询
    @Test
    public void testQuery1(){
        //1 创建QueryWrapper对象，调用方法封装条件
        QueryWrapper<SysRole> wrapper=new QueryWrapper<>();
        wrapper.eq("role_name","李四");
        //2 调用mp方法实现查询操作
        List<SysRole> sysRoles = sysRoleMapper.selectList(wrapper);
        System.out.println(sysRoles);

    }

    //lambada条件查询
    @Test
    public void testQuery2(){
        //1 创建LambadaQueryWrapper对象，调用方法封装条件
        LambdaQueryWrapper<SysRole> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleName,"李四");
        //2 调用mp方法实现查询操作
        List<SysRole> sysRoles = sysRoleMapper.selectList(wrapper);
        System.out.println(sysRoles);

    }

}
