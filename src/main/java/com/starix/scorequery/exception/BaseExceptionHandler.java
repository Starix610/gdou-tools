package com.starix.scorequery.exception;

import com.starix.scorequery.response.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;


@ControllerAdvice
public class BaseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(BaseExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    // @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult handlerAny(Exception e){
        //如果是自定义的业务异常则返回业务异常的详情
        if (e instanceof CustomException){
            CustomException customException = (CustomException) e;
            CommonResult result = customException.getCommonResult();
            logger.error("出现业务异常,code:{},msg:{}",result.getCode(), result.getMessage());
            return result;
        }else {
            // 系统异常
            logger.error("系统内部异常:[{}]",e.getMessage(), e);
            return CommonResult.failed("服务器似乎遇到了一些问题，请稍后重试");
        }
    }

    //处理"请求方法不支持"异常
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public CommonResult handlerMethodNotSupported(HttpServletRequest request, Exception e){
        logger.error("不支持的请求方法:[{}]",request.getMethod());
        return CommonResult.failed("不支持的请求方法:"+request.getMethod());
    }


}
