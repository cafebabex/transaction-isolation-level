package com.qh.transactionisolationlevel;

import com.qh.transactionisolationlevel.api.lock.DeadLock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author qu.hao
 * @date 2021-04-01- 2:21 下午
 * @email quhao.mi@foxmail.com
 */
@SpringBootTest
class DeadLockTest {

    @Resource
    private DeadLock deadLock;

    /**
     * 模拟一个数据库死锁案例
     * 前提条件：保证user表中存在主键id为1和2的两条记录
     * 测试方法 A线程更新id为1记录然后B线程更新id为2记录，之后A线程再次更新id为2记录
     * 测试结果：发生死锁 报错
     * Cause: com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException:
     * Deadlock found when trying to get lock; try restarting transaction
     */
    @Test
    void updateRecord() {
        deadLock.test();
    }

    /**
     * 同样的思路测试查询，不会发生死锁
     */
    @Test
    void selectRecord() {
        deadLock.test2();
    }
}
