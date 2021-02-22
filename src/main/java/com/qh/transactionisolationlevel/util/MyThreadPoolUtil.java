package com.qh.transactionisolationlevel.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 线程池
 */
public class MyThreadPoolUtil {

    private static volatile ThreadPoolExecutor pool = null;

    private MyThreadPoolUtil() {
    }

    public static ThreadPoolExecutor getPool() {
        if (pool == null) {
            synchronized (MyThreadPoolUtil.class) {
                if (pool == null) {
                    pool = new ThreadPoolExecutor(4, 10, 60, TimeUnit.MILLISECONDS,
                            new LinkedBlockingDeque<>(10), new MyThreadFactory(), new MyRejectedExecutionHandler());
                }
            }
        }
        return pool;
    }
}

class MyRejectedExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        throw new RuntimeException("线程资源不足");
    }
}

class MyThreadFactory implements ThreadFactory {

    private final AtomicInteger integer = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("my_thread" + integer.getAndDecrement());
        return thread;
    }
}

