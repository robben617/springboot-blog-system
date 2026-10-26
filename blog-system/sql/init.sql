-- ============================================
-- 博客系统数据库初始化脚本（含建库建表 + 初始数据）
-- 使用方法：在MySQL中执行此脚本
--   mysql -u root -p --default-character-set=utf8mb4 < init.sql
-- 说明：脚本可重复执行（DROP TABLE IF EXISTS + 重建）；
--       建表后会自动写入演示用的初始数据（见文件末尾「初始数据」部分）。
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog_db;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（MD5加密存储）',
    `salt`        VARCHAR(32)  NOT NULL COMMENT '密码盐值',
    `nickname`    VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `email`       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `avatar`      VARCHAR(255)          DEFAULT NULL COMMENT '头像URL',
    `bio`         VARCHAR(300)          DEFAULT NULL COMMENT '个人简介',
    `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户 1-管理员',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用 1-启用',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 分类表
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序（值越小越靠前）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- ----------------------------
-- 标签表
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '标签名称',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ----------------------------
-- 文章表
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id`     BIGINT        NOT NULL COMMENT '作者ID',
    `category_id` BIGINT                 DEFAULT NULL COMMENT '分类ID',
    `title`       VARCHAR(200)  NOT NULL COMMENT '标题',
    `cover`       VARCHAR(255)           DEFAULT NULL COMMENT '封面图URL',
    `summary`     VARCHAR(500)           DEFAULT NULL COMMENT '文章摘要',
    `content`     TEXT          NOT NULL COMMENT '内容（Markdown）',
    `view_count`  INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-草稿 1-已发布',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- ----------------------------
-- 文章-标签关联表（多对多）
-- ----------------------------
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag` (
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id`     BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`article_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章-标签关联表';

-- ----------------------------
-- 评论表
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id`  BIGINT       NOT NULL COMMENT '文章ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论人ID',
    `parent_id`   BIGINT                DEFAULT NULL COMMENT '父评论ID（NULL=一级评论；非空=回复，指向同表一级评论的id，两级评论结构）',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ----------------------------
-- 公告表
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`       VARCHAR(200) NOT NULL COMMENT '标题',
    `content`     TEXT         NOT NULL COMMENT '内容',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ----------------------------
-- 友情链接表
-- ----------------------------
DROP TABLE IF EXISTS `friend_link`;
CREATE TABLE `friend_link` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '友链ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '网站名称',
    `url`         VARCHAR(255) NOT NULL COMMENT '网站地址',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='友情链接表';

-- ============================================
-- 初始数据（演示数据）
-- 说明：
--   1. 以下数据用于本地开发 / 演示 / 接口联调，可按需修改或删除；
--   2. 用户密码采用 MD5(盐 + 明文) 存储，与后端 PasswordUtil 算法一致，
--      此处直接使用 MySQL 内置 MD5(CONCAT(salt, password)) 生成密文；
--   3. 内置账号（明文密码）：
--        管理员   admin    / admin123
--        普通用户 zhangsan / abc123456
--        普通用户 lisi     / abc123456
--        普通用户 wangwu   / abc123456（账号状态为「禁用」，用于登录拦截联调）
-- ============================================

-- ----------------------------
-- 用户数据（4条：1管理员 + 3普通用户，其中 wangwu 为禁用状态）
-- ----------------------------
INSERT INTO `user` (`id`, `username`, `password`, `salt`, `nickname`, `email`, `avatar`, `bio`, `role`, `status`, `deleted`) VALUES
(1, 'admin',    MD5(CONCAT('a1b2c3d4e5f60718', 'admin123')),  'a1b2c3d4e5f60718', '系统管理员', 'admin@blog.com',    NULL, '博客系统超级管理员，负责站点运维与内容审核。',           1, 1, 0),
(2, 'zhangsan', MD5(CONCAT('b2c3d4e5f6071829', 'abc123456')), 'b2c3d4e5f6071829', '张三',       'zhangsan@blog.com', NULL, '后端开发工程师，专注 Java / Spring Boot 技术栈。',      0, 1, 0),
(3, 'lisi',     MD5(CONCAT('c3d4e5f60718293a', 'abc123456')), 'c3d4e5f60718293a', '李四',       'lisi@blog.com',     NULL, '前端爱好者，喜欢分享 Vue 与工程化实践。',              0, 1, 0),
(4, 'wangwu',   MD5(CONCAT('d4e5f60718293a4b', 'abc123456')), 'd4e5f60718293a4b', '王五',       'wangwu@blog.com',   NULL, '该账号因违规已被管理员禁用（用于禁用登录联调）。',      0, 0, 0);

-- ----------------------------
-- 分类数据（5条，按 sort 升序展示）
-- ----------------------------
INSERT INTO `category` (`id`, `name`, `sort`) VALUES
(1, '后端开发', 1),
(2, '前端开发', 2),
(3, '数据库',   3),
(4, '运维部署', 4),
(5, '随笔杂谈', 5);

-- ----------------------------
-- 标签数据（8条）
-- ----------------------------
INSERT INTO `tag` (`id`, `name`) VALUES
(1, 'Java'),
(2, 'Spring Boot'),
(3, 'MySQL'),
(4, 'Vue'),
(5, 'Redis'),
(6, 'Docker'),
(7, 'MyBatis'),
(8, '生活随笔');

-- ----------------------------
-- 文章数据（6条：5篇已发布 + 1篇草稿，cover 暂为 NULL 可通过封面上传接口补充）
-- ----------------------------
INSERT INTO `article` (`id`, `user_id`, `category_id`, `title`, `cover`, `summary`, `content`, `view_count`, `status`, `deleted`, `create_time`) VALUES
(1, 2, 1, 'Spring Boot 入门实战：从零搭建博客系统后端',
 NULL,
 '本文以一个真实的博客系统为例，介绍如何使用 Spring Boot 3 + MyBatis 快速搭建后端服务，涵盖分层架构、统一返回、登录拦截等内容。',
 '## 前言\n\nSpring Boot 让 Spring 应用的创建和开发变得非常简单。本文将带你从零搭建一个博客系统后端。\n\n## 一、项目初始化\n\n通过 Spring Initializr 创建项目，引入 Web、MyBatis、MySQL 驱动等依赖。\n\n## 二、分层架构\n\n采用 Controller / Service / Mapper 经典三层结构，配合统一返回体 Result。\n\n```java\n@RestController\n@RequestMapping(\"/api/articles\")\npublic class ArticleController { }\n```\n\n## 三、登录鉴权\n\n使用拦截器 + Token 校验登录态，未登录用户只能浏览公开内容。\n\n## 结语\n\n至此一个基础的博客后端就搭建完成了。',
 128, 1, 0, '2026-09-01 09:30:00'),
(2, 2, 3, 'MySQL 索引原理详解：B+树为什么这么快',
 NULL,
 '索引是数据库性能优化的核心。本文从 B+树数据结构讲起，分析聚簇索引、回表、最左前缀等概念，并给出实战优化建议。',
 '## 为什么需要索引\n\n全表扫描在数据量大时性能极差，索引可以将查询复杂度从 O(n) 降到 O(log n)。\n\n## B+树结构\n\nB+树是平衡多叉树，非叶子节点只存键值，叶子节点形成有序链表，非常适合磁盘 IO。\n\n## 聚簇索引与回表\n\n聚簇索引的叶子节点存储整行数据，二级索引叶子节点存主键，需要回表查询。\n\n## 最左前缀原则\n\n联合索引 (a,b,c) 可以用于 a、a,b、a,b,c 的查询条件。',
 256, 1, 0, '2026-09-05 14:20:00'),
(3, 1, 2, 'Vue3 组合式 API 开发指南',
 NULL,
 'Vue3 引入了组合式 API（Composition API），让逻辑复用与代码组织更加灵活。本文介绍 setup、ref、reactive 与组合式函数的用法。',
 '## 组合式 API 是什么\n\n组合式 API 是一组 API 的集合，让我们可以编写函数式的组件逻辑。\n\n## ref 与 reactive\n\nref 用于基本类型，reactive 用于对象类型。\n\n```js\nconst count = ref(0)\nconst state = reactive({ name: \"blog\" })\n```\n\n## 逻辑复用\n\n通过抽取 composable（如 useUser）实现跨组件逻辑复用。',
 89, 1, 0, '2026-09-08 10:05:00'),
(4, 3, 1, 'MyBatis 动态 SQL 使用技巧总结',
 NULL,
 '动态 SQL 是 MyBatis 最强大的特性之一。本文总结 if、choose、where、set、foreach 等常用标签的使用场景与注意事项。',
 '## if 标签\n\n根据条件拼接 WHERE 子句，常用于多条件搜索。\n\n## foreach 标签\n\n用于批量插入或 IN 查询。\n\n```xml\n<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">\n  #{id}\n</foreach>\n```\n\n## 注意事项\n\n注意使用 #{} 预编译防止 SQL 注入，${} 仅用于表名/列名等场景。',
 64, 1, 0, '2026-09-12 16:40:00'),
(5, 2, 4, '使用 Docker 一键部署 Spring Boot 应用（草稿）',
 NULL,
 '本篇记录将 Spring Boot 应用容器化并通过 Docker Compose 编排 MySQL 的完整过程，尚未写完。',
 '## 编写 Dockerfile\n\n（内容整理中……）',
 0, 0, 0, '2026-09-15 20:00:00'),
(6, 3, 5, '程序员的自我修养：如何保持持续学习',
 NULL,
 '技术更新飞快，持续学习是程序员的必修课。本文分享我在时间管理、信息源筛选与输出倒逼输入方面的一些经验。',
 '## 为什么要持续学习\n\n技术迭代速度快，停止学习就意味着落后。\n\n## 如何高效学习\n\n1. 以问题为导向；\n2. 建立知识体系；\n3. 输出倒逼输入，写博客是最好的学习方式之一。\n\n## 写在最后\n\n保持耐心，长期主义。',
 175, 1, 0, '2026-09-18 08:15:00');

-- ----------------------------
-- 文章-标签关联数据（多对多）
-- ----------------------------
INSERT INTO `article_tag` (`article_id`, `tag_id`) VALUES
(1, 1), (1, 2),                 -- 文章1：Java、Spring Boot
(2, 3),                         -- 文章2：MySQL
(3, 4),                         -- 文章3：Vue
(4, 1), (4, 7),                 -- 文章4：Java、MyBatis
(5, 6),                         -- 文章5：Docker
(6, 8);                         -- 文章6：生活随笔

-- ----------------------------
-- 评论数据（6条：含两级评论结构，parent_id 非空表示回复某条一级评论）
-- ----------------------------
INSERT INTO `comment` (`id`, `article_id`, `user_id`, `parent_id`, `content`, `deleted`, `create_time`) VALUES
(1, 1, 3, NULL, '写得非常清晰，跟着搭了一遍，已经跑起来了，感谢分享！', 0, '2026-09-02 11:00:00'),
(2, 1, 4, NULL, '请问示例代码有开源仓库地址吗？想学习一下。',           0, '2026-09-02 15:30:00'),
(3, 1, 2, 1,    '感谢支持！代码整理后会发到文章评论区。',             0, '2026-09-02 16:00:00'),
(4, 2, 1, NULL, '索引这部分讲得很透彻，回表和最左前缀终于搞懂了。',   0, '2026-09-06 09:10:00'),
(5, 3, 3, NULL, '收藏了，组合式函数那一节能不能再展开讲讲？',         0, '2026-09-09 13:25:00'),
(6, 2, 2, 4,    '谢谢站长肯定，后续会补充索引失效场景的文章。',       0, '2026-09-06 10:00:00');

-- ----------------------------
-- 公告数据（3条，前端按创建时间倒序展示，首页取最新一条）
-- ----------------------------
INSERT INTO `announcement` (`id`, `title`, `content`, `create_time`) VALUES
(1, '欢迎使用博客系统', '欢迎来到基于 Spring Boot + Vue 的博客系统！这里会持续分享后端、前端与数据库相关的技术文章，祝你阅读愉快。', '2026-09-01 08:00:00'),
(2, '系统升级维护公告', '本站将于本周六 00:00-02:00 进行系统升级维护，期间可能无法正常访问，请提前安排好你的阅读与写作时间。', '2026-09-10 18:00:00'),
(3, '社区评论规范', '请大家文明发言、理性讨论，禁止发布广告、人身攻击及违法违规内容，违规评论将被删除，情节严重者将被封禁账号。', '2026-09-12 12:00:00');

-- ----------------------------
-- 友情链接数据（4条，按 sort 升序展示）
-- ----------------------------
INSERT INTO `friend_link` (`id`, `name`, `url`, `sort`) VALUES
(1, 'Spring 官方', 'https://spring.io',       1),
(2, 'MySQL 官网',  'https://www.mysql.com',   2),
(3, 'Vue.js 官网', 'https://vuejs.org',       3),
(4, '开源中国',    'https://www.oschina.net', 4);

-- ============================================
-- 初始化完成
-- ============================================
