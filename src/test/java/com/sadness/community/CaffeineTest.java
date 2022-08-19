package com.sadness.community;

import com.sadness.community.entity.DiscussPost;
import com.sadness.community.service.DiscussPostService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * @version 1.0
 * @Date 2022/6/27 17:39
 * @Author SadAndBeautiful
 */
@SpringBootTest
@Slf4j
public class CaffeineTest {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 30000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("送东阳马生序");
            post.setContent("余幼时即嗜学。家贫，无从致书以观，每假借于藏书之家，手自笔录，计日以还。天大寒，砚冰坚，手指不可屈伸，弗之怠。录毕，走送之，不敢稍逾约。以是人多以书假余，余因得遍观群书。");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.addDiscussPost(post);
        }
        log.info("任务完成！");
    }

    @Test
    public void testCache() {
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 0));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.getDiscussPosts(0, 0, 10, 1));
    }


}
