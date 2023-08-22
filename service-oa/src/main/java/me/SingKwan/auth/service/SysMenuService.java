package me.SingKwan.auth.service;

import me.SingKwan.model.system.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import me.SingKwan.vo.system.AssignMenuVo;
import me.SingKwan.vo.system.RouterVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-09
 */
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> findNodes();

    void removeMenuById(Long id);

    List<SysMenu> findMenuByRoleId(Long roleId);

    void doAssign(AssignMenuVo assignMenuVo);

    List<RouterVo> findUserMenuListByUserId(Long userId);

    List<String> findUserPermsByUserId(Long userId);
}
