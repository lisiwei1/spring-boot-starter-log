package com.log.core.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * @Author lsw
 * @Date 2023/5/6 13:07
 */
@EnableAsync
@Component
public class TaskPool {

    @Async
    void execute(Runnable runnable){
        runnable.run();
    }

}
