package com.qh.transactionisolationlevel.service;

import com.qh.transactionisolationlevel.dao.mapper.UserMapper;
import com.qh.transactionisolationlevel.dao.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author quhao
 */
@Service
@Slf4j
public class UserDaoService {

    @Autowired
    private UserMapper userMapper;

    private final CountDownLatch waitUpdateLatch = new CountDownLatch(2);
    private final CountDownLatch updateLatch = new CountDownLatch(1);
    private final CountDownLatch commitLatch = new CountDownLatch(1);


    /**
     * 正常线程，在更新前，开启事务更新后，事务提交后，分别读取三次值
     *
     * @param id id
     */
    public void readUnCommittedThread1(Long id) {
        try {
            User user;
            updateLatch.await();
            user = userMapper.selectById(id);
            log.info("线程一 更新后读取数据name：{}", user.getName());
            waitUpdateLatch.countDown();
            commitLatch.await();
            user = userMapper.selectById(id);
            log.info("线程一 提交后读取数据name：{}", user.getName());
        } catch (Exception e) {
            log.error("线程一异常", e);
        }

    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED)
    public void readUnCommittedThread2(Long id) {
        try {
            User user;
            updateLatch.await();
            user = userMapper.selectById(id);
            log.info("线程二 更新后读取数据name：{}", user.getName());
            waitUpdateLatch.countDown();
            commitLatch.await();
            user = userMapper.selectById(id);
            log.info("线程二 提交后读取数据name：{}", user.getName());
        } catch (Exception e) {
            log.error("线程二异常", e);
        }
    }

    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_UNCOMMITTED)
    public void readUnCommittedThread3(Long id, String name) {
        User user = userMapper.selectById(id);
        log.info("线程三 更新前读取数据name：{}，开始准备将数据更新为：{}", user.getName(), name);
        try {
            user.setName(name);
            int i = userMapper.updateById(user);
            if (i < 1) {
                throw new RuntimeException("更新数据异常");
            }
            log.info("线程三 将数据更新为：{}，但是还没有提交", user.getName());
            updateLatch.countDown();

            waitUpdateLatch.await();
            log.info("线程三 提交事务->->->->->->->->");
        } catch (Exception e) {
            log.error("线程三异常", e);
        }finally {
            commitLatch.countDown();
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED)
    public void read(Long id) {
        try {
            //更新前读取
            User user = userMapper.selectById(id);
            log.info("线程一 第一次读取数据，数据库的初始数据：{}", user.getName());

            Thread.sleep(3_000);
            //等待另外线程的修改，但是还未提交
            User user2 = userMapper.selectById(id);
            log.info("线程一 第二次读取数据，此时已经修改，但是事务还未提交：{}", user2.getName());

            Thread.sleep(5_000);
            //第二个线程已经提交，再次读取
            User user3 = userMapper.selectById(id);
            log.info("线程一 第三次读取数据，事务已经提交：{}", user3.getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED)
    public void updateNameNo5sLater(Long id, String expectName) {
        try {
            Thread.sleep(1_000);
            log.info("线程二 开始更新数据，准备将数据更新为：{}", expectName);
            User user = User.builder().id(id).name(expectName).build();
            userMapper.updateById(user);

            Thread.sleep(5_000);
            log.info("线程二 提交事务，将数据更新为：{}", expectName);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

}

