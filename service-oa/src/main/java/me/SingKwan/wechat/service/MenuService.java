package me.SingKwan.wechat.service;


import com.baomidou.mybatisplus.extension.service.IService;
import me.SingKwan.model.wechat.Menu;
import me.SingKwan.vo.wechat.MenuVo;

import java.util.List;

/**
 * <p>
 * 菜单 服务类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-29
 */
public interface MenuService extends IService<Menu> {

    List<MenuVo> findMenuInfo();

    void syncMenu();

    void removeMenu();
}
