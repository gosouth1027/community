package com.sadness.community.dao.impl;

import com.sadness.community.dao.AlphaDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @version 1.0
 * @Date 2022/5/30 9:37
 * @Author SadAndBeautiful
 */
@Repository
@Primary
public class AlphaDaoMybatisImpl implements AlphaDao {

    @Override
    public String select() {
        return "Mybatis";
    }
}
