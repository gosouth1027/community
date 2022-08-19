package com.sadness.community.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.sadness.community.annotation.LoginRequired;
import com.sadness.community.entity.User;
import com.sadness.community.service.FollowService;
import com.sadness.community.service.LikeService;
import com.sadness.community.service.UserService;
import com.sadness.community.util.CommunityConstant;
import com.sadness.community.util.CommunityUtil;
import com.sadness.community.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @version 1.0
 * @Date 2022/6/7 21:46
 * @Author SadAndBeautiful
 */
@Slf4j
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String upload;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    /**
     * 响应修改页面
     */
    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置相应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生产上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "site/setting";
    }

    /**
     * 更新头像的路径
     */
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        // 判断文件名是否为空
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空！");
        }
        // 修改当前用户的头像路径
        String headerUrl = headerBucketUrl + "/" + fileName;
        userService.updateUserHeaderUrl(hostHolder.getUser().getId(), headerUrl);
        return CommunityUtil.getJSONString(0);

    }

    /**
     * 处理头像上传
     * {！！！废弃！！！}
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeaderImage(MultipartFile headerImage, Model model, HttpServletResponse response) {
        // 判断上传的头像是否为空
        if (headerImage == null) {
            model.addAttribute("error", "上传头像不能为空！");
            return "site/setting";
        }
        // 或取上传文件的名字
        String originalFilename = headerImage.getOriginalFilename();
        model.addAttribute("originalFilename", originalFilename);
        // 取后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "上传头像格式不正确！");
            return "site/setting";
        }

        String fileName = CommunityUtil.generateUUID() + suffix;
        // 服务器存文件
        File dest = new File(upload + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("文件上传失败" + e.getMessage());
            throw new RuntimeException("文件上传失败，服务器发生异常");
        }

        // 更新当前头像的路径 http://localhost:8080/community/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        User user = hostHolder.getUser();
        userService.updateUserHeaderUrl(user.getId(), headerUrl);
        model.addAttribute("error", "头像上传成功！");
        return "redirect:/user/setting";
    }

    /**
     * 获取头像
     * {！！！废弃！！！}
     */
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        fileName = upload + "/" + fileName;
        try (
                ServletOutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
        ) {
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        } catch (IOException e) {
            log.error("读取头像失败" + e.getMessage());
        }
    }

    /**
     * 处理修改密码的请求，验证原密码是否正确，修改新的密码
     */
    @PostMapping("/update/password")
    public String updatePassword (String oldPassword, String newPassword, String confirmPassword, Model model) {
        // 获取用户
        User user = hostHolder.getUser();
        if (StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPasswordMsg", "请输入原密码！");
            return "/site/setting";
        }
        if (StringUtils.isBlank(newPassword)) {
            model.addAttribute("newPasswordMsg", "请输入新密码！");
            return "/site/setting";
        }
        if (StringUtils.isBlank(oldPassword)) {
            model.addAttribute("confirmPasswordMsg", "请确认密码！");
            return "/site/setting";
        }

        if (!user.getPassword().equals(CommunityUtil.md5(oldPassword + user.getSalt()))) {
            model.addAttribute("oldPasswordMsg", "原密码错误！");
            return "/site/setting";
        }

        model.addAttribute("oldPassword", oldPassword);
        model.addAttribute("newPassword", newPassword);

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordMsg", "两次输入密码不一致！");
            return "/site/setting";
        }

        userService.updatePassword(user.getId(), CommunityUtil.md5(newPassword + user.getSalt()));

        return "redirect:/login";
    }

    /**
     * 访问个人主页，传入user对象，传入获赞数量
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }

        model.addAttribute("user", user);
        // 获赞数量
        int userLikedCount = likeService.findUserLikedCount(userId);
        model.addAttribute("userLikedCount", userLikedCount);
        // 关注的数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝的数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否关注当前用户
        if (hostHolder.getUser()!= null) {
            boolean isFollow = followService.isFollow(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
            model.addAttribute("isFollow", isFollow);
        }
        return "/site/profile";
    }
}
