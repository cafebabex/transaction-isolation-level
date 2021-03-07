package com.qh.transactionisolationlevel;

import com.qh.transactionisolationlevel.api.ReadCommit;
import com.qh.transactionisolationlevel.api.ReadUnCommit;
import com.qh.transactionisolationlevel.api.RepeatableRead;
import com.qh.transactionisolationlevel.api.Serializable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
class TransactionTests {

    @Autowired
    ReadUnCommit readUnCommit;
    @Autowired
    ReadCommit readCommit;
    @Autowired
    RepeatableRead repeatableRead;
    @Autowired
    Serializable serializable;


    /**
     * 测试不可提交读方式产生的脏读问题，一个线程读到了另外线程的未提交事务
     */
    @Test
    void readUnCommit(){
        //一个数据库值的id
        Long id = 1L;
        //要更新的值
        String expectStr = String.valueOf(new Random().nextInt(100));
        readUnCommit.test(id, expectStr);
    }


    /**
     * 测试可提交读产生的不可重复读问题
     * 线程一读取出当前的值
     * 线程二更新数据库的值
     * 线程一读取出更新的值
     * 导致线程一同一个事务中，两次读取出来的值不一样
     */
    @Test
    void readCommit(){
        //要更新的值
        String expectStr = String.valueOf(new Random().nextInt(100));
        readCommit.test(expectStr);
    }

    /**
     * 测试可提交读产生的不可重复读问题
     * 线程一读取出当前的值
     * 线程二更新数据库的值
     * 线程一读取出更新的值
     * 导致线程一同一个事务中，两次读取出来的值不一样
     */
    @Test
    void RepeatableRead(){
        repeatableRead.test();
    }
    /**
     * 测试可提交读产生的不可重复读问题
     * 线程一读取出当前的值
     * 线程二更新数据库的值
     * 线程一读取出更新的值
     * 导致线程一同一个事务中，两次读取出来的值不一样
     */
    @Test
    void serializable(){
        serializable.test();
    }

}
