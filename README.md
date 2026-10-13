# 实验室成员管理系统（新人入门项目）

> CodeLab 新人第一个完整前后端项目：从需求分析走到验收交付的全流程练习。
> 仓库版本 V1.4 ｜ 题目文档版本：需求 V1.3 / 数据库 V1.2 / 接口 V1.2 / 测试用例 V1.3

本仓库是新人题目的**发布与成果提交入口**，不是通用项目仓库：题目文档、初始化脚本、CI 与审批线在这里统一维护；新人按题目规格在自己的 Fork 中开发，完成后向本仓库提交 PR，由管理员评审并按验收标准打分。

**新人请先读这三篇，再动手：** [需求文档](docs/01-需求文档.md) → [数据库设计](docs/02-数据库设计.md) → [接口文档](docs/03-接口文档.md)，[测试用例](docs/05-测试用例.md) 在自测阶段反复用。

## 文档地图

| 文档 | 内容 | 什么时候用 |
| --- | --- | --- |
| [需求文档](docs/01-需求文档.md) | 背景、范围、成员字段、功能规则、交付物、完成标准 | 动手前通读，验收前逐条对照 |
| [数据库设计](docs/02-数据库设计.md) | 表结构、字段规则、约束、索引、初始化数据要求 | 建表与写实体类时 |
| [接口文档](docs/03-接口文档.md) | 5 个接口、请求/响应结构、参数校验、Swagger 要求 | 写 Controller 与前端联调时 |
| [测试用例](docs/05-测试用例.md) | 接口用例、边界用例、数据一致性、前端用例、结果记录表 | 自测与填「自测结果」时 |
| [init.sql](sql/init.sql) | 建库建表 + 10 条样例数据 | 起库时 |
| `04-验收标准` | **实验室内部文档，不随仓库发布** | 评审时按内部标准执行，仓库内以「基础验收」为最低门槛 |

## 1. 项目做什么

维护 CodeLab 成员的基础信息，并提供一个可用的管理页面。成员信息包含：姓名、学号、性别、年级、专业、手机号、邮箱、所属部门、职位、资料状态、成员状态。

必须实现的能力：

- 后端 5 个 RESTful 接口：新增、详情、分页/条件查询、修改、删除
- 前端成员管理页面：列表（分页 + 条件筛选）、新增、编辑、删除（二次确认）
- 参数校验（前后端规则一致）、统一异常处理、Swagger/OpenAPI 文档、后端自动化测试
- 数据库初始化脚本与至少 10 条覆盖各维度的样例数据

**V1.1 不要求**：登录、注册、JWT、权限管理、Redis、MQ、文件/图片上传、Excel 导入导出、邮件、短信、微服务。自行实现可作为加分项，但**不得影响基础功能**。

## 2. 组织模型与枚举（不要写错）

CodeLab 只维护**两个部门**，职位共**九类**：

| 字段 | 取值 | 中文 |
| --- | --- | --- |
| department | `SOFTWARE` | 软件研发部（承载前端、后端、全栈、产品、测试、运维职位） |
| department | `ACHIEVEMENT` | 成果中心（成员职位为「成果中心」，承载成果归档、竞赛、论文、专利、软著与企业合作） |
| position | `CAPTAIN` | 队长 |
| position | `VICE_CAPTAIN` | 副队长 |
| position | `FRONTEND` | 前端 |
| position | `BACKEND` | 后端 |
| position | `FULLSTACK` | 全栈 |
| position | `PRODUCT` | 产品 |
| position | `QA` | 测试 |
| position | `DEVOPS` | 运维 |
| position | `ACHIEVEMENT` | 成果中心 |
| profileStatus | `PENDING` | 待补录 |
| profileStatus | `COMPLETED` | 资料与部门职位已确认 |
| profileStatus | `ESCALATED` | 超过 7 天仍未完成，已进入人工跟进名单 |
| status | `0` / `1` | 0 已离开实验室 / 1 正常 |
| gender | `0` / `1` / `2` | 0 未知 / 1 男 / 2 女 |

**数据库里存英文枚举码，不要直接存中文。** 枚举校验要在后端做（前端同时做一遍，规则保持一致）。

## 3. 成员字段规格

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | ---: | --- |
| id | Long | 否 | 主键，系统自动生成 |
| name | String | 是 | 姓名，≤ 50 字符 |
| studentNo | String | 是 | 学号，唯一，≤ 30 字符 |
| gender | Integer | 是 | 0 / 1 / 2 |
| grade | String | 是 | 年级，如 `2023` |
| major | String | 是 | 专业，如软件工程、人工智能 |
| phone | String | 否 | 填写时校验手机号格式 |
| email | String | 否 | 填写时校验邮箱格式 |
| department | String | 是 | `SOFTWARE` / `ACHIEVEMENT` |
| position | String | 是 | 九类职位之一 |
| profileStatus | String | 是 | `PENDING` / `COMPLETED` / `ESCALATED`，默认 `PENDING` |
| status | Integer | 是 | 0 / 1，默认 1 |
| createTime | DateTime | 否 | 创建时间 |
| updateTime | DateTime | 否 | 最后修改时间，修改时必须更新 |

## 4. 数据库要点

- 库名 `lab_system`，MySQL 8.x，字符集 `utf8mb4`
- 表 `lab_member`，字段与上表一一对应（下划线命名：`student_no`、`profile_status`、`create_time`、`update_time`）
- 必须的约束：`id` 主键自增、`student_no` 唯一索引、必填字段 `NOT NULL`
- 至少 10 条初始化数据，且要覆盖：不同年级、不同性别、**两个部门**、不同职位（含 `ACHIEVEMENT` 成果中心职位）、**三种资料状态**、正常成员与已离开成员、有联系方式与无联系方式
- 列表查询走**数据库分页**，禁止查全量再在 Java 内存里切页

## 5. 接口速查

Base URL：`/api`，统一返回结构 `{ "code": 200, "message": "success", "data": {} }`。

| Method | URL | 功能 |
| --- | --- | --- |
| POST | `/api/members` | 新增成员 |
| GET | `/api/members/{id}` | 查询成员详情 |
| GET | `/api/members` | 分页 / 条件查询 |
| PUT | `/api/members/{id}` | 修改成员 |
| DELETE | `/api/members/{id}` | 删除成员 |

查询参数：`page`（默认 1）、`pageSize`（默认 10）、`name`（模糊）、`studentNo`、`grade`、`department`、`position`、`profileStatus`、`status`。

分页返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "page": 1,
    "pageSize": 10
  }
}
```

错误约定：参数错误或学号重复 `400`，数据不存在 `404`，服务端异常 `500`。**不要设计成 `/addMember`、`/deleteMember` 这类 RPC 风格路径。**

Swagger 必须可用，推荐访问 `/swagger-ui/index.html`。

## 6. 技术栈

后端：Java 17+、Spring Boot 3.x、Maven、MySQL 8.x、MyBatis-Plus、Lombok、Spring Validation、SpringDoc OpenAPI / Swagger。

前端：Vue 3 + Vite + TypeScript，UI 组件库可选 Element Plus，请求用 fetch 或 axios；开发环境用 Vite 代理 `/api` → `http://localhost:8080`。

## 7. 推荐开发流程（16 步）

1. 通读需求文档，明确「必须实现」与「不要求」
2. 理解数据库设计，确定字段、约束、索引
3. 理解接口约定（路径、参数、返回结构、错误码）
4. 初始化 Spring Boot 项目（Java 17 + Maven）
5. 执行 `sql/init.sql` 完成建库建表与样例数据
6. 完成后端 CRUD（Controller → Service → Mapper 分层清晰）
7. 补齐参数校验与统一异常处理
8. 接入 SpringDoc，确认 Swagger 可查看并可直接调用
9. 补后端自动化测试（Service + Controller 两层）
10. 初始化前端项目（Vue 3 + Vite + TypeScript）
11. 实现列表页与新增/编辑/删除
12. 前后端联调（筛选、分页、错误提示全部走通）
13. 完善自己仓库的 README（含前后端启动步骤）
14. 按 `docs/05-测试用例.md` 逐项自测并记录结果
15. 提交 Git（分支 + 规范提交信息）
16. 发起成果 PR，等待评审

## 8. 交付物结构

```text
docs/          # 题目文档（本仓库提供，自己仓库可直接沿用）
sql/           # 数据库初始化脚本
src/           # 后端源码
frontend/      # 前端源码
README.md      # 含前后端启动步骤
pom.xml
.gitignore
```

## 9. 协作与提交规范

**动手前**：先读飞书《成员手册》与《协作流程》（制度、规范与流程类内容统一维护在飞书共享文件夹），再读本仓库题目文档。本仓库维护类改动走「分支 + PR + `codelab-admin` 批准」，审批线见 `CODEOWNERS`。

**开发时**：

1. Fork 本仓库到自己的 GitHub 账号
2. 在自己的 fork 中开发，分支名用 `feature/xxx`（如 `feature/member-crud`）
3. 提交信息带规范前缀：`feat: xxx`、`fix: xxx`、`docs: xxx`
4. 不直接推 main，只往自己的 feature 分支提交
5. 可以借助 AI 辅助开发，但必须能解释自己的代码、数据库设计与接口设计

**完成后（成果 PR）**：

1. 向本仓库 `nynu-codelab/lab-member-system-docs` 发起 Pull Request
2. 标题格式：`[姓名] lab-member-system 提交`
3. 描述按 PR 模板的「成果提交」部分填写：完成情况、自测结果、AI 辅助说明
4. 管理员在 PR 中评审并按验收标准打分
5. **成果 PR 只评审、不合并**（本仓库唯一例外，不要模仿到其他项目仓库）
6. 验收通过后 PR 关闭，结果记入实验室记录

## 10. CI 会自动检查什么

| 检查 | 触发 | 规则 |
| --- | --- | --- |
| Markdown Lint | PR / push 到 main | `npx markdownlint-cli2 "**/*.md"`，配置见 `.markdownlint-cli2.yaml` |
| PR Title Lint | PR 打开、编辑、重开、同步 | 成果提交需匹配 `[姓名] ... 提交`；维护改动需匹配 `type(scope): description` |

会被 CI 拦下的标题示例与正确写法：

```text
错误：更新readme
错误：Lab member system
正确：[张三] lab-member-system 提交
正确：docs(readme): 补充新人入门项目说明
```

标题正则参考（`.github/workflows/pr-title-lint.yml`）：

```text
^\[[^]]+\].*(提交|submission|project)
^(feat|fix|docs|style|refactor|perf|test|build|ci|chore)(\([a-z0-9_-]+\))?!?: .+
```

## 11. 自测与验收

### 本地启动步骤

前置：Java 17+、Maven 3.9+、Node 18+、MySQL 8.x。先用 `sql/init.sql` 建库建表并写入样例数据——该脚本含 `INSERT`，只能在库未初始化时执行一次，切勿重复执行，否则会触发 `student_no` 唯一键冲突。数据库连接等敏感配置放在未跟踪的 `src/main/resources/application-local.yml`，通过 `local` profile 加载，不要提交或泄露密码。

```bash
# 1) 后端：测试、打包
mvn clean test
mvn clean package

# 2) 后端：以 local profile 启动，默认监听 8080
java -jar target/lab-member-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

```bash
# 3) 前端：安装依赖并启动开发服务器，默认监听 5173
cd frontend
npm install
npm run dev -- --host 127.0.0.1 --port 5173
```

启动后可访问：

- 健康检查：`GET http://localhost:8080/api/health` → `{"status":"UP"}`
- 接口文档：`http://localhost:8080/swagger-ui/index.html`（OpenAPI JSON：`http://localhost:8080/v3/api-docs`）
- 前端页面：`http://127.0.0.1:5173/`，页面通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`

提交前请自己先跑一遍，把输出粘进 PR 描述：

```bash
# 后端
mvn clean test

# 前端
cd frontend && npm install && npm run build
```

**基础验收**：项目能正常构建、启动，前端页面能走通完整 CRUD。

**完成标准（需求文档第 12 节）**：创建成员 → 查询成员 → 修改成员 → 查询修改结果 → 删除成员 → 再次查询确认不存在，整条链路必须能通过前端页面完整走通；成员数据必须包含部门和职位，资料状态可筛选。

**评分维度**：管理员按内部验收标准打分，仓库内保留的「基础验收」只是最低门槛。自测请覆盖 `docs/05-测试用例.md` 的 ADD / GET / LIST / UPDATE / DELETE / EDGE / DATA / FE 各组用例，并把「测试结果记录」表填完整。

## 12. 常见问题

**列表查询返回了全部数据自己切页？** 不允许。必须用数据库分页（MyBatis-Plus 的 `Page` 即可），接口文档明确写死了这条。

**学号重复返回 500？** 应返回 `400`。唯一索引冲突要在业务层捕获并转成参数错误，别让异常穿透成 500。

**查询不存在的成员返回 200 空对象？** 应返回 `404` 且 `message` 为「成员不存在」；删除后再次查询同样必须 404。

**删除做逻辑删除还是物理删除？** 按测试用例 DELETE-003「删除后查询 → 404」，逻辑删除需要额外过滤，否则用例会挂；`status` 字段表达的是「成员是否在实验室」，不是软删标记，两者不要混用。

**枚举直接存了中文？** 数据库、接口、前端三处都统一用英文枚举码（`SOFTWARE`、`BACKEND`、`PENDING`），中文只用于界面展示。

**前端没做校验？** 前后端校验规则必须一致：必填项、`gender ∈ {0,1,2}`、`status ∈ {0,1}`、部门/职位/资料状态枚举、phone/email 格式。前端拦截失败要给明确提示，后端 `400` 的 `message` 也要展示到页面上。

**Swagger 打不开？** 路径是 `/swagger-ui/index.html`（SpringDoc），确认依赖与配置都已生效。

**前端跨域或接口 404？** 开发环境用 Vite 代理 `/api` → `http://localhost:8080`；前端只写相对路径 `/api/members`，不要硬编码后端地址。

**提交后仓库里出现了 `target/`、`node_modules/`？** 先补 `.gitignore` 再提交，不要把构建产物入库。

**能不能提交 `application.yml` 里的真实密码、`.env`、密钥？** 不能。PR Checklist 明确要求：不得提交密钥、`.env`、真实配置或敏感数据。

**PR 标题被 CI 打回？** 看第 10 节的两种合法格式，按类型选一种；标题修改后 CI 会自动重跑。

**想直接推 main 省事？** 不行。本仓库与组织其他仓库一样：分支开发 + PR 审查；成果 PR 由管理员评审后关闭，不要试图合并。

## 13. AI 辅助的边界

允许使用 AI Coding 工具辅助开发，这也是本项目的训练目标之一。但提交者必须能够解释：代码结构、数据库设计理由、接口设计理由、测试如何覆盖需求。PR 里有专门的「AI 辅助说明」栏，请如实填写用到了哪些部分、自己怎么验证的。答不出实现细节的提交，即使功能跑通也不通过验收。

## 变更记录

| 版本 | 日期 | 变更 |
| --- | --- | --- |
| V1.0 | - | 仓库初始化：需求 / 数据库 / 接口 / 测试用例文档、`init.sql`、CI、PR 模板 |
| V1.1 | - | README 初版：协作要求、推荐流程、基础验收 |
| V1.2 | - | 需求 V1.2 与测试用例 V1.2：补充部门职位枚举、资料状态、前端用例与数据一致性用例 |
| V1.3 | - | 规范与流程入口改指飞书；文案对齐平台分工（制度规范在飞书，技术文档随项目） |
| V1.4 | - | 职位枚举新增 `ACHIEVEMENT`（成果中心） |
