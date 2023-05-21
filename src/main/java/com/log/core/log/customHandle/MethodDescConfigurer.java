package com.log.core.log.customHandle;

import java.lang.reflect.Method;

/**
 * 使用用户返回的方法描述
 * @Author lsw
 * @Date 2023/5/18 16:12
 */
public interface MethodDescConfigurer {

    String getMethodDesc(Method method);

}
