package com.sadness.community;

import com.sadness.community.dao.DiscussPostMapper;
import com.sadness.community.dao.UserMapper;
import com.sadness.community.entity.DiscussPost;
import com.sadness.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

/**
 * @version 1.0
 * @Date 2022/5/31 11:57
 * @Author SadAndBeautiful
 */

@SpringBootTest
//@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelect(){
        User user = userMapper.selectUserById(111);
        System.out.println(user);

        user = userMapper.selectUserByUsername("aaa");
        System.out.println(user);

        user = userMapper.selectUserByEmail("nowcoder111@sina.com");
        System.out.println(user);
    }
    @Test
    public void testInsert(){
        User user = new User();
        user.setUsername("赵云");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testDiscussPosts(){
        int count = discussPostMapper.selectCount(133);
        System.out.println(count);
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(133, 0, 5, 0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }

}
