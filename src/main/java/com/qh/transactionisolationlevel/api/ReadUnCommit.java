package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserDaoService;
import com.qh.transactionisolationlevel.util.MyThreadPoolUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Autowired
    private UpdateNameCallable updateNameCallable;

    public void sleepAndCommit(Long id, String name) {
        try {
            //使用正常方法查询数据库的值
            String nowStr = userDaoService.getName(1L);
            log.info("准备更新的值：{}", name);
            log.info("当前数据库的值：{}", nowStr);

            //开一个线程开启事务去更新，休眠10s后提交
            ThreadPoolExecutor pool = MyThreadPoolUtil.getPool();
            updateNameCallable.setName(name);
            updateNameCallable.setId(id);
            Future<Integer> submit = pool.submit(updateNameCallable);


            //休眠一秒，等另一个线程事务操作
            Thread.sleep(1000);

            //使用不可提交读读取数据库的值
            String readUnCommitStr = userDaoService.getUnCommitName(1L);
            log.info("线程一 不可提交读隔离方式读取的值：{}", readUnCommitStr);

            //使用正常方法查询数据库的值
            String normalStr = userDaoService.getName(1L);
            log.info("线程一 正常读取的值：{}", normalStr);

            submit.get();
            log.info("线程二 提交了事务");
            //事务提交后数据库的值
            String commitStr = userDaoService.getName(1L);
            log.info("线程一 提交事务后，数据库的值：{}", commitStr);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

@Service
class UpdateNameCallable implements Callable<Integer> {

    @Autowired
    private UserDaoService userDaoService;

    @Setter
    private Long id;
    @Setter
    private String name;

    @Override
    public Integer call() throws Exception {
        return userDaoService.updateName(id, name);
    }
}
