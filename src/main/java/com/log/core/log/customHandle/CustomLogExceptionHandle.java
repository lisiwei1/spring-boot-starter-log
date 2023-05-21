package com.log.core.log.customHandle;

import com.log.core.log.vo.ExceptionVo;

/**
 * 用户自定义异常处理中响应值处理
 * 用于处理一些自定义异常时，不把异常写到throwable字段，而是返回自定义的响应结果
 * @Author lsw
 * @Date 2023/5/20 10:39
 */
public interface CustomLogExceptionHandle {

    ExceptionVo customExceptionHandler(Throwable throwable);

}
