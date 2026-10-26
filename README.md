# 基于 Spring Boot 的博客系统

一个前后端分离的全栈博客系统：Spring Boot 3 + MyBatis-Plus + MySQL 8 提供 REST 接口，Vue 3 + Vite 构建前台与后台界面，Session + 拦截器完成鉴权。

> 南京工程学院大四实训（第 7—9 周）· 8 人小组协作 · 后端 48 个业务接口 / 前端 18 个页面 / 30 个功能点全部落地

---

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.5 · JDK 21 · MyBatis-Plus 3.5.5 · Maven 3.8 |
| 数据库 | MySQL 8（utf8mb4） |
| 前端 | Vue 3.4 · Vite 5 · Vue Router · Pinia · Element Plus · Axios · Node 22+ |
| 鉴权 | Session + 拦截器（登录 / 管理员双拦截器，本项目明确不引入 Spring Security / JWT） |

## 目录结构

```
├── blog-system/                      # 后端工程（Spring Boot，83 个 Java 类）
│   ├── pom.xml
│   ├── sql/init.sql                  # 建库建表脚本（8 张表 + 种子数据）
│   ├── scripts/
│   │   ├── smoke-test.sh             # 一键冒烟：启动 → 50 项断言 → 关停
│   │   ├── smoke-test-extra.sh       # 扩展用例（文章 / 公告友链 / 管理端）
│   │   └── api-console.html          # 单文件接口自测台（复制到 upload/ 后同源访问）
│   ├── 项目文档-基于SpringBoot的博客系统.md   # ⭐ 全项目总文档（12 章 + 2 附录）
│   ├── 数据库设计说明书.md
│   ├── 用户模块接口文档.md / 文章模块接口文档.md
│   ├── 分类标签评论模块接口文档.md / 公告友链文件上传模块接口文档.md
│   └── src/main/
│       ├── java/com/example/blog/
│       │   ├── common/              # 常量、错误码
│       │   ├── config/              # 跨域、MyBatis-Plus、MVC、自动填充
│       │   ├── controller/          # 14 个控制器
│       │   ├── dto/                 # 请求对象
│       │   ├── entity/              # 8 个实体
│       │   ├── exception/           # 业务异常 + 全局异常处理
│       │   ├── interceptor/         # 登录 / 管理员拦截器
│       │   ├── mapper/              # 数据访问层
│       │   ├── service/             # 业务层（含 impl）
│       │   ├── util/                # 统一返回、分页、密码、文件上传
│       │   └── vo/                  # 前端展示对象
│       └── resources/application.yml
├── blog-frontend/                    # 前端工程（Vue 3 + Vite，31 个源码文件）
│   ├── vite.config.js                # 5173 → 代理 /api、/upload 到 9090
│   ├── package.json
│   └── src/
│       ├── api/                      # 4 个模块化接口封装（user/article/admin/common）
│       ├── views/                    # 18 个页面（10 前台 + 8 后台）
│       ├── components/ router/ store/ utils/
│       └── main.js / App.vue
├── 数据库设计说明书.html              # 含 E-R 图、数据字典
├── 博客系统E-R图.vsdx                 # Visio 版 E-R 图（Chen 氏表示法）
├── 需求分析说明书_修订版.docx
├── 自测与验收操作指南.md              # ⭐ 怎么跑、怎么测、怎么看效果（含报错对照表）
├── 开发踩坑与避错记录.md              # 24 条真实踩坑 + 提交前自查清单
├── 接口清单-分类标签与评论.md
├── 董浩-分类标签与评论模块-开发规划.md
├── E-R图绘制修改说明.md
├── 组员上手指南.md
├── 基于SpringBoot的博客系统开发-7-9周小组分工方案.docx
└── 任务时间顺序与代码交接流程.docx
```

## 功能模块

30 个功能点已全部实现。下表按模块汇总，完整清单（含每个功能点对应的接口与责任标注）见
**《blog-system/项目文档-基于SpringBoot的博客系统.md》第 4 章**。

| 模块 | 功能点 | 编号 | 接口数 |
|---|---|---|---|
| 用户 | 注册、登录、登出、信息查看、资料修改、改密 | U1–U6 | 8 |
| 文章 | 列表/搜索、详情、编辑回显、我的文章、增删改、发布/草稿、封面上传 | A1–A8 | 10 |
| 分类 | 管理端增删改 + 公开列表、按分类浏览、删除前校验 | C1、C3 | 5 |
| 标签 | 管理端增删改 + 公开列表、按标签浏览、删除自动解绑 | C2、C4 | 5 |
| 评论 | 发表、两级回复、树形展示、级联删除 | M1–M4 | 3 |
| 文件 | 封面上传、头像上传、静态资源访问 | F1–F3 | 3 |
| 公告 / 友链 | 公告列表与最新、友链列表、管理端增删改 | N1–N2 | 8 |
| 首页 / 搜索 | 首页聚合、关键词搜索、分类标签导航 | H1–H3 | 复用上方 |
| 管理端 | 概览统计、用户管理、文章管理、评论管理 | — | 6 |
| 公共 | 统一返回体、全局异常处理、跨域、分页、自动填充、拦截器 | — | /test/health |

后端共 **48 个业务接口 + 1 个健康检查**。

## 快速开始

### 1. 建库

在 MySQL 中执行 `blog-system/sql/init.sql`，会创建 `blog_db`、8 张表及种子数据。

### 2. 配置数据库

`application.yml` 中账号密码均为占位符，推荐用环境变量覆盖（不改文件）：

| 变量 | 说明 | 默认值 |
|---|---|---|
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `root` |
| `DB_URL` | JDBC 连接串 | `jdbc:mysql://localhost:3306/blog_db?...` |
| `FILE_UPLOAD_PATH` | 上传文件目录 | `./upload` |

### 3. 启动后端

```bash
cd blog-system
mvn -DskipTests package
java -jar target/blog-system-1.0.0.jar --server.port=9090
```

> 建议显式带 `--server.port=9090`：系统环境变量 `SERVER_PORT` 会覆盖 application.yml 的端口配置。
> 也可以直接在 IDEA 里运行 `BlogApplication`。

验证：`GET http://localhost:9090/test/health` 返回统一返回体的成功响应。

### 4. 启动前端

```bash
cd blog-frontend
npm install          # 首次
npm run dev          # http://localhost:5173
```

前端 `baseURL` 为 `/api`，由 Vite 代理转发到 `http://localhost:9090`，因此**必须先启动后端**。

### 5. 自测（可选）

```bash
DB_PASSWORD=你的密码 CONFIRM_TEST_DB=YES bash scripts/smoke-test.sh   # 会先清空业务表，仅限测试库
cp scripts/api-console.html upload/                                    # 可视化自测台
# 访问 http://localhost:9090/upload/api-console.html
```

完整流程（含截图建议、报错对照表）见 **《自测与验收操作指南.md》**。

## 约定

- 所有接口统一返回 `Result<T>`，业务异常抛 `BusinessException` 由全局处理器兜底。
- 主键自增 BIGINT；时间字段由 MyBatis-Plus 自动填充；逻辑删除用 `deleted` 字段（user / article / comment）。
- 密码使用「MD5 + 随机盐」存储，禁止明文。
- 公开只读接口用复数路径（`/articles`、`/categories`、`/tags`、`/comments/**`）并在 `WebMvcConfig` 精确放行；写操作用单数路径（`/article`、`/comment`）或 `/admin/**`，必须登录。
- 分页统一走 `PageResult<T>` + `PageUtils`（页码、页大小自动兜底）。

## 开发进度

第 7—9 周三阶段全部完成：

- **第 7 周**：后端框架、用户模块、分类标签与评论模块落地。
- **第 8 周**：文章、公告友链、文件上传、管理端接口补齐；Vue 前端 18 个页面与前后端联调完成。
- **第 9 周**：50 项断言冒烟测试通过、材料归档、仓库整合。

遗留项（详见项目文档第 12 章）：`upload/` 目录未纳入版本控制需本地创建、前端 `baseURL` 写死 `/api` 依赖代理、CORS 通配符配合凭证存在安全隐患、缺少自动化单元测试。
