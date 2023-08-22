package me.SingKwan.wechat.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.SneakyThrows;
import me.SingKwan.model.wechat.Menu;
import me.SingKwan.vo.wechat.MenuVo;
import me.SingKwan.wechat.mapper.MenuMapper;
import me.SingKwan.wechat.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-29
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    @Autowired
    private WxMpService wxMpService;

    @Override
    public List<MenuVo> findMenuInfo() {
//1 查询所有菜单list集合（包含一级菜单和二级菜单）
        List<Menu> menus = this.getBaseMapper().selectList(null);
//2 查询所有一级菜单 parent_id=0,返回一级菜单list集合
        LambdaQueryWrapper<Menu> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getParentId,0);
        List<Menu> FirstMenuList = this.getBaseMapper().selectList(wrapper);

//3 一级菜单list集合遍历，得到每一个一级菜单

        List<MenuVo> list=new ArrayList<>();
        for (Menu firstMenu : FirstMenuList) {
//得到一级菜单的id
            Long id = firstMenu.getId();
//4 获取每一个一级菜单里面所有的二级菜单 用一级菜单id 和 二级菜单parent_id比较，相等则说明二级菜单属于该一级菜单
//使用一级菜单的id查wechat_menu表，如果某个菜单的parent_id==id，说明此菜单是该一级菜单的子菜单
//写法一：
// LambdaQueryWrapper<Menu> lambdaQueryWrapper=new LambdaQueryWrapper<>();
// lambdaQueryWrapper.eq(Menu::getParentId,id);
// List<Menu> secondMenuList = this.getBaseMapper().selectList(lambdaQueryWrapper);

//写法二：
            List<Menu> secondMenuList = menus.stream().filter(menu ->
                    menu.getParentId().longValue() == firstMenu.getId()
            ).collect(Collectors.toList());

//5 把一级菜单里面所有二级菜单获取到，封装一级菜单children集合里面
//涉及到Menu和MenuVo转换
//先把二级菜单转换为MenuVo类型
//如果当次循环二级菜单为空，跳出当次循环
            List<MenuVo> children=new ArrayList<>();
            if (secondMenuList==null){
                continue;
            }
//把二级菜单转换为MenuVo类型，封装到List<MenuVo> secondMenuVoList中，待用
            for (Menu menu : secondMenuList) {
                MenuVo menuVo=new MenuVo();
                BeanUtils.copyProperties(menu,menuVo);
                children.add(menuVo);
            }
//把一级菜单转换为MenuVo类型，并设置子菜单为二级菜单List<MenuVo> children
            MenuVo menuVo=new MenuVo();
            BeanUtils.copyProperties(firstMenu,menuVo);
            menuVo.setChildren(children);
            list.add(menuVo);
        }
        return list;
    }

    //同步菜单
    @Override
    public void syncMenu() {
        //1 菜单数据查询出来，封装微信要求菜单格式
        List<MenuVo> menuVoList = this.findMenuInfo();
        //菜单
        JSONArray buttonList = new JSONArray();
        for(MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if(CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://oasystemclienttest.viphk.91tunnel.com/#"+oneMenuVo.getUrl());
            } else {
                JSONArray subButton = new JSONArray();
                for(MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if(twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://oasystemclienttest.viphk.91tunnel.com/#"+twoMenuVo.getUrl());
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        //菜单
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        try {
            //2 调用工具里面的方法实现菜单推送
            wxMpService.getMenuService().menuCreate(button.toJSONString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }

    }


    //删除菜单
    @Override
    public void removeMenu() {
        try {
            wxMpService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

}
