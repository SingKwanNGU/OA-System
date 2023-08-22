package me.SingKwan.common.result;

import io.swagger.models.auth.In;
import lombok.Data;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.common.result
 * @className: Result
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/2 22:54
 * @version: 1.0
 */
@Data
public class Result<T> {

    private Integer code;//状态码
    private String message;//返回信息
    private T data;//数据

    private Result(){}//私有无参构造，只能自己构造，外面不可使用此构造方法
                      //因此，需要提供静态方法给外面调用此类
    //成功（分有数据返回和无数据返回）
    public static<T> Result<T> ok(){
        return build(null,ResultCodeEnum.SUCCESS);
    }

    public static<T> Result<T> ok(T data){
        return build(data,ResultCodeEnum.SUCCESS);
    }


    //封装返回的是数据
    public static <T> Result<T> build(T body ,ResultCodeEnum resultCodeEnum){
        Result<T> result=new Result<>();
        //T body是携带的数据，可能有也可能没有
        if(body !=null){
            result.setData(body);
        }

        //状态码设置
        result.setCode(resultCodeEnum.getCode());
        //返回信息设置
        result.setMessage(resultCodeEnum.getMsg());
        return result;
    }



    //失败（分有数据返回和无数据返回）
    public static<T> Result<T> fail(){
        return build(null,ResultCodeEnum.FAIL);
    }

    public static<T> Result<T> fail(T data){
        return build(data,ResultCodeEnum.FAIL);
    }

    public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code){
        this.setCode(code);
        return this;
    }
}
