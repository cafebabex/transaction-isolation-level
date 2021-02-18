package com.qh.transactionisolationlevel.service;

import com.qh.transactionisolationlevel.dao.mapper.UserMapper;
import com.qh.transactionisolationlevel.dao.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author quhao
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public Integer updateName(Long id, String name) {
        //更新，休眠10s后提交
        int i = userMapper.updateById(User.builder().id(id).name(name).build());
        try {
            Thread.sleep(5000);
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
}

