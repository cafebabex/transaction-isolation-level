package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserReadUnCommitDaoService;
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
public class ReadUnCommit {

    @Resource
    private UserReadUnCommitDaoService userReadUnCommitDaoService;

    private final CountDownLatch latch = new CountDownLatch(3);

    /**
     * 测试思路：总共开三个线程，
     * 线程一：正常线程，开启事务更新后，事务提交后，分别读取两次值
     * 线程二：脏读线程，开启事务更新后，事务提交后，分别读取两次值
     * 线程三：开启事务，默认隔离级别，更新当前值为需要的值
     *
     * @param id   要更新的数据库记录id
     * @param name 要把user的名字改成什么
     */
    public void test(Long id, String name) {


        //线程一：正常线程，开启事务更新后，事务提交后，分别读取两次值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userReadUnCommitDaoService.readUnCommittedThread1(id);
                    log.info("读未提交线程 结束");
                    latch.countDown();
                }
        );

        //线程二：脏读线程，开启事务更新后，事务提交后，分别读取两次值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userReadUnCommitDaoService.readUnCommittedThread2(id);
                    log.info("普通线程 结束");
                    latch.countDown();
                }
        );


        //线程三：开启事务，默认隔离级别，更新当前值为需要的值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userReadUnCommitDaoService.readUnCommittedThread3(id, name);
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
