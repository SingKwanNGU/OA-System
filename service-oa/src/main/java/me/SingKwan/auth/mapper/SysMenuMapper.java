package me.SingKwan.auth.mapper;

import me.SingKwan.model.system.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-09
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    //多表关联查询： 用户角色关系表、角色菜单关系表、菜单表三表联查
    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);
}
