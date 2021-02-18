package com.qh.transactionisolationlevel.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qh.transactionisolationlevel.dao.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author quhao
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
