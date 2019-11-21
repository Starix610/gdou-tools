package com.starix.scorequery.exception;


import com.starix.scorequery.response.CommonResult;

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException {

    private CommonResult commonResult;

    public CustomException(CommonResult commonResult) {
        this.commonResult = commonResult;
    }

    public CommonResult getCommonResult() {
        return commonResult;
    }

    public void setCommonResult(CommonResult commonResult) {
        this.commonResult = commonResult;
    }
}
