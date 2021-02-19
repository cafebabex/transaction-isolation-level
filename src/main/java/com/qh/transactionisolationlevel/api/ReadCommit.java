package com.qh.transactionisolationlevel.api;

import com.qh.transactionisolationlevel.service.UserDaoService;
import com.qh.transactionisolationlevel.util.MyThreadPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author quhao
 */
@Service
public class ReadCommit {

    @Autowired
    private UserDaoService userDaoService;

    public void read(Long id, String expectName) {

        //当前线程去读，更新钱读一次，更新时读一次，事务提交后读一次
        MyThreadPoolUtil.getPool().execute(() -> userDaoService.read(id));

        //新开线程更新数据,5后提交事务
        MyThreadPoolUtil.getPool().execute(() -> userDaoService.updateNameNo5sLater(id,expectName));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
