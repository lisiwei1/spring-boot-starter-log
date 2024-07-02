package com.log.core.config;

import com.log.core.log.customHandle.CustomLogExceptionHandler;
import com.log.core.log.customHandle.DefaultCustomExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @Author lsw
 * @Date 2023/10/16 21:34
 * @Description
 */
@Configuration
public class StarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CustomLogExceptionHandler customLogExceptionHandler(){
        return new DefaultCustomExceptionHandler();
    }

    @Bean(name = "logTaskExecutor")
    @ConditionalOnMissingBean // 当不存在名为 "logTaskExecutor" 的 bean 时生效
    public ThreadPoolExecutor logTaskExecutor() {
        // 获取当前运行时对象
        Runtime runtime = Runtime.getRuntime();
        // 获取当前机器的CPU数量
        int cpuCount = runtime.availableProcessors();

        // 核心线程数
        int corePoolSize = cpuCount;
        // 最大线程数
        int maxPoolSize = cpuCount * 2;
        // 空闲线程存活时间
        long keepAliveTime = 30L;
        // 空闲线程存活时间的时间单位
        TimeUnit unit = TimeUnit.SECONDS;
        // 任务队列，最大容量5000
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        // 线程工厂，用于创建新线程
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        // 拒绝策略，用于处理当任务添加到线程池被拒绝时的情况
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime,
                unit, workQueue, threadFactory, handler);
        return executor;
    }

}
