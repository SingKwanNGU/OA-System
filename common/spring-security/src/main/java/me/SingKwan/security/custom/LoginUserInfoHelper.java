package me.SingKwan.security.custom;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.security.custom
 * @className: LoginUserInfoHelper
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/23 16:49
 * @version: 1.0
 */
public class LoginUserInfoHelper {
    private static ThreadLocal<Long> userId = new ThreadLocal<Long>();
    private static ThreadLocal<String> username = new ThreadLocal<String>();

    public static void setUserId(Long _userId) {
        userId.set(_userId);
    }
    public static Long getUserId() {
        return userId.get();
    }
    public static void removeUserId() {
        userId.remove();
    }
    public static void setUsername(String _username) {
        username.set(_username);
    }
    public static String getUsername() {
        return username.get();
    }
    public static void removeUsername() {
        username.remove();
    }
}
