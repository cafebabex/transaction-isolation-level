package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserRepeatableReadDaoService2;
import com.qh.transactionisolationlevel.util.MyThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @author quhao
 * 读未提交，最不安全的隔离级别
 * 产生脏读现象，读到其他线程还没有提交的事务信息
 */
@Service
@Slf4j
public class RepeatableRead2 {

    @Resource
    private UserRepeatableReadDaoService2 userRepeatableReadDaoService2;

    private final CountDownLatch latch = new CountDownLatch(2);

    public void test() {
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userRepeatableReadDaoService2.readCommittedThread1();
                    log.info("RR线程 结束");
                    latch.countDown();
                }
        );

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userRepeatableReadDaoService2.readCommittedThread3();
                    log.info("更新线程 结束");
                    latch.countDown();
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
