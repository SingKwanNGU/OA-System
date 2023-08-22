package me.SingKwan.security.filter;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.SingKwan.common.jwt.JwtHelper;
import me.SingKwan.common.result.Result;
import me.SingKwan.common.result.ResultCodeEnum;
import me.SingKwan.common.utils.ResponseUtil;
import me.SingKwan.security.custom.CustomUser;
import me.SingKwan.vo.system.LoginVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.security.filter
 * @className: TokenLoginFilter
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/14 18:05
 * @version: 1.0
 */
/**
 * <p>
 * 登录过滤器，继承UsernamePasswordAuthenticationFilter，对用户名密码进行登录校验
 * </p>
 */
//登录接口认证的过滤器 登录认证通过则进行登录，不通过则报错
public class TokenLoginFilter extends UsernamePasswordAuthenticationFilter {

    private RedisTemplate redisTemplate;
    public TokenLoginFilter(AuthenticationManager authenticationManager,RedisTemplate redisTemplate){
        this.setAuthenticationManager(authenticationManager);
        this.setPostOnly(false);
        //指定登录接口及提交方式，可以指定任意路径
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/admin/system/index/login","POST"));
        this.redisTemplate=redisTemplate;
    }
    //登录认证
    //获取输入的用户名和密码,调用方法认证
    /**
     * 登录认证
     * @param request
     * @param response
     * @return Authentication 认证
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)throws AuthenticationException {
        try {
            //获取用户信息
            LoginVo loginVo=new ObjectMapper().readValue(request.getInputStream(),LoginVo.class);
            //封装对象
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(loginVo.getUsername(), loginVo.getPassword());
            //调用方法
            return  this.getAuthenticationManager().authenticate(authenticationToken);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //认证成功调用方法
    /**
     * 登录成功
     * @param request
     * @param response
     * @param chain 过滤链
     * @param authResult 认证结果
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        //获取当前用户
        CustomUser customUser = (CustomUser) authResult.getPrincipal();
        //生成token
        String token=JwtHelper.createToken(customUser.getSysUser().getId(), customUser.getSysUser().getUsername());
        //获取当前用户的权限数据，放到redis中，约定 key:userName value:权限数据集合
        redisTemplate.opsForValue().set(customUser.getSysUser().getUsername(),
                JSON.toJSONString(customUser.getAuthorities()));
        //返回
        Map<String,Object> map =new HashMap<>();
        map.put("token",token);
        ResponseUtil.out(response, Result.ok(map));
    }

    //认证失败调用方法
    /**
     * 登录失败
     * @param request
     * @param response
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationException e) throws IOException, ServletException {
        ResponseUtil.out(response,Result.build(null, ResultCodeEnum.LOGIN_ERROR));

    }
}
