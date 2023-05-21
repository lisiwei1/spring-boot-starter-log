package com.log.core.log.customHandle;

import com.log.util.ApplicationUtil;

/**
 * 使用静态内部类方式获取自定义异常处理
 * @Author lsw
 * @Date 2023/10/17 11:10
 * @Description
 */
public class CustomExceptionHandlerSingleton {

    private CustomExceptionHandlerSingleton(){}

    private static class SingletonHolder{
        private static final CustomLogExceptionHandler handler = ApplicationUtil.getBean(CustomLogExceptionHandler.class);
    }

    public static CustomLogExceptionHandler getInstance(){
        return SingletonHolder.handler;
    }

}
