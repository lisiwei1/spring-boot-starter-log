package com.log.core.annotation;

import java.lang.annotation.*;

/**
 * 日志注解
 * @Author lsw
 * @Date 2023/5/6 13:04
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    String value() default "";//接口名字

    boolean skipLog() default false;//不记录整体日志

    boolean skipReq() default false;//不记录请求日志

    boolean skipRsp() default false;//不记录响应日志

    boolean skipSql() default false;//跳过sql

}
