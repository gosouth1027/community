package com.sadness.community.dao.impl;

import com.sadness.community.dao.AlphaDao;
import org.springframework.stereotype.Repository;

/**
 * @version 1.0
 * @Date 2022/5/30 9:23
 * @Author SadAndBeautiful
 */
@Repository
public class AlphaDaoHibernateImpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}
