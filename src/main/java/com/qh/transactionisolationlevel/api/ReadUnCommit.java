package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserDaoService;
import com.qh.transactionisolationlevel.util.MyThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * @author quhao
 * 读未提交，最不安全的隔离级别
 * 产生脏读现象，读到其他线程还没有提交的事务信息
 */
@Service
@Slf4j
public class ReadUnCommit {

    @Autowired
    private UserDaoService userDaoService;

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
    public void testReadUnCommitted(Long id, String name) {


        //线程一：正常线程，开启事务更新后，事务提交后，分别读取两次值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userDaoService.readUnCommittedThread1(id);
                    latch.countDown();
                }
        );

        //线程二：脏读线程，开启事务更新后，事务提交后，分别读取两次值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userDaoService.readUnCommittedThread2(id);
                    latch.countDown();
                }
        );

        //线程三：开启事务，默认隔离级别，更新当前值为需要的值
        MyThreadPoolUtil.getPool().execute(
                () -> {
                    userDaoService.readUnCommittedThread3(id, name);
                    latch.countDown();
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
