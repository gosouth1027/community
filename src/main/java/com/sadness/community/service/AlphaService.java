package com.sadness.community.service;

import com.sadness.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @version 1.0
 * @Date 2022/5/30 9:58
 * @Author SadAndBeautiful
 */
@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public String find() {
        return alphaDao.select();
    }

    public AlphaService() {
        System.out.println("AlphaService构造器被调用...");
    }

    @PostConstruct
    public void init(){
        System.out.println("AlphaService初始化...");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("AlphaService销毁前...");
    }
}
