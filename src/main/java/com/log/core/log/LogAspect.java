package com.log.core.log;

import com.google.common.collect.Maps;
import com.log.core.annotation.LogOperation;
import com.log.core.async.AsyncComp;
import com.log.core.log.customHandle.MethodDescConfigurer;
import com.log.util.IpUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author lsw
 * @Date 2023/5/6 13:08
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    //设置切点
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void logRequestMapping() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void logGetMapping() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void logPostMapping() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void logDeleteMapping() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void logPutMapping() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void logPatchMapping() {}
    @Pointcut("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void logScheduled() {}
    @Pointcut("@annotation(com.log.core.annotation.LogOperation)")
    public void logLogOperation() {}
    private static final String ASPECT_POINT = "logRequestMapping()||logGetMapping()||logPostMapping()||logDeleteMapping()||logPutMapping()||logPatchMapping()||logScheduled()||logLogOperation()";


    // 从配置文件上读取当前服务节点的名称，默认default-server-01
    @Value("${log.serverName:default-server-01}")
    private String serverName;


    @Autowired
    private AsyncComp asyncComp;

    @Autowired
    @Qualifier("logTaskExecutor")
    private ThreadPoolExecutor logTaskExecutor;

    @Autowired(required = false)
    private MethodDescConfigurer methodDescConfigurer;

    @Value("${log.excludeClassNames:}")
    private String exclude_class_names_str;

    @PostConstruct
    public void init(){
        logger.info(" ------------------- ");
        logger.info("日志线程池相关参数：");
        logger.info("核心线程数：" + logTaskExecutor.getCorePoolSize());
        logger.info("最大线程数：" + logTaskExecutor.getMaximumPoolSize());
        logger.info("线程空闲时间：" + logTaskExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS) + " 毫秒");
        logger.info("工作队列类型：" + logTaskExecutor.getQueue().getClass().getName());
        logger.info("线程工厂类型：" + logTaskExecutor.getThreadFactory().getClass().getName());
        RejectedExecutionHandler rejectedExecutionHandler = logTaskExecutor.getRejectedExecutionHandler();
        if (rejectedExecutionHandler != null) {
            logger.info("拒绝策略类型：" + rejectedExecutionHandler.getClass().getName());
        } else {
            logger.info("拒绝策略：无");
        }
        logger.info(" ------------------- ");
    }


    /**
     * 环绕
     * @param jp
     * @return
     * @throws Throwable
     */
    @Around(ASPECT_POINT)
    public Object doAround(ProceedingJoinPoint jp) throws Throwable {
        // 执行前方法，主要记录各种日志数据
        doBefore(jp);
        // 执行方法
        try {
            Object result = jp.proceed();
            // 记录出参
            LogPackage logPackage = LogPackageHolder.getLogPackage();
            if (logPackage != null) {
                logPackage.putVariable(LogVariableKey.RESPONSE_PARAMS, result);
            }
            return result;
        } catch (Throwable e) {
            LogPackage logPackage = LogPackageHolder.getLogPackage();
            if (logPackage != null) {
                logPackage.setThrowable(e);
            }
            throw e;
        } finally {
            LogPackage logPackage = LogPackageHolder.getLogPackage();
            if (logPackage != null) {
                // asyncComp.execute(new Runnable() {
                //     @Override
                //     public void run() {
                //         logPackage.print();
                //     }
                // });

                logTaskExecutor.execute(() -> logPackage.print());
                // TODO 处理任务被拒绝的情况
            }
            LogPackageHolder.clear();
        }
    }

    /**
     * 执行前方法，主要记录各种日志数据
     * @param jp
     */
    private void doBefore(ProceedingJoinPoint jp) throws Throwable{
        // 跳过指定的class
        String className = jp.getSignature().getDeclaringTypeName();
        // if (className.matches(LogConfig.getExcludeClassNames())) {
        if (isExcluded(className, exclude_class_names_str)) {
            return;
        }

        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        // 判断是否设置为跳过日志记录
        LogOperation logOperation = method.getAnnotation(LogOperation.class);
        if (logOperation != null && logOperation.skipLog()){
            return;
        }
        // 初始日志并解析相关数据
        LogPackageHolder.init();
        LogPackage logPackage = LogPackageHolder.getLogPackage();
        // 获取指定注解信息
        getAnnotationText(logPackage, method);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logPackage.putVariable(LogVariableKey.URL, request.getRequestURL().toString());
            logPackage.putVariable(LogVariableKey.HTTP_METHOD, request.getMethod());
            logPackage.putVariable(LogVariableKey.REQUESTIP, request.getRemoteAddr());
        }
        logPackage.putVariable(LogVariableKey.CLASS_NAME, className);
        logPackage.putVariable(LogVariableKey.CLASS_METHOD, jp.getSignature().getName());
        logPackage.putVariable(LogVariableKey.REQUEST_PARAMS, getReqParams(jp));
        logPackage.putVariable(LogVariableKey.SERVER_NAME, serverName);
        logPackage.putVariable(LogVariableKey.HOST_NAME, IpUtils.getHostName());
        logPackage.putVariable(LogVariableKey.HOST_IP, IpUtils.getHostIp());
    }

    /**
     * 执行后方法，记录执行结果
     * @param result
     * @return
     */
    private Object doAfter(Object result){
        return result;
    }


    /**
     * 判断包含指定的类名或者在指定的包内，是则返回true
     * @param className 当前类名
     * @param excludeClassNames 排除的包名或者类名，多个包名或类名用逗号隔开
     * @return
     */
    private boolean isExcluded(String className, String excludeClassNames) {
        if(StringUtils.isBlank(excludeClassNames)){
            return false;
        }
        if (className.startsWith(excludeClassNames)) {
            return true;
        } else {
            String[] excludePackages = excludeClassNames.split(",");
            for (String packageName : excludePackages) {
                if (className.startsWith(packageName.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *  获取指定注解信息
     */
    private void getAnnotationText(LogPackage logPackage, Method method){
        // 读取用户自定义返回的方法描述
        if (methodDescConfigurer !=null){
            logPackage.putVariable(LogVariableKey.DESC, methodDescConfigurer.getMethodDesc(method));
        }

        // 读取LogOperation注解信息，此注解信息优先级最高，会覆盖其他注解信息
        LogOperation logOperation = method.getAnnotation(LogOperation.class);
        if (logOperation != null) {
            logPackage.setLogOperation(logOperation);
            if (StringUtils.isNotEmpty(logOperation.value())) {
                logPackage.putVariable(LogVariableKey.DESC, logOperation.value());
            }
        }
    }

    /**
     * 获取请求入参
     * @param jp
     * @return
     */
    private Map<String, Object> getReqParams(JoinPoint jp) {
        Map<String, Object> reqParam = Maps.newHashMap();
        String[] parameterNames = ((MethodSignature) jp.getSignature()).getParameterNames();
        Object[] args = jp.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            Object arg = args[i];
            if (arg instanceof HttpServletRequest) {
                reqParam.put(parameterName, ((HttpServletRequest) arg).getParameterMap());
            } else {
                reqParam.put(parameterName, arg);
            }
        }
        return reqParam;
    }

}
