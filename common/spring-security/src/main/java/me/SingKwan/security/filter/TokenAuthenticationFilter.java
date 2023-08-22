package me.SingKwan.security.filter;

import com.alibaba.fastjson.JSON;
import me.SingKwan.common.jwt.JwtHelper;
import me.SingKwan.common.result.Result;
import me.SingKwan.common.result.ResultCodeEnum;
import me.SingKwan.common.utils.ResponseUtil;
import me.SingKwan.security.custom.LoginUserInfoHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.security.filter
 * @className: TokenAuthenticationFilter
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/14 20:17
 * @version: 1.0
 */
/**
 * <p>
 * 认证解析token过滤器
 * </p>
 */
//登录接口认证之前的过滤器  用来判断请求头里有没有token,有token则是登录，通过认证才可以继续进行登录
// 到下一个过滤器TokenLoginFilter进行登录
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private RedisTemplate redisTemplate;

    public TokenAuthenticationFilter(RedisTemplate redisTemplate){
        this.redisTemplate=redisTemplate;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        logger.info("uri:"+request.getRequestURI());
        //如果是登录接口，直接放行
        if("/admin/system/index/login".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        //这里是认证验证逻辑：从请求头里获取token，如果有token，就是登录，无token就不是
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if(null != authentication) {
            //如果是登录，把对象放进去 SecurityContextHolder的getContext()上下文中的Authentication对象中
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            //如果没有token，抛异常，报没有权限
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.PERMISSION));
        }

    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        // token置于header里
        String token = request.getHeader("token");
        logger.info("token:"+token);
        if (!StringUtils.isEmpty(token)) {
            String useruame = JwtHelper.getUsername(token);
            logger.info("useruame:"+useruame);
            if (!StringUtils.isEmpty(useruame)) {
                //把当前用户信息放到threadLocal中
                LoginUserInfoHelper.setUserId(JwtHelper.getUserId(token));
                LoginUserInfoHelper.setUsername(useruame);
                //通过userName从redis中获取权限数据
                String authString = (String) redisTemplate.opsForValue().get(useruame);
                //把redis获取的字符串权限数据转换成要求的集合类型Collection<? extends GrantedAuthority>
                if (StringUtils.hasText(authString)){
                    List<Map> mapList= JSON.parseArray(authString,Map.class);
                    System.out.println(mapList);
                    List<SimpleGrantedAuthority> authList=new ArrayList<>();
                    for (Map map : mapList) {
                        String authority = (String) map.get("authority");
                        authList.add(new SimpleGrantedAuthority(authority));
                    }
                    return new UsernamePasswordAuthenticationToken(useruame, null, authList);
                }else {
                    return new UsernamePasswordAuthenticationToken(useruame, null, new ArrayList<>());
                }
            }
        }
        return null;
    }
}
