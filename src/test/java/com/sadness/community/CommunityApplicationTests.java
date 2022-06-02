package com.sadness.community;

import com.sadness.community.dao.AlphaDao;
import com.sadness.community.dao.impl.AlphaDaoHibernateImpl;
import com.sadness.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext() {
        System.out.println(applicationContext);
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao);
    }

    @Test
    public void testBeanManagement(){
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
        alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
    }

    @Autowired
    private SimpleDateFormat simpleDateFormat;
    @Autowired
    @Qualifier("alphaDaoHibernateImpl")
    private AlphaDao alphaDao;

    @Test
    public void testDI() {
        System.out.println(simpleDateFormat.format(new Date()));
        System.out.println(alphaDao);
    }

}
