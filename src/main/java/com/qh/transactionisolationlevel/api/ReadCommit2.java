package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserReadCommitDaoService2;
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
public class ReadCommit2 {

    private final CountDownLatch latch = new CountDownLatch(3);
    @Resource
    private UserReadCommitDaoService2 userReadCommitDaoService2;

    public void test(String name) {

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userReadCommitDaoService2.readCommittedThread1();
                    log.info("RR线程 结束");
                    latch.countDown();
                }
        );

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userReadCommitDaoService2.readCommittedThread2();
                    log.info("RC线程 结束");
                    latch.countDown();
                }
        );

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userReadCommitDaoService2.readCommittedThread3(name);
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
