package com.log.core.log;

import com.log.core.annotation.LogOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * @Author lsw
 * @Date 2023/5/6 13:20
 */
public class LogPackageHolder {

    private static final ThreadLocal<LogPackage> logPackageThreadLocal = new ThreadLocal<>();

    /**
     * 初始化，获取请求是否带有tracetoken，若无则添加上,调用其他服务节点需要header加上tracetoken
     * 获取请求是否带有currentorder(调用顺序)，若无则添加上
     * @return
     */
    public static LogPackage init() {
        String traceToken = null;
        Integer currentOrder = null;
        String currentOrderString = null;
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null && ((ServletRequestAttributes) attributes).getRequest() != null) {
            traceToken = ((ServletRequestAttributes) attributes).getRequest().getHeader(LogVariableKey.TRACE_TOKEN);
            currentOrderString = ((ServletRequestAttributes) attributes).getRequest().getHeader(LogVariableKey.CURRENT_ORDER);
            if (StringUtils.isNotBlank(currentOrderString)){
                currentOrder = Integer.valueOf(currentOrderString) + 1;
            }
        }
        if (StringUtils.isBlank(traceToken)) {
            traceToken = UUID.randomUUID().toString();
        }
        if (StringUtils.isBlank(currentOrderString)){
            currentOrder = 1;
        }
        LogPackage logPackage = new LogPackage(traceToken, currentOrder);
        logPackageThreadLocal.set(logPackage);
        return logPackageThreadLocal.get();
    }

    public static void addSQL(String sql) {
        LogPackage logPackage = getLogPackage();
        if (logPackage != null) {
            LogOperation logOperation=logPackage.getLogOperation();
            if(logOperation!=null&&logOperation.skipSql()) {
                return;
            }
            logPackage.addSQL(sql);
        }
    }

    protected static LogPackage getLogPackage() {
        return logPackageThreadLocal.get();
    }

    public static void clear() {
        logPackageThreadLocal.remove();
    }

    public static final String getCurrentTraceToken() {
        LogPackage logPackage = getLogPackage();
        if (logPackage != null) {
            return logPackage.getTraceToken();
        }
        return null;
    }
    public static final void setCurrentTraceToken(String traceToken) {
        LogPackage logPackage = getLogPackage();
        if (logPackage != null) {
            logPackage.setTraceToken(traceToken);
        }
    }
    public static final Integer getCurrentOrder() {
        LogPackage logPackage = getLogPackage();
        if (logPackage != null) {
            return logPackage.getCurrentOrder();
        }
        return null;
    }
    public static final void setCurrentOrder(Integer currentOrder) {
        LogPackage logPackage = getLogPackage();
        if (logPackage != null) {
            logPackage.setCurrentOrder(currentOrder);
        }
    }

    /**
     * 通过该方法可以记录业务中产生异常的原始异常信息或其他信息,当前线程未结束时，
     * 每次调用errMsg会拼接到当前api调用日志的errMsg的末尾
     * @param errMsg 要记录的信息
     */
    public static void logErrorMsg(String errMsg) {
        LogPackage logPackage = getLogPackage();
        if (logPackage != null) {
            logPackage.appendVariable(LogVariableKey.ERR_MSG, errMsg);
        }
    }
}
