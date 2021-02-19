package com.qh.transactionisolationlevel.service;

import com.qh.transactionisolationlevel.dao.mapper.UserMapper;
import com.qh.transactionisolationlevel.dao.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author quhao
 */
@Service
@Slf4j
public class UserDaoService {

    @Autowired
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public Integer updateName(Long id, String name) {
        //更新，休眠10s后提交
        int i = userMapper.updateById(User.builder().id(id).name(name).build());
        log.info("线程二 更新了数据为：{},但是还没有提交",name);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return i;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_UNCOMMITTED)
    public String getUnCommitName(long l) {
        return userMapper.selectById(l).getName();
    }

    public String getName(long l) {
        return userMapper.selectById(l).getName();
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

    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_UNCOMMITTED)
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

