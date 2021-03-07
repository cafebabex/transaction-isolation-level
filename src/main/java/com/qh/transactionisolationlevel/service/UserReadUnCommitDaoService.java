package com.qh.transactionisolationlevel.service;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.qh.transactionisolationlevel.dao.mapper.UserMapper;
import com.qh.transactionisolationlevel.dao.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.CountDownLatch;


/**
 * @author quhao
 */
@Service
@Slf4j
public class UserReadUnCommitDaoService {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UserMapper userMapper;

    private final CountDownLatch waitUpdateLatch = new CountDownLatch(2);
    private final CountDownLatch waitStartLatch = new CountDownLatch(2);
    private final CountDownLatch updateLatch = new CountDownLatch(1);
    private final CountDownLatch commitLatch = new CountDownLatch(1);

    public void readUnCommittedThread1(Long id) {
        //1.获取事务定义
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        //2.设置事务隔离级别，开启新事务
        def.setIsolationLevel(Isolation.READ_UNCOMMITTED.value());
        //3.获得事务状态
        TransactionStatus status = transactionManager.getTransaction(def);
        waitStartLatch.countDown();
        try {
            User user;
            updateLatch.await();
            user = userMapper.selectById(id);
            log.info("读未提交线程 更新后读取数据name：{}", user.getName());

            waitUpdateLatch.countDown();
            commitLatch.await();
            user = userMapper.selectById(id);
            log.info("读未提交线程 提交后读取数据name：{}", user.getName());
            transactionManager.commit(status);
        } catch (Exception e) {
            log.error("读未提交线程 异常", e);
            transactionManager.rollback(status);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void readUnCommittedThread2(Long id) {
        try {
            User user;
            waitStartLatch.countDown();
            updateLatch.await();
            user = userMapper.selectById(id);
            log.info("普通线程 更新后读取数据name：{}", user.getName());
            waitUpdateLatch.countDown();
            commitLatch.await();
            user = userMapper.selectById(id);
            log.info("普通线程 提交后读取数据name：{}", user.getName());
        } catch (Exception e) {
            log.error("普通线程 异常", e);
        }
    }

    public void readUnCommittedThread3(Long id, String name) {
        try {
            waitStartLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //1.获取事务定义
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        //2.设置事务隔离级别，开启新事务
        def.setIsolationLevel(Isolation.READ_UNCOMMITTED.value());
        //3.获得事务状态
        TransactionStatus status = transactionManager.getTransaction(def);

        User user = userMapper.selectById(id);
        log.info("更新线程 更新前读取数据name：{}，开始准备将数据更新为：{}", user.getName(), name);
        try {
            user.setName(name);
            int i = userMapper.updateById(user);
            if (i < 1) {
                throw new MybatisPlusException("更新数据异常");
            }
            log.info("更新线程 将数据更新为：{}，但是还没有提交", user.getName());
            updateLatch.countDown();

            waitUpdateLatch.await();
            log.info("更新线程 提交事务->->->->->->->->");
            transactionManager.commit(status);
        } catch (Exception e) {
            log.error("更新线程 异常", e);
            transactionManager.rollback(status);
        } finally {
            commitLatch.countDown();
        }
    }
}

