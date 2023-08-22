package me.SingKwan.auth.activiti;

import org.springframework.stereotype.Component;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.auth.activiti
 * @className: UserBean
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/18 23:09
 * @version: 1.0
 */
@Component
public class UserBean {

    public String getUserName(int id) {
        if(id == 1) {
            return "lilei";
        }
        if(id == 2) {
            return "hanmeimei";
        }
        return "admin";
    }
}
