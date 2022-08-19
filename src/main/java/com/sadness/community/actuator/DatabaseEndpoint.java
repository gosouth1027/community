package com.sadness.community.actuator;

import com.sadness.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @version 1.0
 * @Date 2022/6/28 11:54
 * @Author SadAndBeautiful
 */
@Component
@Endpoint(id = "database")
@Slf4j
public class DatabaseEndpoint {

    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection() {
        try (
                Connection connection = dataSource.getConnection()
        ){
            return CommunityUtil.getJSONString(0, "获取数据库连接成功！");
        } catch (SQLException e) {
            log.error("获取数据库连接失败！");
            return CommunityUtil.getJSONString(0, "获取数据库连接成功！");
        }
        //return CommunityUtil.getJSONString(0, "获取数据库连接成功！");
    }

}
