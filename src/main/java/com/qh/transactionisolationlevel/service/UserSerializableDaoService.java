package com.qh.transactionisolationlevel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;


/**
 * @author quhao
 */
@Service
@Slf4j
public class UserSerializableDaoService {

    private static final String UPDATE_NAME_BY_ID = "UPDATE `user` SET `age` = 1 where id = 1 ;";
    private static final String UPDATE_NAME = "UPDATE `user` SET `age` = 1 where id > 1;";
    private final CountDownLatch commitLatch = new CountDownLatch(1);

    private static final String SELECT_COUNT = "select name from user;";
    private static final String INSERT_USER = "insert into user (name,age) value ('insert' , 1)";
    private final CountDownLatch waitStartLatch = new CountDownLatch(1);
    @Resource
    private DataSource dataSource;

    public void readCommittedThread1() {
        Connection connection = null;
        int count = 0;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            statement = connection.createStatement();
            count = getUserCount(statement);
            log.info("RR线程 更新前读取数据条数：{}", count);

            waitStartLatch.countDown();
            log.info("RR线程 开启事务");

            commitLatch.await();
            count = getUserCount(statement);
            log.info("RR线程 提交后读取数据条数：{}", count);

            statement.execute(UPDATE_NAME_BY_ID);
            log.info("RR线程 对某一条数据进行更新");
            count = getUserCount(statement);
            log.info("RR线程 对指定数据更新后读取数据条数：{}", count);

            statement.execute(UPDATE_NAME);
            log.info("RR线程 对多条数据进行更新");
            count = getUserCount(statement);
            log.info("RR线程 对多条数据更新后读取数据条数：{}", count);

            connection.rollback();
        } catch (Exception e) {
            log.error("RR线程 异常", e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        } finally {
            closeSource(connection, statement);
        }
    }

    public void readCommittedThread3() {
        Connection connection = null;
        int count = 0;
        Statement statement = null;
        try {
            waitStartLatch.await();
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            log.info("更新线程 ：开启事务");

            count = getUserCount(statement);
            log.info("更新线程 更新前读取数据条数：{}，开始准备新增一条数据", count);
            statement.execute(INSERT_USER);

            log.info("更新线程 提交事务->->->->->->->->");
            connection.commit();
            commitLatch.countDown();
        } catch (Exception e) {
            log.error("更新线程异常", e);
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } finally {
            closeSource(connection, statement);
        }
    }

    private int getUserCount(Statement statement) throws SQLException {
        int i = 0;
        ResultSet resultSet = statement.executeQuery(SELECT_COUNT);
        while (resultSet.next()) {
            i++;
        }
        return i;
    }

    private void closeSource(Connection connection, Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

