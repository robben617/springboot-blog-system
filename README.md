# 基于 Spring Boot 的博客系统

A full-stack blog system built with Spring Boot 3 + MyBatis-Plus + MySQL 8, featuring session-based authentication, article / category / tag management and threaded comments.

> 实训项目（第 7—9 周）· 前后端分离 · 8 人小组协作

---

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.5 · JDK 21 · MyBatis-Plus 3.5.5 · Maven 3.8 |
| 数据库 | MySQL 8（utf8mb4） |
| 前端 | Vue 3 · Node.js 22+（独立仓库 / 目录） |
| 鉴权 | Session + 拦截器（本项目明确不引入 Spring Security / JWT） |

## 功能模块

| 模块 | 功能点 | 状态 |
|---|---|---|
| 用户 | 注册、登录、登出、会话校验 | 已完成 |
| 用户 | 个人资料修改、头像上传、用户管理（管理员） | 待开发 |
| 文章 | 发布 / 编辑 / 删除、草稿、浏览量、分页查询 | 待开发 |
| 分类 | 分类增删改查、排序 | 待开发 |
| 标签 | 标签管理、文章标签多对多关联 | 待开发 |
| 评论 | 发表评论、回复（楼中楼，`parent_id` 自关联）、管理员删除 | 待开发 |
| 公告 | 公告发布与管理 | 待开发 |
| 友链 | 友情链接维护 | 待开发 |
| 公共 | 统一返回体、全局异常处理、跨域、分页、自动填充、拦截器 | 已完成 |

## 目录结构

```
├── blog-system/                     # 后端工程（Spring Boot）
│   ├── pom.xml
│   ├── sql/init.sql                 # 建库建表脚本（8 张表）
│   └── src/main/
│       ├── java/com/example/blog/
│       │   ├── common/              # 常量
│       │   ├── config/              # 跨域、MyBatis-Plus、MVC、自动填充
│       │   ├── controller/          # 控制器层
│       │   ├── dto/                 # 请求/响应对象
│       │   ├── entity/              # 实体
│       │   ├── exception/           # 业务异常 + 全局异常处理
│       │   ├── interceptor/         # 登录 / 管理员拦截器
│       │   ├── mapper/              # 数据访问层
│       │   ├── service/             # 业务层（含 impl）
│       │   └── util/                # 统一返回、分页、密码、文件上传
│       └── resources/application.yml
├── 需求分析说明书_修订版.docx
├── 数据库设计说明书.html             # 含 E-R 图、数据字典、关系说明
├── 博客系统E-R图.vsdx                # Visio 版 E-R 图
├── E-R图绘制修改说明.md
├── 基于SpringBoot的博客系统开发-7-9周小组分工方案.docx
└── 任务时间顺序与代码交接流程.docx
```

## 快速开始

1. **建库**：在 MySQL 中执行 `blog-system/sql/init.sql`（会创建 `blog_db` 与 8 张表）。
2. **改配置**：编辑 `blog-system/src/main/resources/application.yml`，填入自己的数据库账号密码与文件上传目录。
3. **启动**：

   ```bash
   cd blog-system
   mvn spring-boot:run
   ```

4. **验证**：访问 `GET /test/health` 应返回统一返回体的成功响应。

## 约定

- 所有接口统一返回 `Result<T>`，业务异常抛 `BusinessException` 由全局处理器兜底。
- 主键自增 BIGINT；时间字段由 MyBatis-Plus 自动填充；逻辑删除用 `deleted` 字段。
- 密码使用「MD5 + 随机盐」存储，禁止明文。

## 开发进度

当前处于第 7 周：后端基础框架与用户模块已落地，其余业务模块开发中。
