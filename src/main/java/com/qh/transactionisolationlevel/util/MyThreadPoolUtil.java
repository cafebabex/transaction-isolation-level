package com.qh.transactionisolationlevel.util;

import java.util.concurrent.*;

/**
 * @author 线程池
 */
public class MyThreadPoolUtil {

    private static volatile ThreadPoolExecutor pool = null;

    private MyThreadPoolUtil() {
    }

    public static ThreadPoolExecutor getPool(){
        if(pool == null){
            synchronized (MyThreadPoolUtil.class){
                if(pool == null){
                    pool = new ThreadPoolExecutor(4,10,60, TimeUnit.MILLISECONDS,
                            new LinkedBlockingDeque<>(10), Executors.defaultThreadFactory(), new MyRejectedExecutionHandler());
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
