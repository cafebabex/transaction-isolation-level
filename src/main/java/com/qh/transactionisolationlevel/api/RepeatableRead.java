package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserRepeatableReadDaoService;
import com.qh.transactionisolationlevel.util.MyThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * @author quhao
 */
@Service
@Slf4j
public class RepeatableRead {

    @Resource
    private UserRepeatableReadDaoService userRepeatableReadDaoService;

    private final CountDownLatch latch = new CountDownLatch(3);

    public void test() {

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userRepeatableReadDaoService.readCommittedThread1();
                    log.info("线程一结束");
                    latch.countDown();
                }
        );

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userRepeatableReadDaoService.readCommittedThread2();
                    log.info("线程二结束");
                    latch.countDown();
                }
        );

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
