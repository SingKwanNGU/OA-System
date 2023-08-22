package me.SingKwan.common.config.exception;

import me.SingKwan.common.result.Result;
import me.SingKwan.common.result.ResultCodeEnum;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @projectName: oa-parent
 * @package: me.SingKwan.common.config.exception
 * @className: GlobalExceptionHandler
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/4 20:33
 * @version: 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    //全局异常执行的方法
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail().message("执行全局异常处理。。。。");
    }


    //特定异常执行的方法  --需要指定特定的异常类型
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e){
        e.printStackTrace();
        return Result.fail().message("执行特定异常处理。。。。");
    }


    /**
     * spring security异常
     * @param e 无权限访问异常
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e) throws AccessDeniedException {
        return Result.build(null, ResultCodeEnum.PERMISSION);
    }


    //自定义异常执行的方法  --需要指定自定义的异常类型
    @ExceptionHandler(CustomizeException.class)
    @ResponseBody
    public Result error(CustomizeException e){
        e.printStackTrace();
        return Result.fail().code(e.getCode()).message(e.getMsg());
    }

}
