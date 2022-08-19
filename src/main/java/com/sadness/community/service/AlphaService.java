package com.sadness.community.service;

import com.sadness.community.dao.AlphaDao;
import com.sadness.community.dao.DiscussPostMapper;
import com.sadness.community.dao.UserMapper;
import com.sadness.community.entity.DiscussPost;
import com.sadness.community.entity.User;
import com.sadness.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

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

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

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


    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object transactionOne() {
        User user = new User();
        user.setUsername("苏轼");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle("一二三");
        discussPost.setContent("四五六");
        discussPostMapper.insertDiscussPost(discussPost);

        int i = 6 / 0;
        return "ok";
    }

    public Object transactionTwo() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                User user = new User();
                user.setUsername("欧阳修");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setCreateTime(new Date());
                userMapper.insertUser(user);
                DiscussPost discussPost = new DiscussPost();
                discussPost.setTitle("000");
                discussPost.setContent("2312312321");
                discussPostMapper.insertDiscussPost(discussPost);

                int i = 6 / 0;
                return "ok";
            }
        });
    }
}
