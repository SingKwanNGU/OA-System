package me.SingKwan.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.SingKwan.auth.service.SysMenuService;
import me.SingKwan.auth.service.SysUserService;
import me.SingKwan.common.config.exception.CustomizeException;
import me.SingKwan.common.jwt.JwtHelper;
import me.SingKwan.common.result.Result;
import me.SingKwan.common.utils.MD5;
import me.SingKwan.model.system.SysMenu;
import me.SingKwan.model.system.SysUser;
import me.SingKwan.vo.system.LoginVo;
import me.SingKwan.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.controller
 * @className: IndexController
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/5 21:10
 * @version: 1.0
 */
@Api(tags = "登录接口")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;

    //login
    @ApiOperation("登录login")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo){
//        {"code":20000,"data":{"token":"admin-token"}}
//        Map<String, Object> map=new HashMap<>();
//        map.put("data","admin-token");
//        return Result.ok(map);
        //1 获取输入用户名和密码
        String username = loginVo.getUsername();
        //2 根据用户名查询数据库
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);
        SysUser user = sysUserService.getOne(wrapper);
        //3 用户信息是否存在
        if(user ==null ){
            throw new CustomizeException(400,"用户不存在");
        }
        //4 判断密码
        //由于密码不可能存储的是明文，用的MD5加密算法，特点是不可逆
        //所以只能重新从前端传递来的loginVo对象里获取用户输入的密码，再次进行MD5算法加密，
        //如果两个密码相同，则证明密码正确
        String dataPassword = user.getPassword();
        String loginVoPassword = loginVo.getPassword();
        String encrypt = MD5.encrypt(loginVoPassword);
        if (!dataPassword.equals(encrypt)){
            throw new CustomizeException(400,"密码错误");
        }

        //5 判断用户是否被禁用 1可用，0禁用
        if(user.getStatus().intValue()==0){
            throw new CustomizeException(400,"当前用户不可用");
        }


        //6 使用jwt根据用户id和用户名称生成token字符串
        String token = JwtHelper.createToken(user.getId(), user.getUsername());
        //7 返回
        Map<String,Object> map=new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }


    //info 获取用户信息
    @ApiOperation("获取用户信息info")
    @GetMapping("/info")
    public Result info(HttpServletRequest request){
        //1 从请求头里获取用户信息(从请求头里获取token字符串)
        String token = request.getHeader("token");
        //2 从token字符串获取userId用户id和userName用户名称
        Long userId =JwtHelper.getUserId(token);
        String username = JwtHelper.getUsername(token);
        //3 根据用户id查询数据库，把用户信息获取出来
        SysUser user = sysUserService.getById(userId);
        //4 根据用户id获取用户可以操作菜单列表
        //查询数据库动态构建路由结构，进行显示
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);
        //5 根据用户id获取用户可以操作按钮列表
        List<String> permsList=sysMenuService.findUserPermsByUserId(userId);
        //6 返回响应的数据
        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",user.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        map.put("routers",routerList);
        map.put("buttons",permsList);
        return Result.ok(map);
    }


    //logout
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }
}
