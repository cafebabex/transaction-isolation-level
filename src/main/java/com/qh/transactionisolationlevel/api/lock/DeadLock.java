package com.qh.transactionisolationlevel.api.lock;

import com.qh.transactionisolationlevel.service.lock.DeadLockDaoService;
import com.qh.transactionisolationlevel.util.MyThreadPoolUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @author qu.hao
 * @date 2021-04-01- 2:23 下午
 * @email quhao.mi@foxmail.com
 */
@Service
public class DeadLock {

    CountDownLatch countDownLatch = new CountDownLatch(2);
    @Resource
    private DeadLockDaoService deadLockDaoService;

    public void test() {
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    deadLockDaoService.lock1();
                    countDownLatch.countDown();
                }
        );

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    deadLockDaoService.lock2();
                    countDownLatch.countDown();
                }
        );

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void test2() {
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    deadLockDaoService.lock3();
                    countDownLatch.countDown();
                }
        );

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    deadLockDaoService.lock4();
                    countDownLatch.countDown();
                }
        );

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
