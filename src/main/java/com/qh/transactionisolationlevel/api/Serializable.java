package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserSerializableDaoService;
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
public class Serializable {

    @Resource
    private UserSerializableDaoService userSerializableDaoService;

    private final CountDownLatch latch = new CountDownLatch(2);

    public void test() {
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userSerializableDaoService.readCommittedThread1();
                    log.info("线程一结束");
                    latch.countDown();
                }
        );

        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userSerializableDaoService.readCommittedThread3();
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
