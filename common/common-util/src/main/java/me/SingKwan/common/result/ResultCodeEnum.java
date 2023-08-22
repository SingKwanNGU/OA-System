package me.SingKwan.common.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"成功"),
    FAIL(201,"失败"),
    LOGIN_ERROR(208,"认证失败"),
    PERMISSION(209,"没有权限");

    private Integer code;
    private String msg;

    private ResultCodeEnum(Integer code,String msg){
        this.code=code;
        this.msg=msg;
    }

}
