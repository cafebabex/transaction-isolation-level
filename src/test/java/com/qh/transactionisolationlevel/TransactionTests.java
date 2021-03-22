package com.qh.transactionisolationlevel;

import com.qh.transactionisolationlevel.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Random;

@SpringBootTest
class TransactionTests {

    @Resource
    ReadUnCommit readUnCommit;
    @Resource
    ReadCommit readCommit;
    @Resource
    ReadCommit2 readCommit2;
    @Resource
    RepeatableRead repeatableRead;
    @Resource
    RepeatableRead2 repeatableRead2;
    @Resource
    Serializable serializable;


    /**
     * 测试不可提交读方式产生的脏读问题
     * 脏读线程读到了另外线程的未提交事务
     * 普通线程没有问题避免了脏读问题
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
     */
    @Test
    void readCommit() {
        //要更新的值
        String expectStr = String.valueOf(new Random().nextInt(100));
        readCommit.test(expectStr);
    }

    @Test
    void readCommit2() {
        //要更新的值
        String expectStr = String.valueOf(new Random().nextInt(100));
        readCommit2.test(expectStr);
    }

    /**
     * 测试可提交读产生的不可重复读问题
     * 线程一读取出当前的值
     * 线程二更新数据库的值
     * 线程一读取出更新的值
     * 导致线程一同一个事务中，两次读取出来的值不一样
     * FIXME 丛输出结果可以看出，rc线程很明显两次读取出了不同的条数，发生了幻读
     *  但是RR线程出人意料的没有发生幻读，在我们的尝试理解中，RR是会发生幻读的，但是为啥没有发生呢
     *  这个其实就是高版本mysql做出的优化，RR隔离级别在一定程度上避免了幻读现象
     *  下面的测试方法RepeatableRead2()我们来模拟出发生幻读，读者可以看看同样是RR级别为什么有的会发生幻读，有的不会发生
     *  如果读者执行了RepeatableRead2（）这个测试方法了，相信仔细观察的话就会发现，最后发生了幻读现象
     *  表面原因就是因为对多条数据发生更新会产生幻读现象
     *  深层次一点的原因呢，就是因为RR级别下更新数据会触发当前读，当前读就会发生幻读现象
     *  而不更新数据，是快照读，快照读是不会发生幻读现象的。
     *  如果读者要具体深入了解的话可以了解数据库的mvcc机制，在这里不过多叙述
     *  我感觉一个挺好的关于mvcc的视频地址 ：https://www.bilibili.com/video/BV1Lt4y1S7zF?from=search&seid=5070580630378692712
     */
    @Test
    void RepeatableRead(){
        repeatableRead.test();
    }

    @Test
    void RepeatableRead2(){
        repeatableRead2.test();
    }

    /**
     * 两个线程，一个线程写，一个线程读
     * 运行后发现写线程一直无法写入数据，因为之前的读线程已经开启了事务并且隔离级别为serializable
     */
    @Test
    void serializable(){
        serializable.test();
    }

}
