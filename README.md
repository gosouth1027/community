# 仿牛客网论坛Web项目

项目描述：主要使用了SpringBoot和SSM框架，配合Mysql数据库完成，实现了登录、首页、发帖、评论、点赞、私信、头像上传等功能。

主要工作：1. 实现邮件激活注册方式，并对用户密码加密保存，登陆时加入验证码验证。2. 对登陆状态进行检查，实现了对不同用户的权限管理。3. 使用Kafka技术实现了对点赞、评论和关注产生的系统消息的通知功能。4. 使用Redis缓存频繁访问的数据，从而减少对数据库得直接访问，提高服务器性能。5. 使用Elasticsearch实现了全文搜索功能。6. 使用前缀树实现了敏感词过滤功能。
