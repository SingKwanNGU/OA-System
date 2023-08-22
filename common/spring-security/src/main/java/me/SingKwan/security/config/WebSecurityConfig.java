package me.SingKwan.security.config;

import me.SingKwan.security.custom.CustomMd5PasswordEncoder;
import me.SingKwan.security.filter.TokenAuthenticationFilter;
import me.SingKwan.security.filter.TokenLoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.EmbeddedLdapServerContextSourceFactoryBean;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * @projectName: oa-parent
 * @package: me.SingKwan.security.config
 * @className: WebSecurityConfig
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/13 19:57
 * @version: 1.0
 */
@Configuration
@EnableWebSecurity//@EnableWebSecurity是开启SpringSecurity的默认行为 过时写法
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomMd5PasswordEncoder customMd5PasswordEncoder;
//    @Bean
//    public static CustomMd5PasswordEncoder passwordEncoder(){
//        return new CustomMd5PasswordEncoder();
//    }

    @Autowired//这里直接用spring库中的Bean
    private AuthenticationConfiguration authenticationConfiguration;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 这是配置的关键，决定哪些接口开启防护，哪些接口绕过防护
        http
                //关闭csrf跨站请求伪造
                .csrf().disable()
                // 开启跨域以便前端调用接口
                .cors().and()
                .authorizeRequests()
                // 指定某些接口不需要通过验证即可访问。登陆接口肯定是不需要认证的
                .antMatchers("/admin/system/index/login").permitAll()
                // 这里意思是其它所有接口需要认证才能访问
                .anyRequest().authenticated()
                .and()
                //TokenAuthenticationFilter放到UsernamePasswordAuthenticationFilter的前面，这样做就是为了除了登录的时候去查询数据库外，其他时候都用token进行认证。
                .addFilterBefore(new TokenAuthenticationFilter(redisTemplate), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new TokenLoginFilter(authenticationManager(authenticationConfiguration),redisTemplate));

        //禁用session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }


//    旧写法
//    @Bean
//    @Override
//    protected AuthenticationManager authenticationManager() throws Exception {
//        return super.authenticationManager();
//    }




    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }





//    @Bean
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        // 指定UserDetailService和加密器
//        auth.userDetailsService(userDetailsService)
//                .passwordEncoder(customMd5PasswordEncoder);
//    }



    /**
     * 配置哪些请求不拦截
     * 排除swagger相关请求
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring().antMatchers("/admin/modeler/**","/diagram-viewer/**","/editor-app/**","/*.html",
                "/admin/processImage/**",
                "/admin/wechat/authorize","/admin/wechat/userInfo","/admin/wechat/bindPhone",
                "/favicon.ico","/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**", "/doc.html");
    }

    //以下是旧写法
    /**
     * 配置哪些请求不拦截
     * 排除swagger相关请求
     * @param web
     * @throws Exception
     */
//    @Overide
//    public void WebSecurityCUstomizer (WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/favicon.ico","/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**", "/doc.html");
//    }
}
