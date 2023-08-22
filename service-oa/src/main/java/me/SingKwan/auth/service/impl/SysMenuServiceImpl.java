package me.SingKwan.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.SingKwan.auth.service.SysRoleMenuService;
import me.SingKwan.auth.utils.MenuHelper;
import me.SingKwan.common.config.exception.CustomizeException;
import me.SingKwan.model.system.SysMenu;
import me.SingKwan.auth.mapper.SysMenuMapper;
import me.SingKwan.auth.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.SingKwan.model.system.SysRoleMenu;
import me.SingKwan.vo.system.AssignMenuVo;
import me.SingKwan.vo.system.MetaVo;
import me.SingKwan.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-09
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> findNodes() {
        //1 查询所有菜单数据
        List<SysMenu> sysMenuList = this.baseMapper.selectList(null);
        if (CollectionUtils.isEmpty(sysMenuList)) return null;
        //2 构建树形结构
        List<SysMenu> resultList= MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    //删除菜单
    //实现删除菜单时，如果当前要删除的菜单有子节点，则不可删除，除非当前菜单无子节点，才可删除
    @Override
    public void removeMenuById(Long id) {
       //判断当前菜单是否有子菜单
        LambdaQueryWrapper<SysMenu> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId,id);
        Integer count = this.getBaseMapper().selectCount(wrapper);
        if(count>0){
            throw new CustomizeException(201,"当前菜单有子菜单，不可删除！");
        }else {
            this.getBaseMapper().deleteById(id);
        }
    }

    //查询所有菜单和角色分配的菜单
    //思路：查询所有菜单: 查询所有status=1的菜单（可用）， 调用findNodes()方法返回树形显示菜单
    //思路：查询用户分配菜单： 根据roleId去查sys_role_menu表-->获得与roleId的相等的sysRoleMenu对象-->从sysRoleMenu对象中获取menu_id
    //接上，最后根据menu_id查sys_menu表，封装最后的sysMenu信息返回
    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        //查询所有菜单- 添加条件 status=1
        LambdaQueryWrapper<SysMenu> sysMenuLambdaQueryWrapper=new LambdaQueryWrapper<>();
        sysMenuLambdaQueryWrapper.eq(SysMenu::getStatus,1);
        List<SysMenu> allSysMenuList = this.getBaseMapper().selectList(sysMenuLambdaQueryWrapper);

        //去sys_role_menu查询所有role_id==roleId的菜单
        LambdaQueryWrapper<SysRoleMenu> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.getBaseMapper().selectList(wrapper);

        //获取所有menu_id

            List<Long> menuIdList = sysRoleMenuList.stream().map(item -> {
                return item.getMenuId();
            }).collect(Collectors.toList());

            //如果menuIdList中有allSysMenuList中的菜单id,说明该菜单被选中。
            allSysMenuList.stream().forEach(item->{
                if(menuIdList.contains(item.getId())){
                    item.setSelect(true);
                }else {
                    item.setSelect(false);
                }
            });

            //返回规定树形显示格式菜单
            List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
            return sysMenuList;

    }

    //给角色分配菜单
    //思路：把用户之前分配菜单数据删除  根据前端传过来的jackson对象assignMenuVo里的角色id查询角色所属的菜单，然后进行删除
    //思路：重新给用户分配菜单  根据前端传过来的jackson对象assignMenuVo里的菜单id列表menuIdList，封装进新的sysRoleMenu对象中
    // 然后对sys_role_menu表进行角色菜单批量添加
    @Override
    public void doAssign(AssignMenuVo assignMenuVo) {
        //查询所有与前端传过来的roleId相同的数据库SysRoleMenu对象，删除掉
        LambdaQueryWrapper<SysRoleMenu> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assignMenuVo.getRoleId());
        sysRoleMenuService.getBaseMapper().delete(wrapper);

        //重新分配菜单
        List<Long> menuIdList = assignMenuVo.getMenuIdList();
            for (Long menuId : menuIdList) {
                if(StringUtils.isEmpty(menuId)){
                    continue;
                }
                SysRoleMenu rolePermission = new SysRoleMenu();
                rolePermission.setRoleId(assignMenuVo.getRoleId());
                rolePermission.setMenuId(menuId);
                sysRoleMenuService.getBaseMapper().insert(rolePermission);
            }


    }

    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList=null;
        //1 判断当前用户是否是管理员  userId=1是管理员
        //1.1 如果是管理员，查询所有菜单列表
        if(userId.longValue()==1){
            LambdaQueryWrapper<SysMenu> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            sysMenuList = this.getBaseMapper().selectList(wrapper);
        }else {
            //1.2 如果不是管理员，根据userId查询可以操作菜单列表
            //多表关联查询： 用户角色关系表、角色菜单关系表、菜单表三表联查
            sysMenuList=this.getBaseMapper().findMenuListByUserId(userId);
        }

        //2 把查询出来数据列表构建成框架要求的路由数据结构
        //使用菜单操作工具类构建树形结构
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        //构建成框架要求的路由结构
        List<RouterVo> routerList=this.buildRouter(sysMenuTreeList);
        return routerList;

    }

    //构建成框架要求的路由结构
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        //创建List集合，存储最终数据
        List<RouterVo> routers=new ArrayList<>();
        //遍历菜单menus 一级菜单
        for (SysMenu menu : menus) {
            RouterVo router=new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(),menu.getIcon()));
            //下一层数据部分封装，如果menu有子节点，且为二级菜单
            List<SysMenu> children=menu.getChildren();
            if(menu.getType().intValue()==1){
                //加载出来下面的隐藏路由 即二级菜单 因为二级菜单的type=1
                //给二级菜单添加他的下一级的隐藏菜单，这部分隐藏菜单是有路由有页面的，如分配权限菜单
                List<SysMenu> hiddenMenuList = children.stream()
                        .filter(item -> !StringUtils.isEmpty(item.getComponent()))
                        .collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter=new RouterVo();
                    //true 隐藏路由 三级菜单有路由的情况。
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(),hiddenMenu.getIcon()));

                    routers.add(hiddenRouter);
                }

            }else {
                //如果menu的children子节点不为空，让其经常显示(打开页面就会自己展开，false则为折叠状态)，然后让自己点自己去查自己的子节点。。。
                if(!CollectionUtils.isEmpty(children)){
                    //有值就自动展开菜单
                    if (children.size()>0){
                        router.setAlwaysShow(true);
                    }
                    //递归
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        //判断是否为一级菜单，不是一级菜单的话可用，这里写的是二级菜单和三级菜单的路由路径获取
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        //1 判断当前用户是否是管理员  如果是管理员，查询所有按钮列表
        List<SysMenu> sysMenuList=null;
        if(userId.longValue()==1){
            LambdaQueryWrapper<SysMenu> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            sysMenuList = this.getBaseMapper().selectList(wrapper);
        }else {
            //2 如果不是管理员，根据userId查询可以操作按钮列表
            //多表关联查询： 用户角色关系表、角色菜单关系表、菜单表三表联查
            sysMenuList=this.getBaseMapper().findMenuListByUserId(userId);
        }
        //3 从查询出来的数据里面，获取可以操作按钮值得list集合，返回
        List<String> permsList = sysMenuList.stream()
                .filter(item->item.getType()==2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());

        return permsList;
    }
}
