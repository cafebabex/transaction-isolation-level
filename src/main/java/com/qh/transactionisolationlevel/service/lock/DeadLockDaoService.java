package com.qh.transactionisolationlevel.service.lock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.qh.transactionisolationlevel.dao.mapper.UserMapper;
import com.qh.transactionisolationlevel.dao.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.util.concurrent.CyclicBarrier;

/**
 * @author qu.hao
 * @date 2021-04-01- 2:24 下午
 * @email quhao.mi@foxmail.com
 */
@Repository
@Slf4j
public class DeadLockDaoService {
    private final CyclicBarrier barrier = new CyclicBarrier(2);
    @Resource
    private PlatformTransactionManager transactionManager;
    @Resource
    private UserMapper userMapper;

    public void lock1() {
        //1.获取事务定义
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        //2.设置事务隔离级别，开启新事务
        def.setIsolationLevel(Isolation.READ_UNCOMMITTED.value());
        //3.获得事务状态
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            //修改id为1的记录
            userMapper.update(null, new UpdateWrapper<User>().lambda()
                    .set(User::getName, "lock1")
                    .eq(User::getId, 1)
            );
            log.info("修改了id为1的记录");
            barrier.await();
            //修改id为2的记录
            log.info("开始修改了id为2的记录");
            userMapper.update(null, new UpdateWrapper<User>().lambda()
                    .set(User::getName, "lock1")
                    .eq(User::getId, 2)
            );

            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("异常", e);
        }
    }

    public void lock2() {
        //1.获取事务定义
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        //2.设置事务隔离级别，开启新事务
        def.setIsolationLevel(Isolation.READ_UNCOMMITTED.value());
        //3.获得事务状态
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            //修改id为2的记录
            userMapper.update(null, new UpdateWrapper<User>().lambda()
                    .set(User::getName, "lock2")
                    .eq(User::getId, 2)
            );
            log.info("修改了id为2的记录");
            barrier.await();
            //修改id为1的记录
            log.info("开始修改了id为1的记录");
            userMapper.update(null, new UpdateWrapper<User>().lambda()
                    .set(User::getName, "lock2")
                    .eq(User::getId, 1)
            );

            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("异常", e);
        }
    }

    public void lock3() {
        //1.获取事务定义
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        //2.设置事务隔离级别，开启新事务
        def.setIsolationLevel(Isolation.REPEATABLE_READ.value());
        //3.获得事务状态
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            //查询id为1的记录
            userMapper.selectOne(new QueryWrapper<User>().lambda()
                    .eq(User::getId, 1)
            );
            log.info("查询了id为1的记录");
            barrier.await();
            //修改id为2的记录
            userMapper.selectOne(new QueryWrapper<User>().lambda()
                    .eq(User::getId, 2)
            );
            log.info("查询了id为2的记录");
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("异常", e);
        }
    }

    public void lock4() {
        //1.获取事务定义
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        //2.设置事务隔离级别，开启新事务
        def.setIsolationLevel(Isolation.REPEATABLE_READ.value());
        //3.获得事务状态
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            //查询id为1的记录
            userMapper.selectOne(new QueryWrapper<User>().lambda()
                    .eq(User::getId, 2)
            );
            log.info("查询了id为2的记录");
            barrier.await();
            //修改id为2的记录
            userMapper.selectOne(new QueryWrapper<User>().lambda()
                    .eq(User::getId, 1)
            );
            log.info("查询了id为1的记录");
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("异常", e);
        }
    }
}
