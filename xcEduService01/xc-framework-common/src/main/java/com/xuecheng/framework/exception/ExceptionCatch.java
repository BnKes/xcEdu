package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice  //控制器增强
public class ExceptionCatch {
    //记录ExceptionCatch类日志
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    //使用集合EXCEPTIONS存放异常类型和错误代码映射，ImmutableMap的特点是被创建后不可改变，并且线程安全
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //使用builder来构建一个异常类型的错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder =  ImmutableMap.builder();

    //捕获Exception异常
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseResult exception(Exception e){
        //记录日志
        LOGGER.error("catch exception : {}\r\nexception: ",e.getMessage(),e);
        if(EXCEPTIONS==null){
            EXCEPTIONS = builder.build();
        }
        final ResultCode resultCode =EXCEPTIONS.get(e.getClass()); //从map中取出异常
            final ResponseResult responseResult;
            if(resultCode != null){
                responseResult = new ResponseResult(resultCode);   //如果map中存在该异常，返回异常结果
            }else{
                responseResult = new ResponseResult(CommonCode.SERVER_ERROR);   //不存在，返回服务器错误
            }
        return  responseResult;
    }

    //捕获CustomException异常
    @ExceptionHandler(CustomException.class)
    @ResponseBody   //将结果转化为json，否则前端无法解析
    public ResponseResult customException(CustomException e){
        //记录日志
        LOGGER.error("catch exception : {}\r\nexception: ",e.getMessage(),e);
        ResultCode resultCode = e.getResultCode();
        ResponseResult responseResult = new ResponseResult(resultCode);
        return responseResult;
    }

    static {
        //这里添加一些基础的异常类型判断
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
    }
}
