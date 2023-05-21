package com.log.core.log.vo;

import java.util.Map;

/**
 * 用户自定义异常处理的出参
 * @Author lsw
 * @Date 2023/5/20 10:48
 */
public class ExceptionVo {

    // 是否抛出异常
    private Boolean isShowThrowable;

    // 返回的响应结果
    private Map<String, Object> responseParams;



    public ExceptionVo(Map<String, Object> responseParams, Boolean isShowThrowable) {
        this.isShowThrowable = isShowThrowable;
        this.responseParams = responseParams;
    }

    public static ExceptionVo empty(){
        return null;
    }

    public Boolean getShowThrowable() {
        return isShowThrowable;
    }

    public Map<String, Object> getResponseParams() {
        return responseParams;
    }
}
