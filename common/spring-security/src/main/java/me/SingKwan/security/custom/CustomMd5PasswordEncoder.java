package me.SingKwan.security.custom;

import me.SingKwan.common.utils.MD5;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.security.custom
 * @className: CustomMd5PasswordEncoder
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/13 22:26
 * @version: 1.0
 */
@Component
public class CustomMd5PasswordEncoder implements PasswordEncoder {

    public String encode(CharSequence rawPassword) {
        return MD5.encrypt(rawPassword.toString());
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(MD5.encrypt(rawPassword.toString()));
    }
}