package com.sadness.community.controller;

import com.google.code.kaptcha.Producer;
import com.sadness.community.entity.User;
import com.sadness.community.service.UserService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import com.sadness.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Date 2022/6/4 10:52
 * @Author SadAndBeautiful
 */
@Slf4j
@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 响应登陆页面
     */
    @GetMapping("/register")
    public String getRegisterPage() {
        return "site/register";
    }

    /**
     * 相应登陆页面
     */
    @GetMapping("/login")
    public String getLoginPage() {
        return "site/login";
    }


    /**
     * 处理注册表单提交
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        // map没有信息说明表单提交没有错误，跳转到登陆成功界面
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，以向您发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            // 注册信息有误，重新返回注册页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "site/register";
        }
    }

    /**
     * 响应激活链接
     * http://localhost:8080/community/activation/id/code
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int activation = userService.activation(userId, code);
        if (activation == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "您的账号已激活，可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (activation == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "当前账号已经激活过了，请不要重复激活。");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活击败，激活码不正确");
            model.addAttribute("target", "/index");
        }
        return "site/operate-result";
    }

    /**
     * 获取验证码
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // Session保存验证码
        // session.setAttribute("kaptcha", text);

        // 给用户生成验证码凭证
        String kaptchaOwner = CommunityUtil.generateUUID();
        // 凭证保存到Cookie里
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 获取Redis存储的Key并存储验证码
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 验证码生成的图片传给服务器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            log.error("验证码生成失败：" + e);
        }
    }

    @PostMapping("/login")
    public String login(String username, String password, String code, Model model, boolean remember/*,
                        HttpSession session*/, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {

        // 判断验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "site/login";
        }
        int expiredSeconds = remember ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        // 判断账号密码是否正确
        Map<String, Object> map = userService.login(username, password, expiredSeconds);

        // 登录成功
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:index";
        } else { //登陆失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    /**
     * 相应注销
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:index";
    }
}
