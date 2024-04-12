package com.log.core.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 异步组件
 * @Author lsw
 * @Date 2023/5/6 13:07
 */
@Component
public class AsyncComp {

    @Autowired
    private TaskPool taskPool;

    public void execute(Runnable runnable) {
        taskPool.execute(runnable);
    }

}
