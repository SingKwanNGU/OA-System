package me.SingKwan.common.config.exception;

import lombok.Data;
import me.SingKwan.common.result.ResultCodeEnum;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.common.config.exception
 * @className: CustomizeException
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/4 21:11
 * @version: 1.0
 */
@Data
public class CustomizeException extends RuntimeException{


    private Integer code;
    private String msg;


    /**
     * 通过状态码和错误消息创建异常对象
     * @param code 状态码
     * @param msg 异常信息
     */
    public CustomizeException(Integer code,String msg){
        super(msg);
        this.code=code;
        this.msg=msg;
    }


    /**
     * 接收枚举类型对象
     * @param resultCodeEnum
     */
    public CustomizeException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMsg());
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMsg();
    }




    @Override
    public String toString() {
        return "CustomizeException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
