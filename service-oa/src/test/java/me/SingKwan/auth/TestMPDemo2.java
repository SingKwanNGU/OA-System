package me.SingKwan.auth;

import me.SingKwan.auth.service.SysRoleService;
import me.SingKwan.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth
 * @className: TestMPDemo2
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/2 21:03
 * @version: 1.0
 */
@SpringBootTest
public class TestMPDemo2 {

    @Autowired
    private SysRoleService service;


    @Test
    public void testInsert(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");

        boolean result = service.save(sysRole);
        System.out.println(result); //影响的行数
        System.out.println(sysRole); //id自动回填
    }



}
