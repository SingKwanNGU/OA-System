package me.SingKwan.auth.service;

import me.SingKwan.model.system.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import me.SingKwan.vo.system.AssignRoleVo;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-07
 */
public interface SysUserService extends IService<SysUser> {

    Map<String, Object> findRoleDataByUserId( Long userId);

    void doAssign(AssignRoleVo assignRoleVo);

    void updateStatus(Long id, Integer status);

    SysUser getUserByUsername(String username);

    Map<String, Object> getCurrentUser();
}
