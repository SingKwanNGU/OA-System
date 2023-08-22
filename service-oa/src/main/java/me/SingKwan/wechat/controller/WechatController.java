package me.SingKwan.wechat.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import me.SingKwan.auth.service.SysUserService;
import me.SingKwan.common.jwt.JwtHelper;
import me.SingKwan.common.result.Result;
import me.SingKwan.model.system.SysUser;
import me.SingKwan.vo.wechat.BindPhoneVo;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.process.controller.api
 * @className: WechatController
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/30 23:01
 * @version: 1.0
 */
@CrossOrigin
@Controller
@RequestMapping("/admin/wechat")
public class WechatController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private WxMpService wxMpService;

    @Value("${wechat.userInfoUrl}")
    private String userInfoUrl;

    @GetMapping("/authorize")
    public String authorize(@RequestParam("returnUrl")String returnUrl,
                            HttpServletRequest request){
        //buildAuthorizationUrl的三个参数
        //第一个参数：授权路径,在那个路径获取微信信息
        //第二个参数：固定值，授权类型  WxConsts.OAuth2Scope.SNSAPI_USERINFO
        //第三个参数：授权成功之后的，跳转路径， ‘guiguoa’转换成‘#’

        String redirectUrl= null;
        try {
            redirectUrl = wxMpService.getOAuth2Service()
                    .buildAuthorizationUrl(userInfoUrl,
                            WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                            URLEncoder.encode(returnUrl.replace("guguoa","#"),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return "redirect:" +redirectUrl;
    }

    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("code") String code,
                           @RequestParam("state") String returnUrl) throws Exception {
        //获取accessToken
        WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
        //使用accessToken获取OpenId
        String openId = accessToken.getOpenId();
        System.out.println(openId);
        //获取微信用户信息
        WxOAuth2UserInfo wxMpUser = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);
        System.out.println("微信用户信息："+JSON.toJSONString(wxMpUser));

        //根据openId查询用户表
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getOpenId,openId);
        SysUser sysUser = sysUserService.getOne(wrapper);
        String token="";
        //null != sysUser 说明已经绑定，反之为建立账号绑定，去页面建立账号绑定
        if (sysUser!=null){
            token= JwtHelper.createToken(sysUser.getId(),sysUser.getUsername());
        }
        //判断returnUrl是否不含字符串？ ==-1说明不含？  含有则==？的在字符串的起始位置
        if(returnUrl.indexOf("?") == -1) {
            return "redirect:" + returnUrl + "?token=" + token + "&openId=" + openId;
        } else {
            return "redirect:" + returnUrl + "&token=" + token + "&openId=" + openId;
        }

    }


    @ApiOperation(value = "微信账号绑定手机")
    @PostMapping("bindPhone")
    @ResponseBody
    public Result bindPhone(@RequestBody BindPhoneVo bindPhoneVo) {
        //1 根据手机号查询数据坤
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone,bindPhoneVo.getPhone());
        SysUser sysUser = sysUserService.getOne(wrapper);
        //2 如果存在，更新记录 openId
        if (sysUser!=null){
            sysUser.setOpenId(bindPhoneVo.getOpenId());
            sysUserService.updateById(sysUser);

            String token=JwtHelper.createToken(sysUser.getId(),sysUser.getUsername());
            return Result.ok(token);
        }else {
            return Result.fail("电话不存在，请联系管理员修改");
        }

    }
}
