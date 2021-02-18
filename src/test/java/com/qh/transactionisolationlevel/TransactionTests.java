package com.qh.transactionisolationlevel;

import com.qh.transactionisolationlevel.controller.ReadUnCommit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
class TransactionTests {

    @Autowired
    ReadUnCommit readUnCommit;

    /**
     * 测试不可提交读方式产生的脏读问题，一个线程读到了另外线程的未提交事务
     */
    @Test
    void readUnCommit(){
        //一个数据库值的id
        Long id = 1L;
        //要更新的值
        String expectStr = String.valueOf(new Random().nextInt());
        readUnCommit.sleepAndCommit(id, expectStr);
    }

}
