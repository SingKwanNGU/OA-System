package me.SingKwan.auth.utils;

import me.SingKwan.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.utils
 * @className: MenuHelper
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/9 19:41
 * @version: 1.0
 */
public class MenuHelper {

    /**
     * 使用递归方法构建树形菜单
     * @param sysMenuList
     * @return
     */
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList){
        //创建List集合，用于接收最终数据
        List<SysMenu> trees=new ArrayList<>();
        //把所有菜单数据进行遍历
        for (SysMenu sysMenu : sysMenuList) {
            //递归入口进入 parentId=0就是递归开始的入口，直到没有子节点停止递归
            if(sysMenu.getParentId().longValue()==0){
                //调用getChildren方法获取一级菜单的下层子节点,添加所有下层子节点进trees集合中
                trees.add(findChildren(sysMenu,sysMenuList));
            }

        }
        return trees;
    }

    //获取下层子节点方法 此时传入的是一级菜单
    public static SysMenu findChildren(SysMenu sysMenu,List<SysMenu> sysMenuList){
        //给一级菜单设置新的集合来封装下级菜单
        sysMenu.setChildren(new ArrayList<>());
        //遍历所有菜单数据，判断一级菜单id和某个菜单的parentId的对应关系
        //如果遍历到的当前菜单menu的parentId == 一级菜单sysMenu的id,说明当前菜单menu是一级菜单sysMenu的下级菜单（子节点）
        //也说明一级菜单sysMenu有子节点，子节点下是否有子节点？故继续调用getchildren方法获取下层子节点
        for (SysMenu menu : sysMenuList) {
            if(sysMenu.getId().longValue()==menu.getParentId().longValue()){
                if(sysMenu.getChildren()==null){
                    sysMenu.setChildren(new ArrayList<>());
                }
                //再次获取下级子节点，如果有的话
                sysMenu.getChildren().add(findChildren(menu,sysMenuList));
            }
        }
        return sysMenu;

    }



}
