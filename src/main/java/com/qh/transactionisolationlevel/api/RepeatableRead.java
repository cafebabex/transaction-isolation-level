package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserRepeatableReadDaoService;
import com.qh.transactionisolationlevel.util.MyThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * @author quhao
 * 读未提交，最不安全的隔离级别
 * 产生脏读现象，读到其他线程还没有提交的事务信息
 */
@Service
@Slf4j
public class RepeatableRead {

    @Autowired
    private UserRepeatableReadDaoService userRepeatableReadDaoService;

    private final CountDownLatch latch = new CountDownLatch(3);

    /**
     * 测试思路：总共开三个线程，
     * 线程一：正常线程，开启事务更新后，事务提交后，分别读取两次值
     * 线程二：脏读线程，开启事务更新后，事务提交后，分别读取两次值
     * 线程三：开启事务，默认隔离级别，更新当前值为需要的值
     *
     */
    public void test() {


        //线程一：正常线程，开启事务更新后，事务提交后，分别读取两次值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userRepeatableReadDaoService.readCommittedThread1();
                    log.info("线程一结束");
                    latch.countDown();
                }
        );

        //线程二：脏读线程，开启事务更新后，事务提交后，分别读取两次值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userRepeatableReadDaoService.readCommittedThread2();
                    log.info("线程二结束");
                    latch.countDown();
                }
        );


        //线程三：开启事务，默认隔离级别，更新当前值为需要的值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userRepeatableReadDaoService.readCommittedThread3();
                    log.info("线程三结束");
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