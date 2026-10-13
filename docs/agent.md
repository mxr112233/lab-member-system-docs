# Agent 开发长期记忆

> 文件用途：作为本项目后续 Agent 对话的长期执行记忆，保存已确认的技术决策、开发顺序、接口口径和验收要求。
> 最后更新：2026-10-13
> 当前阶段：M2 已完成，M3 待开始
> 项目根目录：`C:\Users\Administrator.DESKTOP-FUSABER\Desktop\lab-member-system-docs`

## 1. 新对话启动规则

每个新对话开始开发前，必须按以下顺序读取资料：

1. 本文件 `docs/agent.md`
2. `README.md`
3. `docs/01-需求文档.md`
4. `docs/02-数据库设计.md`
5. `docs/03-接口文档.md`
6. `docs/05-测试用例.md`
7. `sql/init.sql`

执行前先检查工作区状态。除用户明确要求讨论方案外，应直接推进开发，不要只重复分析计划。

本文件是执行记忆，不替代题目文档。若与仓库最新版文档冲突，以最新的需求、数据库、接口和测试文档为准；解决后的结论必须回写到本文件。

## 2. 当前仓库状态

- 当前仓库已完成 M0 工程初始化、M1 领域模型和 M2 后端 CRUD/数据库分页，包含 Maven 后端、Vue 3 前端、基础配置、实体、Mapper、DTO、VO、统一成功响应、Service、Controller、基础 404/重复学号响应和分页拦截器。
- 当前开发分支为 `codex/m2-crud-api`，尚未推送。
- 后端以 Java 17 为编译目标，本机运行时为 Java 21；前端使用 Vue 3、Vite、TypeScript、Element Plus 和 Axios。
- MySQL 8.4.9 可用；现有 `MySQL84` 服务保持原样。M0 已另建独立本地实例 `127.0.0.1:3307`，创建 `lab_system` 并执行 `sql/init.sql`；应用本地凭据写入被 Git 忽略的 `src/main/resources/application-local.yml`。
- 开发时不得直接推送 `main`。默认使用 `codex/` 前缀分支，例如 `codex/m1-domain-model`；若用户使用自己的 Fork，也可按仓库要求采用 `feature/xxx`。
- `docs/agent.md` 由当前交接创建，可能处于未提交状态。新 Agent 必须保留并纳入后续提交，不得丢弃。

## 3. 项目目标与范围

实现一个可独立运行的实验室成员管理系统：

- 后端提供成员新增、详情、分页条件查询、修改、删除五个 RESTful 接口。
- 前端提供成员列表、筛选、分页、新增、编辑和删除确认。
- 后端使用数据库分页、参数校验、统一异常处理、统一返回结构和 Swagger。
- 提供后端自动化测试、数据库初始化数据和启动说明。

V1.1 不要求：

- 登录、注册、JWT、权限管理
- Redis、MQ、微服务
- 文件或图片上传
- Excel 导入导出
- 邮件、短信

## 4. 已确认的关键口径

| 主题 | 已确认决策 |
| --- | --- |
| 文档版本口径 | 以 README V1.4、需求 V1.3、数据库 V1.2、接口 V1.2、测试 V1.3 为基础 |
| 职位枚举 | 共九类，必须包含 `ACHIEVEMENT` |
| 部门职位关系 | 不额外校验部门和职位的匹配关系；`ACHIEVEMENT + PRODUCT` 也应允许，以满足 FE-018 |
| 删除方式 | 物理删除；`status` 只表示成员是否仍在实验室，不能作为软删除标记 |
| 分页方式 | 必须由数据库分页，禁止查询全量后在 Java 内存切页 |
| 枚举存储 | 数据库、接口、前端统一使用英文枚举码，中文只用于展示 |
| 错误返回 | HTTP 状态码与响应体 `code` 保持一致：400、404、500 |
| 学号唯一 | 业务层预检查和数据库唯一索引双重保证，唯一冲突统一返回 400 |
| 修改时间 | 每次成功修改都必须刷新 `updateTime`，即使提交值与原值相同 |
| 可选联系方式 | `phone`、`email` 为空字符串时按未填写处理；填写后必须校验格式 |
| 超长字段 | 必须按数据库长度限制校验，不能让超长值穿透为未处理的 500 |
| 分页边界 | `page`、`pageSize` 至少大于 0，推荐 `pageSize` 上限 100，防止异常大查询 |
| 敏感信息 | 不提交真实密码、`.env`、密钥或本地配置；使用环境变量或未跟踪的本地配置 |

## 5. 领域模型

数据表：`lab_member`

| 字段 | 类型 | 规则 |
| --- | --- | --- |
| id | Long | 主键，自增 |
| name | String | 必填，最长 50 |
| studentNo | String | 必填，唯一，最长 30 |
| gender | Integer | 必填，0 未知 / 1 男 / 2 女 |
| grade | String | 必填，最长 20 |
| major | String | 必填，最长 100 |
| phone | String | 可选，最长 20，填写时校验手机号 |
| email | String | 可选，最长 100，填写时校验邮箱 |
| department | String | 必填，`SOFTWARE` / `ACHIEVEMENT` |
| position | String | 必填，九类职位之一 |
| profileStatus | String | 必填，默认 `PENDING` |
| status | Integer | 必填，默认 1；0 已离开 / 1 正常 |
| createTime | DateTime | 系统生成 |
| updateTime | DateTime | 系统生成，修改时必须刷新 |

职位枚举：

- `CAPTAIN`：队长
- `VICE_CAPTAIN`：副队长
- `FRONTEND`：前端
- `BACKEND`：后端
- `FULLSTACK`：全栈
- `PRODUCT`：产品
- `QA`：测试
- `DEVOPS`：运维
- `ACHIEVEMENT`：成果中心

资料状态枚举：

- `PENDING`：待补录
- `COMPLETED`：已完成
- `ESCALATED`：已升级人工跟进

## 6. API 契约

Base URL：`/api`

统一成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

接口：

| Method | URL | 功能 | 关键结果 |
| --- | --- | --- | --- |
| POST | `/api/members` | 新增成员 | 成功返回 `data.id`；参数错误或学号重复返回 400 |
| GET | `/api/members/{id}` | 查询详情 | 成功返回完整成员；不存在返回 404 |
| GET | `/api/members` | 分页条件查询 | 返回 `records`、`total`、`page`、`pageSize` |
| PUT | `/api/members/{id}` | 修改成员 | ID 不存在返回 404；学号冲突或参数错误返回 400 |
| DELETE | `/api/members/{id}` | 物理删除 | 成功 `message` 为“删除成功”；不存在返回 404 |

列表查询参数：

- `page`：默认 1
- `pageSize`：默认 10
- `name`：姓名模糊查询
- `studentNo`
- `grade`
- `department`
- `position`
- `profileStatus`
- `status`

## 7. 技术与目录基线

后端：

- Java 17+
- Spring Boot 3.x
- Maven
- MySQL 8.x
- MyBatis-Plus
- Lombok
- Spring Validation
- SpringDoc OpenAPI / Swagger
- JUnit、MockMvc、Mockito

前端：

- Vue 3
- Vite
- TypeScript
- Element Plus
- Axios
- npm
- Vite 开发代理 `/api` 到 `http://localhost:8080`

建议后端包名：`com.nynu.codelab.labmember`

目标目录：

```text
src/main/java/com/nynu/codelab/labmember/
  common/
  member/controller/
  member/service/
  member/mapper/
  member/entity/
  member/dto/
  member/vo/
src/test/java/com/nynu/codelab/labmember/
frontend/src/
  api/
  components/
  constants/
  types/
  views/
pom.xml
README.md
```

## 8. 开发顺序

| 阶段 | 状态 | 任务 | 完成标准 |
| --- | --- | --- | --- |
| M0 | 已完成 | 初始化 Spring Boot、Vue 3 + Vite + TypeScript、MySQL 配置、Vite 代理和基础目录 | 前后端可构建；后端可启动；Swagger 路径可访问；前端可启动并正确代理；数据库初始化脚本和样例数据已通过独立 3307 实例验证 |
| M1 | 已完成 | 核对数据库脚本，实现实体、Mapper、DTO、VO 和查询模型 | 表结构与 10 条样例数据符合文档，数据覆盖全部要求维度 |
| M2 | 已完成 | 实现五个后端 CRUD 接口和数据库分页 | Swagger 可直接调用五个接口，响应结构符合接口文档 |
| M3 | 待开始 | 完成 Validation、枚举、长度、手机号、邮箱、统一异常和学号冲突处理 | ADD、GET、UPDATE、DELETE、EDGE 相关错误码符合测试用例 |
| M4 | 未开始 | 编写 Service 和 Controller 自动化测试 | `mvn clean test` 通过，覆盖测试文档要求的成功、失败和不存在场景 |
| M5 | 未开始 | 实现前端列表、筛选、分页、新增、编辑、删除确认和错误提示 | FE-001 至 FE-020 可在浏览器中完成验证 |
| M6 | 未开始 | 前后端联调、全量自测、README、提交和 PR | 完成标准链路通过，测试结果完整，成果 PR 符合模板和 CI |

## 9. M0/M1 阶段记录

M0 只负责工程初始化和可运行验证，不提前实现业务 CRUD。

必须完成：

1. 检查 Java、Maven、Node、npm、MySQL 环境。
2. 创建 Maven 后端项目，加入 Web、Validation、MyBatis-Plus、MySQL、Lombok、SpringDoc 等基础依赖。
3. 创建 Spring Boot 启动类和基础配置，使用环境变量提供数据库连接，不提交真实密码。
4. 创建 Vue 3 + Vite + TypeScript 前端项目，加入 Element Plus 和 Axios。
5. 配置 Vite `/api` 代理到 `localhost:8080`。
6. 建立后端与前端的基础目录，仅放置后续开发需要的空结构或最小占位。
7. 验证后端构建和启动，验证 Swagger/OpenAPI 路径。
8. 验证前端构建和开发服务器启动。
9. 检查 `.gitignore`，确保 `target/`、`node_modules/`、本地配置和构建产物不会入库。
10. 更新本文件的阶段状态和实际验证结果。

M0 不实现：

- 成员实体和数据库 CRUD
- 业务接口
- 前端成员列表和表单
- 自动化业务测试

### M0 实际验证记录（2026-10-13 11:17 CST）

环境检查：

- Java：`21.0.12.1`，项目编译目标为 Java 17。
- Maven：`3.9.16`。
- Node：`v24.21.0`。
- npm：`11.19.0`。
- MySQL：`8.4.9`；现有 `MySQL84` 服务未修改；M0 独立实例监听 `127.0.0.1:3307`。
- Git 分支：`codex/m0-bootstrap`。

已执行验证：

1. 后端：`mvn clean test` 通过。M0 阶段尚未编写业务测试，因此测试数为 0。
2. 后端：`mvn clean package` 通过，生成 Spring Boot 可执行 JAR。
3. 前端：`npm ci` 通过，新增 98 个包、审计 99 个包，npm audit 显示 0 个漏洞。
4. 前端：`npm run build` 通过，Vite `6.4.4` 构建 1599 个模块。
5. 后端启动：加载被 Git 忽略的 `application-local.yml` 并执行 `java -jar target/lab-member-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=local`，应用成功启动于 8080。
6. 后端接口：`GET /api/health` 返回 200 和 `{"status":"UP"}`。
7. OpenAPI：`GET /v3/api-docs` 返回 200。
8. Swagger：`GET /swagger-ui/index.html` 与 `GET /swagger-ui.html` 均返回 200。
9. 前端启动：`npm run dev -- --host 127.0.0.1 --port 5173` 成功启动于 5173。
10. 前端代理：`GET http://127.0.0.1:5173/api/health` 返回 200 和 `{"status":"UP"}`，证明 `/api` 已代理到后端 8080。
11. 文档：`npx --yes markdownlint-cli2@0.23.2 --no-globs "README.md" "docs/**/*.md" ".github/**/*.md"` 通过，检查 7 份文档、0 个问题。
12. 数据库：使用 `mysqld --initialize` 初始化独立 3307 实例，并以 `lab_member` 账号执行 `sql/init.sql`；查询结果为 1 张表、10 条成员、2 个部门、9 个职位、3 种资料状态和 1 条 `status=0` 数据。
13. 后端：使用 `--spring.profiles.active=local` 加载 `application-local.yml` 后启动成功，`/api/health`、`/v3/api-docs` 和 `/swagger-ui/index.html` 均返回 200。

数据库验证与如实说明：

- 已在 127.0.0.1:3307 创建独立 MySQL 8.4.9 实例，未修改现有 MySQL84 服务或 3306 数据。
- 已创建数据库 lab_system 和应用账号 lab_member；本地管理员凭据位于 C:\Users\Administrator.DESKTOP-FUSABER\AppData\Local\LabMemberSystem\mysql-3307\admin-credentials.txt，应用凭据位于被忽略的 src/main/resources/application-local.yml，均不得提交。
- sql/init.sql 已成功执行：1 张表、10 条样例数据、2 个部门、9 个职位、3 种资料状态、1 条 status=0 数据。
- 独立实例当前作为本机进程运行，未注册为 Windows 服务；数据目录为 C:\Users\Administrator.DESKTOP-FUSABER\AppData\Local\LabMemberSystem\mysql-3307\data。
- M0 启动日志出现 “No MyBatis mapper was found”，原因是 M0 尚未创建业务 Mapper，属预期现象，M1 实现后应消失。
- 前端构建提示单个 chunk 超过 500 kB，是 Element Plus 当前整包引入导致的非阻塞警告；可在后续性能优化阶段处理。

M0 结论：工程初始化和数据库初始化均已完成，不包含成员实体、CRUD、成员页面或业务测试。可以直接进入 M1。

### M1 实际验证记录（2026-10-13 11:39 CST）

数据库核对：

- 连接独立实例 `127.0.0.1:3307/lab_system`，`SHOW CREATE TABLE lab_member` 返回 14 个字段，主键为 `id`，唯一索引为 `uk_student_no(student_no)`。
- 样例数据为 10 条；年级 3 种；性别覆盖 1、2；部门覆盖 `SOFTWARE`、`ACHIEVEMENT`；职位覆盖 9 种；资料状态覆盖 `PENDING`、`COMPLETED`、`ESCALATED`；成员状态覆盖 0、1。
- 未重复执行 `sql/init.sql`，未修改现有 `MySQL84` 服务或 3306 数据。

已实现范围：

- 实体：`LabMember`，使用 `@TableName("lab_member")` 和自增主键映射。
- Mapper：`LabMemberMapper`，继承 `BaseMapper<LabMember>`。
- DTO：`AddMemberRequest`、`UpdateMemberRequest`、`MemberQueryRequest`。
- VO：`MemberVO`、`MemberIdVO`、`MemberPageVO`。
- 未实现 Controller、Service、CRUD、Validation、统一异常、前端成员页面和自动化业务测试；这些分别属于 M2-M5。

执行命令与结果：

1. `mvn clean test`：`BUILD SUCCESS`。M1 尚未编写业务测试，因此测试数为 0，M4 补齐。
2. `mvn clean package`：`BUILD SUCCESS`，生成 `target/lab-member-system-0.0.1-SNAPSHOT.jar`。
3. `java -jar target/lab-member-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=local`：应用成功启动于 8080，使用 `local` profile 和独立 MySQL 3307。
4. `GET /api/health`：返回 200 和 `{"status":"UP"}`。
5. `GET /v3/api-docs`：返回 200。
6. `GET /swagger-ui/index.html`：返回 200。
7. 启动日志检查：未出现 M0 阶段的 `No MyBatis mapper was found` 警告。
8. 验证完成后已停止 8080 应用进程，3307 数据库继续运行。

M1 未加入分页拦截器，数据库分页插件及实际分页查询保留在 M2 实现。

M1 结论：领域模型与真实数据库结构、样例数据一致，后端可在真实数据库配置下构建、启动并生成 OpenAPI；未越界实现 CRUD、校验、异常处理、业务测试或前端页面，可以进入 M2。

### M2 实际验证记录（2026-10-13 11:44 CST）

M2 实现范围：

- 新增 `ApiResponse` 统一成功响应结构。
- 新增 `MemberService` 和 `MemberController`，实现 `POST /api/members`、`GET /api/members/{id}`、`GET /api/members`、`PUT /api/members/{id}`、`DELETE /api/members/{id}`。
- 新增 `MybatisPlusConfig`，注册 `PaginationInnerInterceptor`；MyBatis-Plus 版本由 3.5.9 升级到本机缓存可用的 3.5.17，并显式引入同版本 `mybatis-plus-jsqlparser`。
- 新增最小化的成员不存在 404 和重复学号 400 响应；完整 Validation、枚举/长度/格式校验和统一异常体系仍留待 M3。
- 列表条件通过 `LambdaQueryWrapper` 组合执行，分页由 `labMemberMapper.selectPage` 在数据库完成，未在 Java 内存中切页。

已执行验证：

1. `mvn clean test`：`BUILD SUCCESS`。M2 未新增 M4 业务自动化测试，因此测试数仍为 0。
2. `mvn clean package`：`BUILD SUCCESS`，生成 `target/lab-member-system-0.0.1-SNAPSHOT.jar`。
3. `java -jar target/lab-member-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=local`：应用成功启动于 8080，连接独立 MySQL 3307。
4. `GET /api/health`：200。
5. `GET /v3/api-docs`：200；OpenAPI 中 `/api/members` 包含 get/post，`/api/members/{id}` 包含 get/put/delete。
6. `GET /swagger-ui/index.html`：200。
7. 真实成功链路：新增成员返回 id=11；查询详情返回 `ACHIEVEMENT` + `PRODUCT` 组合；条件分页查询 `page=1&pageSize=5` 命中 1 条；修改后查询确认字段变化；`updateTime` 从 `11:44:12` 刷新到 `11:44:13`；删除返回 `message=删除成功`；再次查询返回 404 和 `成员不存在`。
8. 分页 SQL 验证：临时启用 MyBatis SQL 输出后请求 `GET /api/members?page=2&pageSize=5`，日志确认执行 `SELECT COUNT(*) AS total FROM lab_member` 和 `SELECT ... FROM lab_member ORDER BY id ASC LIMIT ?,?`，参数为 `5(Long), 5(Long)`，响应 `page=2`、`pageSize=5`、`total=10`、`records=5`。
9. 验证结束后停止 8080 应用，保留 3307 MySQL 实例和原 3306/MySQL84 服务不变。

M2 结论：五个 CRUD 接口、统一成功响应、数据库分页和基础不存在响应均已在真实 MySQL 3307 环境验证，满足进入 M3 的条件。M3 需要补齐 Validation、枚举/长度/格式校验、统一异常处理及学号冲突完整处理。

## 10. 实现约束

后端：

- Controller、Service、Mapper 分层清晰。
- 使用统一响应对象和统一异常处理。
- 新增和修改使用独立请求 DTO，不在 Controller 直接接收数据库实体。
- 使用 MyBatis-Plus 分页插件进行数据库分页。
- 修改前先确认 ID 存在，再处理业务校验和更新。
- 捕获唯一索引冲突并转换为 400，避免返回 500。
- 参数绑定错误、JSON 解析错误和非法枚举统一返回 400。
- 未处理异常统一返回 500 和“系统异常”。

前端：

- 列表字段完整展示部门、职位、资料状态和成员状态。
- 筛选参数与后端接口一致。
- 新增和编辑复用同一个表单组件。
- 部门和职位分别校验合法枚举，但不校验组合关系。
- 删除必须二次确认。
- 展示后端返回的 400、404、500 `message`。
- 保存后刷新列表；删除最后一页最后一条数据时合理回退页码。

## 11. 测试与验收

自动化命令：

```bash
mvn clean test
cd frontend && npm install && npm run build
```

必须覆盖：

- Service：正常新增、重复学号、查询不存在、修改不存在、删除不存在。
- Controller：新增成功、参数错误、查询成功、查询不存在、修改成功、删除成功。
- 手工：ADD、GET、LIST、UPDATE、DELETE、EDGE、DATA、FE 全部用例组。

最终业务链路：

创建成员 → 查询成员 → 修改成员 → 查询修改结果 → 删除成员 → 再次查询返回 404。

前端必须通过页面完整走通该链路。`status` 可筛选，`department`、`position`、`profileStatus` 可保存和筛选。

## 12. 提交与协作

- 完成后使用规范提交信息：`feat: xxx`、`fix: xxx`、`docs: xxx`。
- 成果 PR 标题：`[姓名] lab-member-system 提交`。
- 维护 PR 标题：`type(scope): description`。
- 成果 PR 只评审、不合并。
- 不提交密钥、真实配置和敏感数据。
- AI 辅助开发必须如实说明，开发者需要能够解释代码、数据库设计和接口设计。

## 13. 变更记录

| 日期 | 变更 |
| --- | --- |
| 2026-10-12 | 创建长期记忆，固化文档范围、关键口径、技术栈、M0-M6 开发顺序和验收要求 |
| 2026-10-13 | 完成 M0 工程初始化，记录环境检查、前后端构建与启动、Swagger 和代理验证 |
| 2026-10-13 | 使用独立 MySQL 3307 实例创建 `lab_system`，执行 `sql/init.sql`，完成真实数据库配置下的启动与 Swagger 验证 |
| 2026-10-13 | 完成 M1 领域模型；核对真实表结构和样例数据覆盖，完成后端测试、打包、真实数据库启动、Mapper 扫描、Swagger 验证 |
| 2026-10-13 | 完成 M2 后端 CRUD 和数据库分页；升级 MyBatis-Plus 至 3.5.17，完成真实 3307 CRUD 链路、SQL 分页日志、Swagger/OpenAPI 和文档校验 |
