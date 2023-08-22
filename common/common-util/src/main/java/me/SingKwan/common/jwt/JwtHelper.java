package me.SingKwan.common.jwt;
import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;
/**
 * @projectName: oa-parent
 * @package: me.SingKwan.common.jwt
 * @className: JwtHelper JWT工具类
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/10 21:16
 * @version: 1.0
 */
public class JwtHelper {

    //token有效时间
    private static long tokenExpiration = 365 * 24 * 60 * 60 * 1000;
    //token秘钥
    private static String tokenSignKey = "123456";

    //生成token
    public static String createToken(Long userId, String username) {
        String token = Jwts.builder()
                //设置token分类
                .setSubject("AUTH-USER")
                //设置token有效时长
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                //设置主体部分
                .claim("userId", userId)
                .claim("username", username)
                //签名算法使用HS512,token秘钥
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                //压缩方法 GZIP
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    //从token获取用户Id
    public static Long getUserId(String token) {
        try {
            if (StringUtils.isEmpty(token)) return null;
            //需要解析token且需要使用token秘钥tokenSignKey获取主体部分
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            //获取主体部分对象，从主体部分继续获取主体里的信息
            Claims claims = claimsJws.getBody();
            Integer userId = (Integer) claims.get("userId");
            return userId.longValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //从token获取用户名称
    public static String getUsername(String token) {
        try {
            if (StringUtils.isEmpty(token)) return "";

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            return (String) claims.get("username");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String token = JwtHelper.createToken(7L, "zhangsan");
        System.out.println(token);
        System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUsername(token));
    }
}