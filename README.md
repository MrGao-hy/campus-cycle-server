# campus-cycle-server

校园循环 —— 校园二手交易平台后端服务。技术架构详见 [ARCHITECTURE.md](./ARCHITECTURE.md)。

## 技术栈

| 组件 | 版本 |
|---|---|
| JDK | 21（Temurin LTS） |
| Spring Boot | 3.5.3 |
| MyBatis-Plus | 3.5.12（含 jsqlparser 分页） |
| MySQL | 8.x |
| JWT | jjwt 0.12.6 |
| API 文档 | springdoc-openapi 2.8.9（Swagger UI） |
| 构建 | Maven 3.9+ |

## Nacos 配置中心

配置已托管到 Nacos（`nacos/` 目录下为配置源文件 + 一键发布脚本）：

| dataId | 说明 | 内容 |
|---|---|---|
| `campus-cycle-server.yaml` | 公共（所有环境） | JWT 密钥/有效期、微信 appid、调度间隔、上传目录、**商品分页大小** |
| `campus-cycle-server-dev.yaml` | 开发环境 | 数据源（devuser/123456）、日志级别、微信 AppSecret、`mock: false` |
| `campus-cycle-server-prod.yaml` | 生产模板 | `<CHANGE_ME>` 占位，发布前替换 |

- 分组：`CAMPUS_CYCLE`；命名空间：默认 public（可用 `NACOS_NAMESPACE` 覆盖）
- **优先级**：Nacos 配置 > 本地 `application-{profile}.yml` > `application.yml`
- **兜底**：`spring.config.import` 带 `optional:` 前缀，Nacos 没起也能直接用本地配置启动，不会启动失败
- **动态刷新**：`refresh-enabled: true`，Nacos 改配置即时推送；`@Value` 字段所在 Bean 需加 `@RefreshScope`（如 `AuthServiceImpl`）

### 一键发布配置到 Nacos

```bash
cd ~/nacos && ./start.sh                 # 1. 先启动 Nacos（服务端 8848 / 控制台 18080）
./nacos/publish.sh dev                   # 2. 发布公共 + dev 配置
# ./nacos/publish.sh prod                #    生产环境
```

可用环境变量：`NACOS_SERVER_ADDR`（默认 `127.0.0.1:8848`）、`NACOS_USERNAME/PASSWORD`（默认 `nacos/nacos`）、`NACOS_GROUP`、`NACOS_NAMESPACE`。

### 常用可调项（改 Nacos 即可生效，无需重启）

| 配置项 | 默认 | 说明 |
|---|---|---|
| `campus.goods.page-size` | 20 | 首页商品列表每页条数 |
| `campus.scheduler.order-sweep-interval-ms` | 60000 | 订单超时扫描间隔（重启生效） |
| `campus.wx.mock` | dev=false | 切回 mock 登录 |
| `campus.upload.dir` | `./uploads` | 图片上传目录 |

## 快速启动

### 前置条件

- JDK 21（`java -version` 确认）
- Maven 3.9+（`mvn -version`）
- MySQL 8.x 本机运行中（默认 `127.0.0.1:3306`）
- Nacos 3.x 运行（可选，不启动时走本地 `application*.yml` 兜底）

### 步骤

```bash
# 1. 建库建表（一次性；含 5 所学校种子数据）
mysql -uroot -p < sql/schema.sql

# 2. 导入 mock 演示数据（可重复执行，幂等；10 所学校 / 13 用户 / 62 商品 / 20 订单 / 10 评价 / 10 账单 / 10 会话 / 36 消息）
mysql -udevuser -p123456 < sql/mock-data.sql

# 3. 配置数据库连接（可选，dev 环境默认 devuser/123456）
export DB_USERNAME=devuser DB_PASSWORD=123456

# 4. 启动服务（开发模式）
mvn spring-boot:run

# 或打包后运行（生产/后台）
mvn package -DskipTests
java -jar target/campus-cycle-server-0.0.1-SNAPSHOT.jar
```

启动成功看到 `Started CampusCycleApplication`，监听 `http://127.0.0.1:8080`。

### 验证

```bash
# OpenAPI JSON（列出全部接口定义）
curl http://127.0.0.1:8080/v3/api-docs

# 登录拿 token（mock 模式，无需真实微信）
curl -X POST http://127.0.0.1:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"jsCode":"test-001"}'
# → {"code":200,"data":{"token":"...","userInfo":{...}}}
```

## Swagger 接口文档（前端接入首选）

启动后浏览器打开：**http://127.0.0.1:8080/swagger-ui.html**

- 28 个接口全部可视化，可直接在页面内调试（输入参数 → 发送 → 看响应）
- 每个接口带中文说明、请求/响应结构
- 调试需登录的接口：先调 `/auth/login` 拿 token，点击右上角 **Authorize** 填入 `token`（不带 Bearer 前缀），后续请求自动携带
- 前端也可直接拉取 `http://127.0.0.1:8080/v3/api-docs` 生成 TS 类型或客户端代码

## 接口约定

- 统一响应：`{ code, message, data }`，`code=200` 成功、`401` 未登录、`1000` 业务规则不允许
- 认证：除 `/auth/**`、`/school/list`、Swagger 文档外，请求头必须带 `token: <jwt>`
- 时间戳：全部为毫秒（`BIGINT`），与前端 number 一致

完整接口清单见 [ARCHITECTURE.md §7](./ARCHITECTURE.md#7-接口清单25-个swagger-可交互调试)。

## 前端联调（campus-cycle）

1. 前端 `vite.config.ts` 已配置代理：`/api` → `http://localhost:8080`（自动去掉 `/api` 前缀）
2. 前端请求 `POST /api/auth/login`、`GET /api/goods/list?schoolId=...` 即可直连后端
3. 前端登录态 key 为 `member_token`，请求拦截器自动放入 `token` header，与后端契约一致
4. 后端 `WX_MOCK=true` 时前端可传任意 `jsCode` 登录；已接真实微信（`application-dev.yml` 已配 AppSecret）时前后端均走真实 code2session

## 微信登录模式

| 模式 | 配置 | 行为 |
|---|---|---|
| 真实微信（当前默认，dev profile） | `application.yml` appid 默认 `wx1d337ce3ce3bae15`；`application-dev.yml` 已配 secret + `mock: false` | 前端 `uni.login` 取 code → 后端 code2session 换 openid，同一微信账号稳定登录 |
| mock（联调兜底） | `WX_MOCK=true`（或 appid/secret 未配置） | 按 jsCode 直接建号签发 token；前端 H5 固定演示账号 10001（皮蛋同学，含 mock 数据） |

- 前端 `src/config/env.ts` 已按环境切换：小程序 develop/trial/release 均 `wxMock: false`（真实 code），H5/App 演示环境走 mock
- 切换回 mock：后端设环境变量 `WX_MOCK=true` 重启，前端把 `wxMock` 改回 `true`
- ⚠️ AppSecret 属敏感凭证：`application-dev.yml` 中的 secret 仅限本地开发；若仓库公开，请在微信公众平台重置 secret，并改用环境变量 `WX_SECRET` 注入
- 真实模式需在微信公众平台配置小程序（AppID 已填 `wx1d337ce3ce3bae15`）并申请对应接口权限

## 环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| DB_HOST / DB_PORT / DB_NAME | 127.0.0.1 / 3306 / campus_cycle | 数据库连接 |
| DB_USERNAME / DB_PASSWORD | devuser / 123456 | 数据库账号（dev 默认） |
| CAMPUS_JWT_SECRET | dev 默认值（仅开发） | JWT 密钥，生产必须覆盖 |
| CAMPUS_JWT_EXPIRE_MS | 2592000000（30 天） | token 有效期 |
| WX_APPID | wx1d337ce3ce3bae15 | 微信小程序 AppID（application.yml 默认） |
| WX_SECRET | 空（dev profile 已配本地值） | 微信小程序 AppSecret，生产必须环境变量注入 |
| WX_MOCK | true（application.yml 兜底） | dev Nacos 配置为 `${WX_MOCK:false}`；H5 演示想跳过微信时启动服务带上 `WX_MOCK=true` |
| SERVER_PORT | 8080 | 服务端口 |
| NACOS_SERVER_ADDR | 127.0.0.1:8848 | Nacos 地址 |
| NACOS_USERNAME / NACOS_PASSWORD | nacos / nacos | Nacos 账号 |
| NACOS_GROUP | CAMPUS_CYCLE | 配置分组 |
| NACOS_NAMESPACE | 空（public） | 配置命名空间 |

## 业务规则（与前端 src/types 契约一致）

- **订单状态机**：`待卖家确认 → 待线下交易 → 待买家确认 → 申诉期(48h) → 已完成/已取消`
  - 卖家 24h 未确认 → 自动过期取消（不收手续费）
  - 买家确认后进入 48h 申诉期，到期无异议自动完成并生成手续费账单
  - 申诉期内卖家可提出异议，进入平台申诉处理
- **手续费**：首单免费；成交价 6%，最低 1 元，最高 20 元；未结清禁止发布新商品
- **超时流转**：`OrderScheduler` 每 60s 扫描 + 订单查询前兜底触发
- **安全**：JWT 认证 + ThreadLocal 用户上下文，白名单外均需 token

## 常用命令

```bash
mvn compile                 # 编译
mvn spring-boot:run         # 开发启动
mvn package -DskipTests     # 打包（跳过测试）
java -jar target/campus-cycle-server-0.0.1-SNAPSHOT.jar   # 运行 jar
```

## 目录结构

```
src/main/java/com/campus/cycle/
├── CampusCycleApplication.java   # 启动入口
├── common/                       # 常量 / 异常 / 统一响应 / 装配器
├── config/                       # Web / MyBatis-Plus / OpenAPI / 定时任务
├── security/                     # JwtUtil / AuthInterceptor / UserContext
├── controller/                   # auth / school / goods / order / chat / fee
├── service/                      # 业务接口 + impl（订单状态机核心）
├── mapper/                       # MyBatis-Plus Mapper
├── entity/                       # 8 张表实体
├── dto/                          # 请求体
└── vo/                           # 响应体（对齐前端契约）
sql/schema.sql                    # 建库建表 + 学校种子数据
```

## 常见问题

| 问题 | 处理 |
|---|---|
| `Unsupported character encoding 'utf8mb4'` | JDBC URL 用 `characterEncoding=UTF-8`（Connector/J 9.x 不认 utf8mb4） |
| `condition` 字段 SQL 报错 | `condition` 是 MySQL 保留字，实体已 `@TableField("\`condition\`")` 转义 |
| 登录 401 | 请求头缺 `token` 或已过期，重新调 `/auth/login` |
| 8080 端口被占用 | `lsof -iTCP:8080` 查占用进程，或 `SERVER_PORT` 换端口 |
