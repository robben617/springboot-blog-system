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
| 分类 | 分类增删改查（管理员）、按分类浏览、删除前校验 | 已完成 |
| 标签 | 标签增删改查（管理员）、按标签浏览、删除自动解绑文章 | 已完成 |
| 评论 | 发表评论、回复（两级结构，`parent_id` 自关联）、按文章展示、删除级联回复 | 已完成 |
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
│       │   ├── util/                # 统一返回、分页、密码、文件上传
│       │   └── vo/                  # 前端展示对象
│       └── resources/application.yml
├── 需求分析说明书_修订版.docx
├── 数据库设计说明书.html             # 含 E-R 图、数据字典、关系说明
├── 博客系统E-R图.vsdx                # Visio 版 E-R 图
├── E-R图绘制修改说明.md
├── 董浩-分类标签与评论模块-开发规划.md   # 接口清单与实施规划
├── 开发踩坑与避错记录.md              # 踩坑沉淀 + 提交前自查清单
├── 基于SpringBoot的博客系统开发-7-9周小组分工方案.docx
└── 任务时间顺序与代码交接流程.docx
```

## 接口速查（分类标签 / 评论模块）

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/categories` | 公开 | 分类列表 |
| GET | `/categories/{id}/articles` | 公开 | 按分类浏览文章（分页） |
| GET | `/tags` | 公开 | 标签列表 |
| GET | `/tags/{id}/articles` | 公开 | 按标签浏览文章（分页） |
| GET | `/comments/article/{articleId}` | 公开 | 评论展示（两级结构） |
| POST | `/comment` | 登录 | 发表评论 / 回复（带 parentId 即回复） |
| DELETE | `/comment/{id}` | 管理员或文章作者 | 删除评论（级联删除其回复） |
| POST / PUT / DELETE | `/admin/category`、`/admin/tag` | 管理员 | 分类、标签管理与删除校验 |

## 快速开始

1. **建库**：在 MySQL 中执行 `blog-system/sql/init.sql`（会创建 `blog_db` 与 8 张表）。
2. **改配置**：`application.yml` 中的数据库账号、密码、上传目录均为占位符配置，可用环境变量覆盖（推荐），或直接修改文件里的默认值：
   - `DB_USERNAME` / `DB_PASSWORD` / `DB_URL` —— 数据库连接
   - `FILE_UPLOAD_PATH` —— 文件上传目录（默认 `./upload`）
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

当前处于第 7 周：后端基础框架、用户模块、**分类标签模块、评论模块**已落地并通过编译与启动验证；文章、公告友链、文件上传模块开发中。
