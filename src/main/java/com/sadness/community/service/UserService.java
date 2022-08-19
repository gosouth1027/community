package com.sadness.community.service;

import com.sadness.community.dao.LoginTicketMapper;
import com.sadness.community.dao.UserMapper;
import com.sadness.community.entity.LoginTicket;
import com.sadness.community.entity.User;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import com.sadness.community.util.MailClient;
import com.sadness.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.rmi.MarshalledObject;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Date 2022/6/1 10:42
 * @Author SadAndBeautiful
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domian;

    @Value("${server.servlet.context-path}")
    private String contextPath;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;


    public User getUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    public User findUserByName(String username) {
        return userMapper.selectUserByUsername(username);
    }

    /**
     * 注册业务，判断注册信息是否合法，注册邮箱用户名是否存在，密码加密，发送激活邮件
     */
    public Map<String, Object> register(User user) {
        // map用来存放注册错误相关信息
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 判断用户名是否合法
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        // 判断邮箱是否合法
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "用户邮箱不能为空！");
            return map;
        }
        // 判断密码是否合法
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证注册用户名邮箱是否已经存在
        User u = userMapper.selectUserByUsername(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "用户名已存在！");
            return map;
        }
        u = userMapper.selectUserByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已存在！");
            return map;
        }

        // 注册处理，密码加密，将注册信息放入数据库
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        context.setVariable("url", domian + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode());
        String process = templateEngine.process("/mail/activation", context);
        mailClient.sentMail(user.getEmail(), "激活邮件", process);

        return map;
    }

    /**
     * 激活账号，判断是否重复激活，激活码是否正确
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectUserById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 处理登陆业务，判断账号，密码是否为空，账号是否存在，发送登陆凭证
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {

        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        // 判断账号是否存在，密码是否正确，是否激活
        User user = userMapper.selectUserByUsername(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        // 验证密码是否正确
        if (!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }
        // 验证账号是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活，请激活后使用！");
            return map;
        }

        // 没有问题，登陆成功，发送登陆凭证
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setTicket(CommunityUtil.generateUUID());
        ticket.setStatus(0);
        ticket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(ticket);

        // 使用Redis存储登陆凭证
        String ticketKey = RedisKeyUtil.getTicketKey(ticket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, ticket);

        map.put("ticket", ticket.getTicket());

        return map;
    }

    /**
     * 注销业务，撤销登陆凭证
     */
    public void logout(String ticket) {
        // loginTicketMapper.updateLoginTicket(ticket, 1);
        // 修改Redis中凭证的状态
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    /**
     * 获取登录凭证
     */
    public LoginTicket getLoginTicket(String ticket) {
        // return loginTicketMapper.selectLoginTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        return loginTicket;
    }

    /**
     * 更新头像
     */
    public int updateUserHeaderUrl(int userId, String headerUrl) {
        int row = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return row;
    }

    /**
     * 修改密码
     */
    public int updatePassword(int userId, String password) {
        int row = userMapper.updatePassword(userId, password);
        clearCache(userId);
        return row;
    }

    /**
     * 优先从缓存中取值
     */
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /**
     * 缓存中取不到值则初始化
     */
    private User initCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = userMapper.selectUserById(userId);
        redisTemplate.opsForValue().set(userKey, user);
        return user;
    }

    /**
     * 数据变更时清除缓存
     */
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    /**
     * 获取用户的权限
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        List<GrantedAuthority> list = new ArrayList<>();
        User user = getUserById(userId);
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}

