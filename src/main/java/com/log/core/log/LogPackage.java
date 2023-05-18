package com.log.core.log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.log.core.annotation.LogOperation;
import com.log.util.ExceptionUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @Author lsw
 * @Date 2023/5/6 13:16
 */
public class LogPackage {

    private Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private Gson reqRspGson = new GsonBuilder().disableHtmlEscaping().create();

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    private long startTime;

    private String traceToken;

    private Throwable throwable;

    private Map<String, Object> variables = Maps.newHashMap();

    private List<String> sqls = Lists.newArrayList();

    private LogOperation logOperation;

    // 执行顺序，第一次执行是1，每次递增1
    private Integer currentOrder;

    // 用于记录服务节点之间的调用顺序
    private List<Host> hosts =Lists.newArrayList();

    public LogPackage(String traceToken, Integer currentOrder) {
        this.traceToken = traceToken;
        this.startTime = System.currentTimeMillis();
        this.currentOrder = currentOrder;
    }

    public void putVariable(String key, Object value) {
        add(key, value, false);
    }

    public void appendVariable(String key, Object value) {
        add(key, value, true);
    }

    public long getCurrentConsumeTime(long currentTime) {
        return currentTime - startTime;
    }

    /**
     *
     * @param key
     * @param value
     * @param isAppend  true表示添加到list，false表示直接覆盖
     */
    private void add(String key, Object value, boolean isAppend) {
        if (logOperation != null) {
            // 判断是否跳过请求日志或响应日志
            if (LogVariableKey.REQUEST_PARAMS.equals(key) && logOperation.skipReq()) {
                return;
            }
            if (LogVariableKey.RESPONSE_PARAMS.equals(key) && logOperation.skipRsp()) {
                return;
            }
        }
        if (isAppend) {
            Object currentValue = variables.get(key);
            if (currentValue != null) {
                if (currentValue instanceof List) {
                    List currentValueList = (List) currentValue;
                    currentValueList.add(value);
                } else {
                    variables.put(key, Lists.newArrayList(currentValue, value));
                }
            } else {
                variables.put(key, Lists.newArrayList(value));
            }
        } else {
            variables.put(key, value);
        }
    }

    public void addSQL(String sql) {
        sqls.add(sql);
    }

    // 打印日志
    public void print() {
        Map<String, Object> printObject = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof Number || value instanceof CharSequence||value instanceof List)) {
                printObject.put(entry.getKey(), reqRspGson.toJson(value));
            } else {
                printObject.put(entry.getKey(), value);
            }
        }
        boolean isShowThrowable = true;

        // TODO 此处处理自定义的业务异常,应该要设置code、message、responseParams，而不是直接抛异常
        //  参考下面例子的BusinessException，继承于RuntimeException，用于一些业务上需要抛出的业务异常
        // if (throwable instanceof BusinessException) {
        //     isShowThrowable = false;
        //     BusinessException businessException = (BusinessException) throwable;
        //     Map<String, Object> responseParams = Maps.newHashMap();
        //     responseParams.put(LogVariableKey.CODE, businessException.getCode());
        //     responseParams.put(LogVariableKey.MESSAGE, businessException.getMessage());
        //     printObject.put(LogVariableKey.RESPONSE_PARAMS, reqRspGson.toJson(responseParams));
        // }


        if (isShowThrowable) {
            String throwableToString = ExceptionUtil.throwableToString(throwable);
            if (throwableToString != null) {
                printObject.put(LogVariableKey.THROWABLE, throwableToString);
            }
        }

        // TODO sql语句应该也要记录
        printObject.put(LogVariableKey.SQLS, sqls);

        printObject.put(LogVariableKey.REQUEST_TIME, DateFormatUtils.format(startTime, DATE_FORMAT_PATTERN));
        long nowTime = System.currentTimeMillis();
        printObject.put(LogVariableKey.RESPONSE_TIME, DateFormatUtils.format(nowTime, DATE_FORMAT_PATTERN));
        printObject.put(LogVariableKey.CONSUME_TIME, getCurrentConsumeTime(nowTime));
        printObject.put(LogVariableKey.TRACE_TOKEN, traceToken);
        printObject.put(LogVariableKey.CURRENT_ORDER, currentOrder);
        LoggerFactory.getLogger("LogPackage").info(gson.toJson(printObject));
    }

    public LogOperation getLogOperation() {
        return logOperation;
    }

    public void setLogOperation(LogOperation logOperation) {
        this.logOperation = logOperation;
    }


    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getTraceToken() {
        return traceToken;
    }

    public void setTraceToken(String traceToken) {
        this.traceToken = traceToken;
    }

    public Integer getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(Integer currentOrder) {
        this.currentOrder = currentOrder;
    }
}
