# 基于 Spring Boot 的博客系统 —— 项目文档

> 文档版本：v1.0（重建版）
> 适用实训：南京工程学院 大四实训（第 7–9 周）
> 架构形态：Spring Boot 3 + Vue 3 前后端分离
> 编制说明：本文档基于真实源码（`blog-system` 后端、`blog-frontend` 前端）逐项核对后重建，接口路径、表字段、类名均与代码一致。

---

## 0. 编制说明

本文档因原项目文档被误删后，基于真实源码（`blog-system` 后端、`blog-frontend` 前端）逐项核对重建，可作为教师评审用正式交付物。编制遵循以下原则：

1. **以代码为准**：所有接口路径、HTTP 方法、数据库表字段、实体类名、状态码均直接读取源码与 `sql/init.sql` 核对，凡源码中不存在的接口或字段一律不写。
2. **严格区分责任人**：用户董浩同学仅负责「分类标签模块（C1–C4）+ 评论模块（M1–M4）」，相关工作统一标注 **【董浩】**；其余工作统一标注 **【其他】**，全文不出现任何其他同学的具体姓名。
3. **口径一致**：数据库 8 张表、E-R 关系、逻辑删除范围、复合主键 `article_tag`、`Category.articleCount` 不落库等数据口径，均以董浩手绘 Chen 氏 E-R 图与源码实体类为准，与已核验事实基线保持一致。
4. **可复核**：文中涉及的统计数据（Java 类数/行数、前端文件数/行数、接口数、断言数）均来自对仓库的实际统计，非估算。

---

## 目录

1. 项目概述
2. 技术栈与选型说明
3. 系统架构
4. 功能需求与实现状态（30 个功能点）
5. 数据库设计（8 张表 + E-R 关系）
6. 接口清单（48 个业务接口）
7. 前端设计
8. 核心实现要点
9. 部署与运行
10. 测试与验证
11. 代码规模统计
12. 已知问题与改进建议
- 附录 A：环境变量与配置项速查
- 附录 B：常见报错对照表

---

## 1. 项目概述

### 1.1 项目背景

本系统为南京工程学院大四实训项目，由 8 人小组在第 7–9 周完成。系统采用**前后端分离**架构：后端基于 Spring Boot 3 + MyBatis-Plus 提供 RESTful 接口，前端基于 Vue 3 + Vite 实现交互界面，数据持久化使用 MySQL 8。系统定位为一个支持文章发布、分类标签、评论互动、公告友链与个人中心的完整博客平台。

### 1.2 项目目标

- 实现一个可运行、可演示的多用户博客系统；
- 覆盖用户、文章、分类、标签、评论、公告、友链、文件上传等核心模块；
- 通过 Session + 拦截器实现登录态与角色权限控制；
- 提供标准化统一返回体、全局异常处理、分页封装等工程化基础能力；
- 交付可被教师评审的完整项目文档与自测脚本。

### 1.3 项目范围

- **已实现模块**：用户（注册/登录/个人中心）、文章（发布/编辑/草稿/删除）、分类、标签、评论（含回复）、公告、友链、文件上传（头像/封面）。
- **后端 HTTP 端点**：共 **49 个** = **48 个业务接口** + 1 个 `/test/health` 健康检查。
- **前端页面**：**18 个 `.vue` 页面**（10 个前台页含 Admin 布局 + 8 个后台页），另含 2 个公共组件。

### 1.4 小组分工总览表

> 责任标注约定：**【董浩】** 表示用户董浩同学负责的工作；**【其他】** 表示小组其余成员负责的工作。本文档一律不出现具体同学姓名，仅以「其他」统称。

| 模块 | 子模块 | 责任标注 | 后端接口数 | 说明 |
|------|--------|----------|------------|------|
| 用户模块 | 注册/登录/退出/个人信息/改密 | 【其他】 | 7 | `U` 系列功能点 |
| 文章模块 | 列表/详情/编辑/我的/发布/草稿/封面上传 | 【其他】 | 9 | `A` 系列功能点 |
| 分类模块 | 增删改查 + 按分类浏览文章 | 【董浩】 | 5 | `C1`–`C4` |
| 标签模块 | 增删改查 + 按标签浏览文章 | 【董浩】 | 5 | `C1`–`C4`（与分类合并口径） |
| 评论模块 | 发表/回复/展示/删除 | 【董浩】 | 3 | `M1`–`M4`（展示接口含在 3 内） |
| 文件上传 | 头像/封面/静态访问 | 【其他】 | 1 | `F` 系列功能点 |
| 公告友链 | 公告管理 + 友链管理 | 【其他】 | 9 | `N` 系列功能点 |
| 后台概览 | 统计/用户管理 | 【其他】 | 7 | `N`/`H` 部分 |
| 测试 | 健康检查 | 【其他】 | 1 (`/test/health`) | 非业务接口 |
| **合计** | — | 董浩 13 / 其他 35 + health 1 | **49** | 业务接口 48 |

**董浩负责范围一句话总结**：分类标签模块（C1–C4，共 10 个接口）+ 评论模块（M1–M4，共 3 个接口）= **13 个接口 / 8 个功能点**。其余 35 个业务接口与 `/test/health` 均属「其他」同学负责范围。

---

## 2. 技术栈与选型说明

### 2.1 技术栈总表

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 | 应用容器、Web MVC、自动装配 |
| JDK | Java | 21 | 运行环境与编译目标 |
| 持久层 | MyBatis-Plus | 3.5.5 | ORM、分页、逻辑删除、条件构造器 |
| 数据库 | MySQL | 8（utf8mb4） | 关系型数据存储 |
| 构建 | Maven | 3.8 | 依赖管理与打包 |
| 前端框架 | Vue | 3.4 | 组件化视图层 |
| 构建工具 | Vite | 5 | 开发服务器与打包 |
| 状态管理 | Pinia | — | 全局状态（用户信息） |
| UI 组件 | Element Plus | — | 后台与表单组件 |
| HTTP 客户端 | Axios | — | 前端接口请求 |
| 运行环境 | Node.js | 22 | 前端开发与构建 |

### 2.2 鉴权技术选型：为何不引入 JWT / Spring Security / Redis

本项目**明确不引入** Spring Security、JWT、Redis，采用 **Session + 拦截器** 方案，理由如下：

1. **实训周期短、团队规模小**：8 人小组、第 7–9 周三周工期，Session 方案零额外依赖、上手快、调试直观，适合教学场景。
2. **无分布式部署需求**：系统为单体部署（单台应用 + 单库），Session 存于应用内存即可满足，引入 Redis 做 Session 共享属于过度设计。
3. **JWT 的收益不成立**：JWT 主要解决「无状态、跨服务」场景；本项目接口均由同一后端提供，Session Cookie 已足够，且 JWT 一旦签发难以即时吊销，对「禁用账号」「退出登录即时生效」等需求反而更麻烦。
4. **Spring Security 学习与配置成本高**：其过滤器链、权限表达式对实训同学不友好；用 `HandlerInterceptor` 自定义两个拦截器（登录、管理员）即可清晰表达「游客/登录用户/管理员」三级权限，代码可读性强、便于评审。
5. **安全性边界清晰**：所有写操作走登录拦截器，所有 `/admin/**` 走管理员拦截器，公开只读接口在 `WebMvcConfig` 白名单中显式放行，权限边界一目了然。

> 说明：该选型是工程权衡结果，非技术能力不足。若后续演进为多端/微服务，可平滑迁移到 JWT + Redis 方案。

---

## 3. 系统架构

### 3.1 分层架构说明

后端采用经典 **Controller → Service → Mapper** 三层结构，辅以 config / util / entity / dto / vo / exception / interceptor 等横切层：

```
请求 → WebMvcConfig(拦截器注册/静态资源)
     → LoginInterceptor(登录校验) / AdminInterceptor(管理员校验)
     → Controller(参数接收 + 返回封装)
     → Service / ServiceImpl(业务规则)
     → Mapper(MyBatis-Plus / 注解 SQL)
     → MySQL
统一返回：Result<T>；统一异常：GlobalExceptionHandler
```

各层职责：

| 层 | 包路径 | 职责 |
|----|--------|------|
| 控制层 | `controller` | 接收 HTTP 参数、调用 Service、封装 `Result` 返回 |
| 业务层 | `service` / `service.impl` | 业务规则、事务控制、权限判断 |
| 持久层 | `mapper` | 数据库访问（MyBatis-Plus 自带 + 自定义注解 SQL） |
| 实体层 | `entity` | 与数据库表一一对应（含 `@TableName`） |
| 传输层 | `dto` | 前端入参对象（如 `CategoryDTO`、`CommentDTO`） |
| 视图层 | `vo` | 出参对象（如 `CommentVO`、`ArticleBriefVO`） |
| 配置层 | `config` | CORS、MyBatis-Plus、拦截器注册、MetaObjectHandler |
| 工具层 | `util` | `Result`、`PageResult`、`PageUtils`、`PasswordUtil`、`FileUploadUtil` |
| 异常层 | `exception` | `BusinessException` + `GlobalExceptionHandler` |
| 拦截器 | `interceptor` | `LoginInterceptor`、`AdminInterceptor` |
| 常量 | `common` | `Constants`（状态码、角色、Session Key 等） |

### 3.2 请求链路（以"发表评论"为例）

```
浏览器(POST /api/comment, withCredentials)
  → Vite 代理(/api → http://localhost:9090, 去掉 /api 前缀)
  → LoginInterceptor.preHandle: 从 Session 取 userId, 无则 401
  → CommentController.addComment: 取 request.getAttribute("userId")
  → CommentService.addComment: 校验内容/文章/父评论, 存库
  → GlobalExceptionHandler(无异常则跳过)
  → Result.success("评论成功", vo) 返回
```

> 注意：前端 `baseURL: '/api'`，Vite 开发服务器将 `/api/**` 代理到后端 `9090` 并 `rewrite` 去掉 `/api` 前缀；因此后端实际收到的是无 `/api` 前缀的路径（如 `/comment`）。生产环境同样依赖反向代理完成该映射。

### 3.3 后端目录结构树

```
blog-system/
├── pom.xml                          # Maven 依赖配置
├── sql/
│   └── init.sql                     # 建库建表(8张表) + 演示数据
├── scripts/
│   ├── smoke-test.sh                # 一键冒烟测试(50项断言)
│   ├── smoke-test-extra.sh          # 扩展冒烟测试
│   └── api-console.html             # 网页版接口自测台
├── upload/                          # 上传文件落地目录(运行期生成)
├── src/main/java/com/example/blog/
│   ├── BlogApplication.java         # 启动类
│   ├── common/Constants.java
│   ├── config/                      # CorsConfig / MyBatisPlusConfig
│   │               / MyMetaObjectHandler / WebMvcConfig
│   ├── controller/                  # 13 个 Controller
│   ├── dto/                         # 入参对象(12 个)
│   ├── entity/                      # 实体(8 张表 + ArticleTag)
│   ├── exception/                   # BusinessException / GlobalExceptionHandler
│   ├── interceptor/                 # LoginInterceptor / AdminInterceptor
│   ├── mapper/                      # 9 个 Mapper
│   ├── service/                     # 接口(11) + impl(11)
│   ├── util/                        # Result / PageResult / PageUtils
│   │        / PasswordUtil / FileUploadUtil
│   └── vo/                          # 出参对象(11 个)
└── src/main/resources/
    ├── application.yml              # 数据源/文件上传/MyBatis-Plus 配置
    └── (mapper XML 未使用, 走注解/MP 自带)
```

### 3.4 前端目录结构树

```
blog-frontend/
├── index.html
├── package.json
├── vite.config.js                   # 端口5173 + /api、/upload 代理
└── src/
    ├── main.js                      # 入口: 注册 Pinia/路由/ElementPlus
    ├── App.vue                      # 根布局(导航/页脚/路由出口)
    ├── api/                         # article.js / user.js / admin.js / common.js
    ├── router/index.js              # 路由表 + 全局守卫
    ├── store/user.js                # Pinia 用户状态(含 localStorage 持久化)
    ├── utils/request.js             # Axios 实例 + 请求/响应拦截器
    ├── utils/markdown.js            # Markdown 渲染工具
    ├── components/                  # AppSidebar.vue / ArticleCard.vue(公共组件)
    ├── assets/main.css              # 全局样式
    └── views/                       # 10 前台页
        ├── Admin.vue                # 后台布局容器
        ├── Home / ArticleList / ArticleDetail / ArticleEdit
        ├── Login / Register / Profile / MyArticles / NotFound
        └── admin/                   # 8 后台页
            ├── AdminDashboard / AdminUsers / AdminArticles / AdminComments
            ├── AdminCategories / AdminTags / AdminAnnouncements / AdminFriendLinks
```

---

## 4. 功能需求与实现状态（30 个功能点）

> 责任标注：**【董浩】** / **【其他】**。功能点编号中 `C`=分类、`M`=评论 为董浩负责；`U`=用户、`A`=文章、`F`=文件、`N`=公告友链、`H`=首页搜索 为其他同学负责。

| 编号 | 功能点名称 | 责任标注 | 对应接口（节选） | 状态 |
|------|------------|----------|------------------|------|
| U1 | 用户注册 | 【其他】 | POST /register | 已实现 |
| U2 | 用户登录（Session） | 【其他】 | POST /login | 已实现 |
| U3 | 退出登录 | 【其他】 | POST /logout | 已实现 |
| U4 | 查看/获取当前用户信息 | 【其他】 | GET /user/info、GET /user/{id} | 已实现 |
| U5 | 修改个人资料 | 【其他】 | PUT /user/profile | 已实现 |
| U6 | 修改密码 | 【其他】 | PUT /user/password | 已实现 |
| A1 | 文章分页列表/搜索 | 【其他】 | GET /articles | 已实现 |
| A2 | 文章详情（浏览量+1） | 【其他】 | GET /articles/{id} | 已实现 |
| A3 | 编辑回显（不+浏览量） | 【其他】 | GET /article/{id}/edit | 已实现 |
| A4 | 我的文章（含草稿） | 【其他】 | GET /article/my | 已实现 |
| A5 | 新建文章 | 【其他】 | POST /article | 已实现 |
| A6 | 编辑文章 | 【其他】 | PUT /article/{id} | 已实现 |
| A7 | 删除文章（逻辑删除） | 【其他】 | DELETE /article/{id} | 已实现 |
| A8 | 发布/转为草稿 | 【其他】 | PUT /article/{id}/publish、PUT /article/{id}/draft | 已实现 |
| **C1** | **分类管理（增/删/改/列表）** | **【董浩】** | POST/PUT/DELETE /admin/category、GET /categories | **已实现** |
| **C2** | **标签管理（增/删/改/列表）** | **【董浩】** | POST/PUT/DELETE /admin/tag、GET /tags | **已实现** |
| **C3** | **按分类浏览文章** | **【董浩】** | GET /categories/{id}/articles | **已实现** |
| **C4** | **按标签浏览文章** | **【董浩】** | GET /tags/{id}/articles | **已实现** |
| **M1** | **发表一级评论** | **【董浩】** | POST /comment | **已实现** |
| **M2** | **回复评论（两级）** | **【董浩】** | POST /comment（带 parentId） | **已实现** |
| **M3** | **评论展示（两级树）** | **【董浩】** | GET /comments/article/{articleId} | **已实现** |
| **M4** | **删除评论（级联回复）** | **【董浩】** | DELETE /comment/{id} | **已实现** |
| F1 | 文章封面上传 | 【其他】 | POST /article/cover | 已实现 |
| F2 | 用户头像上传 | 【其他】 | POST /user/avatar | 已实现 |
| F3 | 上传文件静态访问 | 【其他】 | GET /upload/**（资源映射） | 已实现 |
| N1 | 公告管理（列表/最新/增删改） | 【其他】 | GET /announcements、GET /announcements/latest、POST/PUT/DELETE /admin/announcement | 已实现 |
| N2 | 友链管理（列表/增删改） | 【其他】 | GET /friend-links、POST/PUT/DELETE /admin/friend-link | 已实现 |
| H1 | 首页聚合展示（公告/分类/最新文章） | 【其他】 | 组合前端请求 | 已实现 |
| H2 | 文章搜索 | 【其他】 | GET /articles?keyword= | 已实现 |
| H3 | 分类/标签导航 | 【其他】 | GET /categories、GET /tags | 已实现 |

**董浩功能点合计**：C1–C4、M1–M4 = **8 个**，对应 **13 个后端接口**。
**其他功能点合计**：U6 + A8 + F3 + N2 + H3 = 22 个，对应其余 35 个业务接口。

---

## 5. 数据库设计

### 5.1 数据表总览（8 张表）

| 表名 | 中文 | 主外键/关联 | 逻辑删除 |
|------|------|-------------|----------|
| `user` | 用户表 | 主键 `id`；被 `article.user_id`、`comment.user_id` 引用 | 有（`deleted`） |
| `article` | 文章表 | 主键 `id`；`user_id`→user，`category_id`→category | 有（`deleted`） |
| `category` | 分类表 | 主键 `id`；被 `article.category_id` 引用 | 无 |
| `tag` | 标签表 | 主键 `id` | 无 |
| `article_tag` | 文章-标签关联 | 复合主键 `(article_id, tag_id)`；分别→article、tag | 无 |
| `comment` | 评论表 | 主键 `id`；`article_id`→article，`user_id`→user，`parent_id` 自关联 | 有（`deleted`） |
| `announcement` | 公告表 | 主键 `id` | 无 |
| `friend_link` | 友链表 | 主键 `id` | 无 |

> 逻辑删除字段 `deleted` **仅** `user`、`article`、`comment` 三张表存在，由 MyBatis-Plus 全局逻辑删除（`logic-delete-field: deleted`，值 1=删除/0=未删）自动生效；分类、标签、关联表、公告、友链为物理删除。

### 5.2 各表字段结构

#### 5.2.1 user（用户表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名（唯一） |
| password | VARCHAR(100) | NOT NULL | 密码（MD5+盐 加密） |
| salt | VARCHAR(32) | NOT NULL | 密码盐值 |
| nickname | VARCHAR(50) | NULL | 昵称 |
| email | VARCHAR(100) | NULL | 邮箱 |
| avatar | VARCHAR(255) | NULL | 头像URL |
| bio | VARCHAR(300) | NULL | 个人简介 |
| role | TINYINT | NOT NULL DEFAULT 0 | 0-普通用户 1-管理员 |
| status | TINYINT | NOT NULL DEFAULT 1 | **0-禁用 1-启用** |
| deleted | TINYINT | NOT NULL DEFAULT 0 | 逻辑删除 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

索引：`uk_username`（唯一）。

#### 5.2.2 category（分类表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 分类ID |
| name | VARCHAR(50) | NOT NULL | 分类名称 |
| sort | INT | NOT NULL DEFAULT 0 | 排序（值小靠前） |
| create_time | DATETIME | NOT NULL | 创建时间 |

> 注：实体类 `Category.articleCount` 标注 `@TableField(exist = false)`，**不落库**，仅用于前台分类入口展示该分类下已发布文章数。

#### 5.2.3 tag（标签表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 标签ID |
| name | VARCHAR(50) | NOT NULL, UNIQUE (`uk_tag_name`) | 标签名称（唯一） |
| create_time | DATETIME | NOT NULL | 创建时间 |

#### 5.2.4 article（文章表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 文章ID |
| user_id | BIGINT | NOT NULL | 作者ID（→user.id） |
| category_id | BIGINT | NULL | 分类ID（→category.id，可空） |
| title | VARCHAR(200) | NOT NULL | 标题 |
| cover | VARCHAR(255) | NULL | 封面图URL |
| summary | VARCHAR(500) | NULL | 摘要 |
| content | TEXT | NOT NULL | 内容（Markdown） |
| view_count | INT | NOT NULL DEFAULT 0 | 浏览量 |
| status | TINYINT | NOT NULL DEFAULT 1 | 0-草稿 1-已发布 |
| deleted | TINYINT | NOT NULL DEFAULT 0 | 逻辑删除 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

索引：`idx_user_id`、`idx_category_id`、`idx_status`。

#### 5.2.5 article_tag（文章-标签关联表，多对多中间表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| article_id | BIGINT | PK（复合） | 文章ID（→article.id） |
| tag_id | BIGINT | PK（复合） | 标签ID（→tag.id） |

索引：主键 `(article_id, tag_id)`；`idx_tag_id`。

> **数据口径（以 E-R 图为准）**：该表拥有独立实体类 `ArticleTag`，使用**复合主键** `(article_id, tag_id)`，**没有自增 ID**；因复合主键不继承 MyBatis-Plus 的 `BaseMapper`，统一走 `ArticleTagMapper` 中的自定义 SQL（`insert`、`deleteByArticleId`、`deleteByTagId`、`selectTagIdsByArticleId`）。

#### 5.2.6 comment（评论表，两级自关联）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 评论ID |
| article_id | BIGINT | NOT NULL | 文章ID（→article.id） |
| user_id | BIGINT | NOT NULL | 评论人ID（→user.id） |
| parent_id | BIGINT | NULL | 父评论ID；NULL=一级评论，非空=回复（指向同表一级评论 id） |
| content | VARCHAR(500) | NOT NULL | 评论内容（≤500字） |
| deleted | TINYINT | NOT NULL DEFAULT 0 | 逻辑删除 |
| create_time | DATETIME | NOT NULL | 创建时间 |

索引：`idx_article_id`、`idx_parent_id`。

> 两级评论：`parent_id` 为 NULL 表示直接评论文章的一级评论；非空表示对某条一级评论的回复，**回复不支持再嵌套**（写入时已保证仅两级，历史脏数据在内存组装时归并）。

#### 5.2.7 announcement（公告表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 公告ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| content | TEXT | NOT NULL | 内容 |
| create_time | DATETIME | NOT NULL | 创建时间 |

#### 5.2.8 friend_link（友链表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 友链ID |
| name | VARCHAR(50) | NOT NULL | 网站名称 |
| url | VARCHAR(255) | NOT NULL | 网站地址（http/https） |
| sort | INT | NOT NULL DEFAULT 0 | 排序 |
| create_time | DATETIME | NOT NULL | 创建时间 |

### 5.3 实体关系（Chen 氏表示法口径）

以董浩手绘 E-R 图为准，采用 Chen 氏表示法（矩形=实体，菱形=关系，椭圆=属性，连线标注基数）：

```
 ┌────────┐          ┌──────────────┐          ┌────────┐
 │  USER  │──写───▶(发表)──▶│   ARTICLE    │◀──属于──(归类)──│ CATEGORY│
 │ (用户) │  1   N  │  (文章)       │   N   1  │ (分类)  │
 └────────┘          └──────┬───────┘          └────────┘
      │                     │
      │  1            N     │ N
      ▼  (发表)      (拥有)  ▼
 ┌────────┐◀────────────(标记)─────────────▶┌──────┐
 │ COMMENT│                                  │ TAG  │
 │ (评论) │◀──自关联(回复)── 1 : N ──────────│(标签)│
 └────────┘                                  └──────┘
      ▲                                         ▲
      │              (多对多中间表)              │
      └────────── ARTICLE_TAG ───────────────────┘
                 (article_id, tag_id)

 ┌────────────┐      ┌─────────────┐
 │ ANNOUNCEMENT│      │ FRIEND_LINK │   （独立实体，无外键关系）
 │  (公告)     │      │  (友链)     │
 └────────────┘      └─────────────┘
```

基数关系说明：

- **USER 1 — N ARTICLE**：一个用户可写多篇文章（`article.user_id`）。
- **CATEGORY 1 — N ARTICLE**：一个分类下可有多篇文章（`article.category_id`，允许为 NULL）。
- **ARTICLE N — N TAG**：通过 `article_tag` 中间表实现多对多；该表为独立实体、复合主键。
- **ARTICLE 1 — N COMMENT**：一篇文章可有多条评论（`comment.article_id`）。
- **USER 1 — N COMMENT**：一个用户可发多条评论（`comment.user_id`）。
- **COMMENT 1 — N COMMENT（自关联）**：父评论（一级）1 对多 回复，但限制为两级。
- **ANNOUNCEMENT / FRIEND_LINK**：独立实体，无外键约束，仅前台展示。

---

## 6. 接口清单（48 个业务接口）

> 权限图例：**公开**=游客可访问（在 `WebMvcConfig` 白名单放行）；**登录**=需登录（`LoginInterceptor` 校验 Session）；**管理员**=`/admin/**` 路径（`AdminInterceptor` 校验 `role=1`）。
> 责任标注：**【董浩】** / **【其他】**。

### 6.1 分类标签评论模块（董浩，13 个）

| 方法 | 路径 | 权限 | 需求说明 | 责任标注 |
|------|------|------|----------|----------|
| GET | `/categories` | 公开 | 分类列表（含各分类已发布文章数） | 【董浩】 |
| GET | `/categories/{id}/articles` | 公开 | 按分类浏览文章（分页，仅已发布） | 【董浩】 |
| POST | `/admin/category` | 管理员 | 新增分类（重名校验） | 【董浩】 |
| PUT | `/admin/category/{id}` | 管理员 | 修改分类（重名校验） | 【董浩】 |
| DELETE | `/admin/category/{id}` | 管理员 | 删除分类（分类下有关联文章则拒绝） | 【董浩】 |
| GET | `/tags` | 公开 | 标签列表（按名称升序） | 【董浩】 |
| GET | `/tags/{id}/articles` | 公开 | 按标签浏览文章（分页，仅已发布） | 【董浩】 |
| POST | `/admin/tag` | 管理员 | 新增标签（重名校验） | 【董浩】 |
| PUT | `/admin/tag/{id}` | 管理员 | 修改标签（重名校验） | 【董浩】 |
| DELETE | `/admin/tag/{id}` | 管理员 | 删除标签（自动解除与文章关联） | 【董浩】 |
| GET | `/comments/article/{articleId}` | 公开 | 评论展示（两级树结构） | 【董浩】 |
| POST | `/comment` | 登录 | 发表评论 / 回复评论（parentId 非空为回复） | 【董浩】 |
| DELETE | `/comment/{id}` | 登录 | 删除评论（作者或管理员，级联删回复） | 【董浩】 |

> 注：管理员评论列表接口 `GET /admin/comments` 虽属评论管理视图，但其控制器 `AdminCommentController` 归"其他"同学维护，不计入董浩 13 个接口。

### 6.2 用户模块（其他，7 个）

| 方法 | 路径 | 权限 | 需求说明 | 责任标注 |
|------|------|------|----------|----------|
| POST | `/register` | 公开 | 用户注册（密码≥6位，MD5+盐） | 【其他】 |
| POST | `/login` | 公开 | 用户登录（写入 Session） | 【其他】 |
| POST | `/logout` | 登录 | 退出登录（Session 失效） | 【其他】 |
| GET | `/user/info` | 登录 | 获取当前登录用户信息 | 【其他】 |
| GET | `/user/{id}` | 登录 | 按ID查询用户 | 【其他】 |
| PUT | `/user/profile` | 登录 | 修改个人资料 | 【其他】 |
| PUT | `/user/password` | 登录 | 修改密码 | 【其他】 |

### 6.3 文章模块（其他，9 个）

| 方法 | 路径 | 权限 | 需求说明 | 责任标注 |
|------|------|------|----------|----------|
| GET | `/articles` | 公开 | 文章分页列表/搜索（仅已发布） | 【其他】 |
| GET | `/articles/{id}` | 公开 | 文章详情（浏览量+1；草稿仅作者/管理员可见） | 【其他】 |
| GET | `/article/{id}/edit` | 登录 | 编辑回显（不+浏览量） | 【其他】 |
| GET | `/article/my` | 登录 | 我的文章（含草稿） | 【其他】 |
| POST | `/article` | 登录 | 新建文章（默认草稿，可直发） | 【其他】 |
| PUT | `/article/{id}` | 登录 | 编辑文章（仅作者/管理员） | 【其他】 |
| DELETE | `/article/{id}` | 登录 | 删除文章（逻辑删除，仅作者/管理员） | 【其他】 |
| PUT | `/article/{id}/publish` | 登录 | 发布（草稿→已发布） | 【其他】 |
| PUT | `/article/{id}/draft` | 登录 | 转为草稿（已发布→草稿） | 【其他】 |

### 6.4 文件上传模块（其他，1 个）

| 方法 | 路径 | 权限 | 需求说明 | 责任标注 |
|------|------|------|----------|----------|
| POST | `/article/cover` | 登录 | 上传文章封面图（≤5MB，jpg/jpeg/png/gif/webp） | 【其他】 |

> 头像上传 `POST /user/avatar`（≤2MB）归在用户模块（其他）。静态访问 `GET /upload/**` 由 `WebMvcConfig` 资源映射提供，不计为业务接口。

### 6.5 公告友链模块（其他，9 个）

| 方法 | 路径 | 权限 | 需求说明 | 责任标注 |
|------|------|------|----------|----------|
| GET | `/announcements` | 公开 | 公告列表 | 【其他】 |
| GET | `/announcements/latest` | 公开 | 最新公告（首页，limit 可配） | 【其他】 |
| POST | `/admin/announcement` | 管理员 | 新增公告 | 【其他】 |
| PUT | `/admin/announcement/{id}` | 管理员 | 修改公告 | 【其他】 |
| DELETE | `/admin/announcement/{id}` | 管理员 | 删除公告 | 【其他】 |
| GET | `/friend-links` | 公开 | 友链列表 | 【其他】 |
| POST | `/admin/friend-link` | 管理员 | 新增友链 | 【其他】 |
| PUT | `/admin/friend-link/{id}` | 管理员 | 修改友链 | 【其他】 |
| DELETE | `/admin/friend-link/{id}` | 管理员 | 删除友链 | 【其他】 |

### 6.6 后台管理模块（其他，9 个）

| 方法 | 路径 | 权限 | 需求说明 | 责任标注 |
|------|------|------|----------|----------|
| GET | `/admin/stats` | 管理员 | 后台概览统计 | 【其他】 |
| GET | `/admin/users` | 管理员 | 用户分页列表/搜索 | 【其他】 |
| PUT | `/admin/user/{id}/status` | 管理员 | 启用/禁用用户（0禁用1启用） | 【其他】 |
| DELETE | `/admin/user/{id}` | 管理员 | 删除用户（逻辑删除，级联文章/评论） | 【其他】 |
| PUT | `/admin/user/{id}/reset-password` | 管理员 | 重置密码为初始密码 | 【其他】 |
| GET | `/admin/articles` | 管理员 | 管理员文章列表/搜索（含草稿） | 【其他】 |
| GET | `/admin/comments` | 管理员 | 评论管理分页/搜索（含回复平铺） | 【其他】 |
| GET | `/test/health` | 公开 | 健康检查（非业务接口，单独计） | 【其他】 |

> 业务接口小计：董浩 13 + 其他 35 = **48**。叠加 `/test/health` 后 HTTP 端点总数为 **49**。

### 6.7 统一返回与状态码

所有接口统一返回 `Result<T>`：

```json
{ "code": 200, "msg": "操作成功", "data": {} }
```

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 业务正常 |
| 400 | 参数有误 | 名称为空/超长/重复、分类下有关联文章禁止删除等 |
| 401 | 未登录 | `LoginInterceptor` 直接返回 HTTP 401 |
| 403 | 无权限 | 非管理员访问 `/admin/**`、非作者非管理员删评论 |
| 404 | 资源不存在 | 分类/标签/文章/评论不存在（`NoHandlerFoundException` 也映射到此） |
| 405 | 方法不支持 | 请求方法不匹配 |
| 413 | 文件过大 | `MaxUploadSizeExceededException` |
| 500 | 系统错误 | 未捕获异常兜底 |

> 业务异常（`BusinessException`）由 `GlobalExceptionHandler` 捕获，HTTP 状态恒为 200，错误体现在 `body.code`；仅拦截器拒绝（401/403）与框架级异常（404/405/413）返回真实 HTTP 状态。

---

## 7. 前端设计

### 7.1 页面清单（18 个 .vue + 2 公共组件）

**前台页（10 个，含 Admin 布局）**：

| 页面 | 路由 | 说明 |
|------|------|------|
| `App.vue` | 根布局 | 顶部导航 + 页脚 + 路由出口（公共） |
| `Home.vue` | `/` | 首页（公告/分类/最新文章聚合） |
| `ArticleList.vue` | `/articles` | 文章列表/搜索 |
| `ArticleDetail.vue` | `/articles/:id` | 文章详情 + 评论区 |
| `ArticleEdit.vue` | `/article/new`、`/article/:id/edit` | 写文章/编辑 |
| `Login.vue` | `/login` | 登录 |
| `Register.vue` | `/register` | 注册 |
| `Profile.vue` | `/profile` | 个人中心 |
| `MyArticles.vue` | `/my/articles` | 我的文章 |
| `NotFound.vue` | `*` | 404 页面 |

**后台页（8 个，嵌套于 `Admin.vue`）**：

| 页面 | 路由 | 说明 |
|------|------|------|
| `admin/AdminDashboard.vue` | `/admin/dashboard` | 概览统计 |
| `admin/AdminUsers.vue` | `/admin/users` | 用户管理 |
| `admin/AdminArticles.vue` | `/admin/articles` | 文章管理 |
| `admin/AdminComments.vue` | `/admin/comments` | 评论管理 |
| `admin/AdminCategories.vue` | `/admin/categories` | 分类管理（董浩模块） |
| `admin/AdminTags.vue` | `/admin/tags` | 标签管理（董浩模块） |
| `admin/AdminAnnouncements.vue` | `/admin/announcements` | 公告管理 |
| `admin/AdminFriendLinks.vue` | `/admin/friend-links` | 友链管理 |

**公共组件（2 个）**：

| 组件 | 说明 |
|------|------|
| `components/AppSidebar.vue` | 后台侧边栏导航 |
| `components/ArticleCard.vue` | 文章列表卡片（前台复用） |

> 董浩负责的模块在前端由 `AdminCategories.vue`、`AdminTags.vue` 与 `ArticleDetail.vue` 中的评论区共同体现；其余页面由其他同学负责。

### 7.2 路由结构（`router/index.js`）

- 采用 `createWebHistory()` 历史模式；路由按 `meta` 声明权限：`requiresAuth`（需登录）、`requiresAdmin`（需管理员）、`guestOnly`（仅游客）。
- 后台路由 `/admin` 为父路由，声明 `requiresAuth + requiresAdmin`，8 个子路由继承权限。
- **全局前置守卫**（`beforeEach`）逻辑：
  1. 需管理员 → 未登录跳 `/login`；已登录但 `role !== 1` 跳首页；
  2. 需登录 → 未登录跳 `/login`（携带 `redirect`）；
  3. `guestOnly` 且已登录 → 跳首页；
  4. 其余放行。
- **后置守卫**（`afterEach`）滚动到顶部。

### 7.3 状态管理（`store/user.js`，Pinia）

- 状态：`userInfo`（id/username/nickname/email/avatar/bio/role/status）、`isLoggedIn`。
- Getter：`isAdmin`（`role === 1`）、`userId`。
- 关键 Action：`login`、`register`、`logout`、`fetchUserInfo`、`updateProfile`、`changePassword`、`uploadAvatar`、`saveToStorage`/`restoreFromStorage`（localStorage 持久化，刷新不丢登录态）。
- 初始角色默认 `role: 0`，`status: 1`。

### 7.4 API 封装（`src/api/`）

按模块拆分为 4 个文件，统一调用 `utils/request`：

- `article.js`（`articleApi`）：文章列表/详情/编辑回显/我的/增删改/发布草稿/封面上传。
- `user.js`（`userApi`）：注册/登录/退出/信息/统计/资料/密码/头像。
- `admin.js`（`adminApi`）：后台统计、用户管理、文章管理、评论管理、分类、标签、公告、友链（董浩模块的增删改亦在此封装）。
- `common.js`（`commonApi`）：分类/标签/评论/公告/友链的公开只读接口（含按分类/标签查文章、评论列表、发评论、删评论）。

> 示例（`common.js` 中董浩模块封装）：

```js
// 分类列表（公开）
getCategories() { return request({ url: '/categories', method: 'get' }) },
// 按分类浏览文章
getCategoryArticles(categoryId, params) {
  return request({ url: `/categories/${categoryId}/articles`, method: 'get', params })
},
// 发表评论
createComment(data) { return request({ url: '/comment', method: 'post', data }) }
```

### 7.5 请求/响应拦截器（`utils/request.js`）

- **Axios 实例**：`baseURL: '/api'`、`timeout: 15000`、`withCredentials: true`（携带 Session Cookie）。
- **请求拦截器**：补全 `Content-Type: application/json`（未指定时）。
- **响应拦截器**：
  - `blob` 类型直接返回（文件下载场景）；
  - `code === 200` → 返回 `res.data`；
  - `code === 401` → 清空本地用户态、提示并跳 `/login`；
  - `code === 403` → 提示无权限；
  - `code === 404` → 提示资源不存在；
  - 其他业务错误 → `ElMessage` 提示；
  - HTTP 层错误（无响应 data）：按 `error.response.status` 分别提示 401/403/404/413/500；网络失败提示"网络连接失败"。

---

## 8. 核心实现要点

### 8.1 两级评论归并与级联删除（董浩，评论模块）

`CommentServiceImpl` 实现两级评论：

- **归并策略**（`addComment`）：若 `parentId` 指向的父评论本身也是回复，则自动归并到其所属一级评论（`parentId = parent.getParentId() == null ? parent.getId() : parent.getParentId()`），保证**永远只有两级**。
- **内存组装**（`buildTwoLevelTree`）：一次 SQL（JOIN 评论人信息）取回扁平列表，用 `LinkedHashMap` 按 `parentId` 组装成「一级 + replies」结构；对历史脏数据（父评论已删/父评论也是回复）做防御性归并，避免出现三级或评论丢失。
- **级联删除**（`deleteComment`，`@Transactional`）：删除评论时先删其下回复（`parent_id = 评论ID`），再删自身，二者均走逻辑删除（`deleted=1`），保证回复不残留。
- **删除权限**：管理员可删全站任意评论；文章作者可删自己文章下的评论；其余人返回 403。

```java
// CommentServiceImpl.java（节选）
remove(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId)); // 先删回复
removeById(commentId);                                                        // 再删自身
```

### 8.2 分类删除前校验（董浩）

`CategoryServiceImpl.deleteCategory`：删除前统计该分类下文章数（`baseMapper.countArticles(id)`），若 `> 0` 则抛出 `BusinessException(400, "该分类下还有 N 篇文章，无法删除…")`，必须先将文章移到其他分类或删除文章后才能删分类。

### 8.3 标签删除自动解绑（董浩）

`TagServiceImpl.deleteTag`（`@Transactional`）：先调用 `articleTagMapper.deleteByTagId(id)` 解除该标签与所有文章的关联，再 `removeById(id)` 删除标签本身。**文章不受影响**，两步同事务，避免解绑成功而删标签失败造成脏数据。

### 8.4 逻辑删除

- 仅 `user`/`article`/`comment` 三表有 `deleted` 字段；`application.yml` 配置 MyBatis-Plus 全局逻辑删除（`logic-delete-field: deleted`，值 `1/0`）。
- 查询自动附加 `deleted = 0`；`removeById`/`remove` 自动转为 `UPDATE ... SET deleted=1`，物理行不删除，便于审计与恢复。

### 8.5 分页封装（PageResult / PageUtils）

- `PageResult<T>`：通用分页结果（`pageNum/pageSize/total/pages/list`）。
- `PageUtils`：
  - `normalizePageNum`：空或 `<1` → 默认 1；
  - `normalizePageSize`：空或 `<1` → 默认 10；超过上限 100 截断；
  - `convert(IPage)`：MyBatis-Plus 分页对象 → `PageResult`。

```java
// PageUtils.java（节选）
public static long normalizePageSize(Long pageSize) {
    if (pageSize == null || pageSize < 1) return Constants.DEFAULT_PAGE_SIZE;
    return Math.min(pageSize, MAX_PAGE_SIZE); // MAX_PAGE_SIZE = 100
}
```

### 8.6 统一返回体 Result

`Result<T>` 提供 `success(...)` / `error(...)` 静态工厂，固定三字段 `code/msg/data`，`code` 取值见第 6.7 节。全局通过 `GlobalExceptionHandler` 统一兜底。

### 8.7 全局异常处理

`GlobalExceptionHandler`（`@RestControllerAdvice`）捕获：

- `BusinessException` → 返回其 `code/msg`；
- `MethodArgumentNotValidException` / `BindException` → 400，聚合字段错误；
- `HttpRequestMethodNotSupportedException` → 405；
- `NoHandlerFoundException` → 404（需 `throw-exception-if-no-handler-found: true`，已在 yml 开启）；
- `MaxUploadSizeExceededException` → 413；
- `IllegalArgumentException` → 400；
- `NullPointerException` / `Exception` → 500（"系统异常，请联系管理员"）。

### 8.8 文件上传

- `FileController.uploadCover`（文章封面，≤5MB）与 `UserManageServiceImpl.uploadAvatar`（头像，≤2MB）共用 `FileUploadUtil`。
- 工具类校验：非空、大小（头像 2MB / 封面 5MB，Spring 硬上限 5MB）、扩展名（jpg/jpeg/png/gif/webp）。
- 文件按日期分目录 `/upload/yyyy/MM/dd/` 存储，返回相对路径 `/upload/yyyy/MM/dd/uuid.ext`。
- 关键坑（已在代码注释固化）：Spring Boot 3（Servlet 6）下 `MultipartFile.transferTo` 遇相对路径会解析到容器临时目录，因此上传前用 `Paths.get(uploadPath).toAbsolutePath().normalize()` 转绝对路径。

### 8.9 Session 鉴权链路

- `LoginInterceptor` 拦截 `/**`，白名单（公开接口）在 `WebMvcConfig` 显式排除；未登录返回 401 并写入 `Result.error(401, "未登录，请先登录")`，同时将 `userId/username/role` 写入 `request` 属性供 Controller 使用。
- `AdminInterceptor` 仅拦截 `/admin/**`，`role != 1` 返回 403。
- 登录成功后 `UserController.login` 将 `userId/username/role` 存入 `HttpSession`（键名见 `Constants`：`userId`/`username`/`role`），`session.timeout: 30m`。
- 公开详情接口 `GET /articles/{id}` 不走登录拦截器，改从 `request.getSession(false)` 直接读登录态以判定"草稿仅作者/管理员可见"。
- 预检 `OPTIONS` 请求在 `LoginInterceptor` 直接放行，避免跨域被拦。

---

## 9. 部署与运行

### 9.1 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 21 | `java -version` 应显示 21.x |
| MySQL | 8 | 服务须处于运行状态，字符集 utf8mb4 |
| Node.js | 22 | 前端开发/构建 |
| Maven | 3.8 | 后端打包（IDEA 自带亦可） |

### 9.2 建库

在 `blog-system` 根目录（与 `pom.xml` 同级）执行：

```bash
mysql -u root -p --default-character-set=utf8mb4 < sql/init.sql
```

脚本会创建数据库 `blog_db`（8 张表）并写入演示数据（4 用户 / 5 分类 / 8 标签 / 6 文章 / 关联 / 6 评论 / 3 公告 / 4 友链）。

> 演示账号（来自 `init.sql`，明文密码）：`admin`/`admin123`（管理员）、`zhangsan`/`abc123456`、`lisi`/`abc123456`（普通用户）、`wangwu`/`abc123456`（**禁用**，用于登录拦截联调）。

### 9.3 环境变量（必设，密码不写死代码）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `root` | **数据库密码（最重要，漏设必然满屏 500）** |
| `DB_URL` | `jdbc:mysql://localhost:3306/blog_db?...` | 数据库连接串 |
| `FILE_UPLOAD_PATH` | `./upload` | 上传文件存储目录（相对/绝对均可） |

设置方式（任选其一）：IDEA Run Configuration 的 Environment variables；CMD `set DB_PASSWORD=xxx`；Git Bash `export DB_PASSWORD=xxx`。**注意 CMD 用 `set`、Git Bash 用 `export`，且只对当前窗口有效，改完需重启应用。**

### 9.4 后端启动

- IDEA：运行 `BlogApplication.java`，出现 `Started BlogApplication in x.xxx seconds` 即成功（端口 **9090**）。
- 命令行：`mvn package` 后 `java -jar target/blog-system-1.0.0.jar --server.port=9090`。

### 9.5 前端启动

```bash
cd blog-frontend
npm install
npm run dev      # 开发服务器，端口 5173
```

### 9.6 端口与代理

- 后端：**9090**；前端：**5173**。
- `vite.config.js` 代理：

```js
server: {
  port: 5173,
  proxy: {
    '/api':    { target: 'http://localhost:9090', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') },
    '/upload': { target: 'http://localhost:9090', changeOrigin: true }  // 头像/封面图片代理
  }
}
```

- 前端 `baseURL: '/api'`，经代理去掉 `/api` 前缀转发到后端；上传图片返回 `/upload/...` 同样经代理命中后端静态资源映射。

### 9.7 验证是否跑通

1. `http://localhost:9090/test/health` → `{"code":200,...}` 说明进程已起（**health 不查库**）。
2. `http://localhost:9090/categories` → 返回 `data:[...]` 说明数据库已连通；若返回 `code:500` 说明数据库未连上（先查 `DB_PASSWORD`）。

---

## 10. 测试与验证

### 10.1 自测工具

- **`scripts/smoke-test.sh`**：一键冒烟测试脚本，自动完成"启动产物 → **50 项接口断言** → 关停"，覆盖登录、分类/标签增删改查与重名/越权/空值校验、评论发表/回复/两级归并/级联删除、分类删除前校验、标签删除解绑等核心路径；断言函数 `ck()` 输出 `[PASS]/[FAIL]`，结尾汇总通过数。
- **`scripts/api-console.html`**：网页版接口自测台，可在浏览器点击测试登录、查分类标签、发评论等，并提供"一键诊断"（判断应用是否起来、数据库是否连通）。访问地址：`http://localhost:9090/upload/api-console.html`（若文件不存在，将 `scripts/api-console.html` 复制到 `blog-system/upload/` 再访问）。

### 10.2 已验证项（覆盖董浩模块与关键链路）

- 分类：列表公开可读、按分类查文章（仅已发布）、分页 `total` 正确、重复/空名被拒、普通用户越权被拦（403）、有文章的分类禁止删除、空分类可删。
- 标签：列表公开可读、按标签查文章（M:N 生效）、重复名被拒、删除标签后关联解除（`article_tag` 清零）、文章不被连带删除。
- 评论：未登录不能评论（401）、发表一级/回复评论、回复的回复自动归并为一级、`parentId` 正确、空/超长（>500）被拒、跨文章回复被拒、公开可读且含 `replies` 两级结构、作者/管理员可删、删除级联其回复（库内 `deleted=1`）、无关用户删他人评论被拒（403）。
- 通用：Session 登录态、管理员拦截、统一返回与状态码。

### 10.3 未覆盖项 / 局限

- **无自动化单元测试**：项目未引入 JUnit 业务层单测（`spring-boot-starter-test` 仅声明未实际编写用例），回归依赖 `smoke-test.sh` 的集成式 HTTP 断言，覆盖广度有限。
- 前端未做组件级测试与 E2E 测试。
- 并发场景（如同一分类并发删除/统计）未做压力与竞态验证。
- 密码强度、越权边角（如修改他人资料）、XSS/注入防御仅做了基础校验，未做安全审计。

---

## 11. 代码规模统计

> 统计口径：`blog-system/src/main/java` 下全部 `.java`；`blog-frontend/src` 下全部源文件（`.vue`/`.js`/`.css`）。

### 11.1 后端

| 指标 | 数值 |
|------|------|
| Java 类总数 | **83** |
| Java 代码总行数 | **5228** |
| Controller 数 / 行数 | 13 / 1005 |
| Service 接口 / 实现 | 11 / 11（impl 1561 行，接口 2012 含接口+impl 合计口径） |
| Mapper 数 / 行数 | 9 / 429 |
| Entity 数 / 行数 | 9 / 332 |
| DTO / VO 数 / 行数 | 12 / 227；11 / 405 |
| Util 数 / 行数 | 5 / 374 |
| Config / Interceptor / Exception / Common | 4 / 2 / 2 / 1（约 427 行合计） |

### 11.2 前端

| 指标 | 数值 |
|------|------|
| 源文件总数 | **31** |
| 源码总行数 | **7951** |
| `.vue` 页面 | 18（前台 10 + 后台 8） |
| 公共组件 | 2（`AppSidebar`、`ArticleCard`） |
| API 封装文件 | 4（`article/user/admin/common`） |
| 其余 | `router`、`store`、`utils`(2)、`main.js`、`App.vue`、`assets/main.css` |

### 11.3 接口与数据规模

| 指标 | 数值 |
|------|------|
| HTTP 端点总数 | 49（48 业务 + 1 health） |
| 董浩接口数 | 13（分类 5 + 标签 5 + 评论 3） |
| 其他接口数 | 35 + health 1 |
| 数据库表 | 8 |
| 功能点 | 30（董浩 8 + 其他 22） |

---

## 12. 已知问题与改进建议

以下问题均为**真实存在于当前代码/配置**中的情况（经核对源码确认）：

1. **密码加密强度不足（MD5 + 盐）**：`PasswordUtil` 使用 `MD5(盐 + 明文)`，MD5 已被证明易碰撞且不支持加盐迭代/慢哈希，抗暴力破解能力弱。建议升级为 **BCrypt / PBKDF2 / Argon2** 等自适应哈希。
2. **CORS 通配符配合携带凭证的安全隐患**：`CorsConfig` 使用 `addAllowedOriginPattern("*")` + `setAllowCredentials(true)`，等价于对任意来源开放携带 Cookie 的跨域访问。生产环境应明确指定可信前端域名清单，关闭通配。
3. **前端 `baseURL` 写死 `/api`，强依赖代理**：`utils/request.js` 中 `baseURL: '/api'` 仅在存在 `/api` 代理时可用；若前端直连后端或部署路径变化，需手动改代码。建议改为可配置（环境变量 `VITE_API_BASE`）。
4. **无自动化单元测试**：仅 `smoke-test.sh` 做集成断言，业务层缺乏 JUnit 单测，回归成本与风险较高。建议为分类/标签/评论核心逻辑补充单测。
5. **`mapper-locations` 指向 XML 但无 XML 文件**：`application.yml` 配置 `mapper-locations: classpath:mapper/*.xml`，但项目实际未使用任何 Mapper XML（SQL 全部走 MyBatis-Plus 自带或 Mapper 接口注解）。该配置项当前无害，但属于冗余配置，易误导，建议删除或补充 XML。
6. **上传目录依赖相对路径 `./upload`**：默认 `FILE_UPLOAD_PATH=./upload`，相对项目启动目录，若在不同目录启动会导致历史图片 404。建议生产环境用环境变量指定绝对路径，并对上传目录做备份策略。
7. **`/logout` 未被列入公开白名单**：`WebMvcConfig` 的排除列表未包含 `/logout`，导致未登录状态下调用 `/logout` 会被 `LoginInterceptor` 返回 401（实际退出场景均在已登录时发生，影响有限）。建议将 `/logout` 加入白名单。
8. **逻辑删除与物理删除混用**：分类/标签/公告/友链为物理删除，而用户/文章/评论为逻辑删除，数据清理策略不统一；删除分类/标签/公告/友链时若存在关联数据需业务层自行处理（如标签已做解绑，但分类删除前需人工迁移文章）。建议统一删除策略或补充级联/约束说明。

> 关于"双上传目录 `upload/` 与 `uploads/` 并存"：经全项目代码与目录检索，**当前代码仅使用 `./upload`（资源映射 `/upload/**`），`FileUploadUtil` 返回路径亦为 `/upload/...`，未发现 `uploads/`（多 s）的任何引用或目录**。该项在基线中被列为已知问题，但核对结果显示其已不存在或从未落地，特此说明，避免误记。

> 关于《首次运行必读.txt》：该文档标注"整理人：董浩、整理日期 2026-10-15"，但其内容称"文章/公告/友链未完成"，且示例测试账号为 `admin01/user01/user02`（密码 123456），与当前 `init.sql` 实际种子账号（`admin/admin123` 等）**不一致**。该文档属历史遗留说明，本文档第 9 章已按 `init.sql` 真实数据为准重写，部署时请以 `init.sql` 与本文档为准。

---

## 附录 A：环境变量与配置项速查

| 配置项 / 变量 | 位置 | 默认值 | 说明 |
|---------------|------|--------|------|
| `server.port` | application.yml | `9090` | 后端服务端口 |
| `server.servlet.session.timeout` | application.yml | `30m` | Session 超时 |
| `DB_URL` | 环境变量 | `jdbc:mysql://localhost:3306/blog_db?...` | 数据库连接串 |
| `DB_USERNAME` | 环境变量 | `root` | 数据库用户名 |
| `DB_PASSWORD` | 环境变量 | `root` | 数据库密码（必设） |
| `FILE_UPLOAD_PATH` | 环境变量 | `./upload` | 上传根目录 |
| `spring.servlet.multipart.max-file-size` | application.yml | `5MB` | 单文件上限（封面硬上限） |
| `spring.servlet.multipart.max-request-size` | application.yml | `20MB` | 单次请求上限 |
| `mybatis-plus.global-config.db-config.logic-delete-field` | application.yml | `deleted` | 逻辑删除字段 |
| `mybatis-plus.configuration.log-impl` | application.yml | `StdOutImpl` | SQL 日志（上线建议关闭） |
| 前端 `baseURL` | `utils/request.js` | `/api` | 接口前缀（依赖代理） |
| 前端 `port` | `vite.config.js` | `5173` | 前端开发端口 |
| 前端 `/api` 代理目标 | `vite.config.js` | `http://localhost:9090` | 后端地址 |

---

## 附录 B：常见报错对照表

| HTTP/code | 典型现象 | 排查路径 |
|-----------|----------|----------|
| **400** | `msg` 提示"分类名称已存在/不能为空/不能超过…" | 入参校验失败（重名、空值、超长、分类下有关联文章、评论内容超 500 等），属业务校验，按 `msg` 修正入参即可。 |
| **401** | `code:401, msg:"未登录，请先登录"` | 未登录或 Session 过期。前端响应拦截器会清本地态并跳登录页；检查是否已登录、`withCredentials` 是否携带 Cookie。 |
| **403** | `code:403, msg:"无权限，需要管理员身份"` 或"仅管理员或文章作者可删除" | 非管理员访问 `/admin/**`，或非作者非管理员删评论/文章。确认当前账号 `role=1`，或改用作者账号。 |
| **404** | `code:404, msg:"分类不存在/标签不存在/文章不存在/评论不存在"` 或"请求的接口不存在" | ①URL 中 id 填错（建议先查列表取真实 id）；②接口路径拼错；③`NoHandlerFoundException` 映射（路径不存在）。 |
| **405** | `code:405, msg:"请求方法不支持"` | 用了错误的 HTTP 方法（如用 GET 访问只支持 POST 的接口）。对照第 6 章接口清单的方法列。 |
| **413** | `code:413, msg:"上传文件过大"` | 超出 `max-file-size=5MB`（封面）或业务层头像 2MB 限制。压缩图片或调整限制。 |
| **500** | `code:500, msg:"系统异常，请联系管理员"` | 未捕获异常。重点场景：**health 正常但查库接口(如 `/categories`)返回 500 → 数据库未连通**（九成是 `DB_PASSWORD` 没设/设错/设错窗口/改完没重启）。另查 `Unknown database 'blog_db'`（建库未执行）、`Unknown column`（用了旧建库脚本，需重新执行 `init.sql`）。 |
| 端口占用 | `Port 9090 was already in use` | 后端端口被占用，关掉旧进程或换端口启动。 |
| Maven 类加载 | Git Bash 执行 `mvn` 报"找不到主类 Launcher" | Windows 版 Maven 在 Git Bash 的已知问题，改用 IDEA Maven 面板或 CMD/PowerShell。 |

> **关键口诀**：`/test/health` 只证明进程活着，**不查数据库**；只有 `/categories`、`/articles` 这类查库接口返回 200 才说明数据库真通了。**health 正常却 500 = 没连上数据库 / 没设 `DB_PASSWORD`**。
