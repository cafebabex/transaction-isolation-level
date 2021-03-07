package com.qh.transactionisolationlevel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class UserReadCommitDaoService {

    @Autowired
    private DataSource dataSource;

    private final CountDownLatch waitUpdateLatch = new CountDownLatch(2);
    private final CountDownLatch waitStartLatch = new CountDownLatch(2);
    private final CountDownLatch updateLatch = new CountDownLatch(1);
    private final CountDownLatch commitLatch = new CountDownLatch(1);

    private static final String SELECT_NAME = "select name from user where id = 1;";
    private static final String SELECT_COUNT = "select name from user;";

    public void readCommittedThread1() {
        Connection connection = null;
        String name = "";
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            statement = connection.createStatement();
            waitStartLatch.countDown();
            log.info("线程一 开启事务");

            updateLatch.await();
            name = getUserName(statement);
            log.info("线程一 更新后读取数据name：{}", name);
            waitUpdateLatch.countDown();

            commitLatch.await();
            name = getUserName(statement);
            log.info("线程一 提交后读取数据name：{}", name);
            connection.commit();
        } catch (Exception e) {
            log.error("线程一异常", e);
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


    public void readCommittedThread2() {
        Connection connection = null;
        String name = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            statement = connection.createStatement();
            waitStartLatch.countDown();
            log.info("线程二 开启事务");

            updateLatch.await();
            name = getUserName(statement);
            log.info("线程二 更新后读取数据name：{}", name);
            waitUpdateLatch.countDown();

            commitLatch.await();
            name = getUserName(statement);
            log.info("线程二 提交后读取数据name：{}", name);
        } catch (Exception e) {
            log.error("线程二异常", e);
        } finally {
            closeSource(connection, statement);
        }
    }

    public void readCommittedThread3(String name) {
        Connection connection = null;
        String oldName = "";
        Statement statement = null;
        try {
            waitStartLatch.await();
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            log.info("线程三 ：开启事务");

            oldName = getUserName(statement);
            log.info("线程三 更新前读取数据name：{}，开始准备将数据更新为：{}", oldName, name);
            statement.execute("update user set name = " + name + " where  id = 1;");
            log.info("线程三 将数据更新为：{}，但是还没有提交", name);
            updateLatch.countDown();

            waitUpdateLatch.await();
            log.info("线程三 提交事务->->->->->->->->");
            connection.commit();
            commitLatch.countDown();
        } catch (Exception e) {
            log.error("线程三异常", e);
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

    private String getUserName(Statement statement) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSet resultSet = statement.executeQuery(SELECT_NAME);
        while (resultSet.next()) {
            sb.append(resultSet.getString("name"));
        }
        return sb.toString();
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

