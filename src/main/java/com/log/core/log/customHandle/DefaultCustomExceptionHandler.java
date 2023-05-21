package com.log.core.log.customHandle;

import com.log.core.log.vo.ExceptionVo;

/**
 * @Author lsw
 * @Date 2023/10/16 21:30
 * @Description
 */
public class DefaultCustomExceptionHandler implements CustomLogExceptionHandler{
    @Override
    public ExceptionVo handleException(Throwable throwable) {
        // 默认为null
        return null;
    }
}
