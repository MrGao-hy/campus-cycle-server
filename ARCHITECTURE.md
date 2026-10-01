# 校园循环后端架构说明

> 版本：v0.1.0 ｜ 更新时间：2026-10-01
> 对应前端仓库：`campus-cycle`（UniApp + Vue3 + TypeScript）

## 1. 项目概述

校园二手交易平台后端服务，支撑前端全业务流程：商品发布/浏览、购买申请、订单状态机流转、站内沟通、手续费账单、学校隔离与安全承诺。

后端接口契约完全对齐前端 `src/api/module/*` 的 mock 定义，前端可无感切换真实接口。

## 2. 技术栈

| 分类 | 选型 | 版本 | 说明 |
|---|---|---|---|
| 语言 | Java | 21（Temurin LTS） | |
| 框架 | Spring Boot | 3.5.3 | web / validation / scheduling |
| ORM | MyBatis-Plus | 3.5.12 | 含 jsqlparser 分页插件 |
| 数据库 | MySQL | 8.x | utf8mb4 |
| 认证 | JWT（jjwt） | 0.12.6 | HS384，载荷仅存 userId |
| API 文档 | springdoc-openapi | 2.8.9 | Swagger UI |
| 构建 | Maven | 3.9+ | |
| 辅助 | Lombok | - | 由 Boot parent 管理 |

## 3. 总体架构

```
┌─────────────────────────────────────────────────────┐
│                    前端（campus-cycle）              │
│   UniApp H5/小程序/App  →  /api/* （vite 代理）      │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP + header: token
┌──────────────────────▼──────────────────────────────┐
│                 campus-cycle-server :8080            │
│  ┌───────────────────────────────────────────────┐  │
│  │ Controller 层（auth/school/goods/order/chat/fee）│  │
│  └──────────────────────┬────────────────────────┘  │
│  ┌──────────────────────▼────────────────────────┐  │
│  │ Security 层：AuthInterceptor → UserContext     │  │
│  │（JWT 解析，白名单放行 /auth、/school/list）      │  │
│  └──────────────────────┬────────────────────────┘  │
│  ┌──────────────────────▼────────────────────────┐  │
│  │ Service 层：业务规则 + 订单状态机 + 事务管理     │  │
│  │ OrderScheduler：超时自动流转（每 60s 扫描）      │  │
│  └──────────────────────┬────────────────────────┘  │
│  ┌──────────────────────▼────────────────────────┐  │
│  │ Mapper 层（MyBatis-Plus）+ Entity + DTO/VO     │  │
│  └──────────────────────┬────────────────────────┘  │
│  ┌──────────────────────▼────────────────────────┐  │
│  │ MySQL（campus_cycle 库，8 张表）                │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

**统一响应**：`Result<T> { code, message, data }`

| code | 含义 | 前端行为 |
|---|---|---|
| 200 | 业务成功 | 返回 data |
| 400 | 参数校验失败 | 弹 message |
| 401 | 未登录/过期 | 弹窗并跳登录页 |
| 403 | 无权限 | 弹 message |
| 404 | 资源不存在 | 弹 message |
| 1000 | 业务规则不允许 | 弹 message（如"未结清手续费"） |
| 500 | 服务器异常 | 兜底提示 |

## 4. 目录结构

```
src/main/java/com/campus/cycle/
├── CampusCycleApplication.java   # 启动类（@MapperScan + @EnableScheduling）
├── common/
│   ├── constant/                 # OrderStatus / GoodsStatus / FeeRules 常量
│   ├── exception/                # BusinessException / UnauthorizedException / 全局异常处理
│   ├── result/                   # Result<T> / ResultCode 统一响应
│   └── util/Assemblers.java      # 实体 → VO 装配器
├── config/
│   ├── WebMvcConfig.java         # 拦截器注册 + CORS
│   ├── MybatisPlusConfig.java    # 分页插件
│   ├── MyMetaObjectHandler.java  # createTime/updateTime 自动填充
│   ├── OpenApiConfig.java        # Swagger 文档信息
│   └── OrderScheduler.java       # 订单超时自动流转定时任务
├── security/
│   ├── JwtUtil.java              # 签发/解析 token
│   ├── AuthInterceptor.java      # header token 拦截
│   └── UserContext.java          # ThreadLocal 当前用户
├── controller/                   # 6 个 REST 控制器
├── service/ + service/impl/      # 接口 + 实现（订单状态机核心）
├── mapper/                       # 8 个 MyBatis-Plus Mapper
├── entity/                       # 8 张表实体
├── dto/                          # 请求体（含参数校验注解）
└── vo/                           # 响应体（对齐前端契约）
src/main/resources/
├── application.yml               # 公共配置（JWT/微信/调度）
├── application-dev.yml           # 开发环境（本机 devuser/123456）
└── application-prod.yml          # 生产环境（环境变量注入）
sql/schema.sql                    # 建库建表 + 学校种子数据
```

## 5. 数据库设计（8 张表）

| 表 | 说明 | 关键字段 |
|---|---|---|
| `t_school` | 学校 | name, short_name, goods_count（冗余） |
| `t_user` | 用户 | openid(唯一), school_id, credit_score, success_count, contact_* |
| `t_goods` | 商品 | seller_id, school_id, price, images(JSON), status, views, want_count |
| `t_review` | 商品评价 | goods_id, order_id(唯一), rate 1-5, content |
| `t_order` | 订单（核心） | status, price, expire_time, appeal_end_time, fee_billed |
| `t_conversation` | 会话 | goods_id, buyer_id, seller_id, last_message, unread_for(JSON) |
| `t_message` | 聊天消息 | conversation_id, from_user_id, content |
| `t_fee_bill` | 手续费账单 | order_id(唯一), rate, amount, status(UNPAID/PAID) |

约定：
- 业务时间戳统一 `BIGINT` 毫秒（与前端 number 契约一致）
- 逻辑删除 `deleted` 字段，MyBatis-Plus `@TableLogic`
- `condition` 为 MySQL 保留字，实体中已反引号转义
- 索引覆盖：订单按买卖双方+状态、商品按学校+状态、消息按会话+时间

## 6. 核心业务设计

### 6.1 订单状态机

```
PENDING_SELLER(待卖家确认) ──卖家确认──▶ PENDING_OFFLINE(待线下交易)
        │ 24h 超时自动过期(不收手续费)        │ 卖家标记交付
        ▼                                   ▼
    CANCELLED                          PENDING_BUYER(待买家确认)
                                              │ 买家确认
                                              ▼
                                        APPEALING(申诉期 48h)
                                        │ 卖家异议 → 平台介入(停留)
                                        │ 无异议到期 ─▶ COMPLETED(已完成)
```

- 状态常量：`PENDING_SELLER / PENDING_OFFLINE / PENDING_BUYER / APPEALING / COMPLETED / CANCELLED`
- 卖家处理截止 = 申请时间 + 24h；申诉期截止 = 买家确认 + 48h
- 取消规则：未进入线下交易阶段（无 buyerConfirmTime）取消时商品恢复在售
- 重复申请拦截：同一买家对同一商品仅一个待确认订单

### 6.2 手续费规则（FeeRules）

| 规则 | 值 |
|---|---|
| 首单 | 免费（amount=0，账单直接记 PAID） |
| 费率 | 成交价 × 6% |
| 下限 / 上限 | 最低 1 元 / 最高 20 元 |
| 生成时机 | 订单最终完成后（completeOrder） |
| 发布限制 | 有未结清账单禁止发布新商品 |

### 6.3 超时自动流转（OrderScheduler）

- 每 60s 扫描一次（`campus.scheduler.order-sweep-interval-ms` 可调）
- 逻辑与订单查询前兜底触发（sweepOrders）双保险
- 卖家 24h 未确认 → CANCELLED（cancelTime=expireTime，不收手续费）
- 申诉期到期且无异议 → COMPLETED + 商品置灰(SOLD) + 生成手续费账单 + 卖家 successCount+1

### 6.4 认证机制

- `POST /auth/login`：微信登录（jsCode → code2session → openid）
- **mock 模式**（`campus.wx.mock=true`，默认）：按 jsCode 直接签发测试 token，首次登录自动建号（昵称"同学+随机数"，默认学校 s_0001），本地联调无需真实微信
- 真实模式：配置 `WX_APPID/WX_SECRET` 后自动走微信接口
- 拦截器从 header `token` 解析 userId，写入 `UserContext`（ThreadLocal，请求结束清理防串号）
- 白名单：`/auth/**`、`/school/list`、`/error`、`/v3/api-docs/**`、`/swagger-ui/**`

## 7. 接口清单（25 个，Swagger 可交互调试）

### 认证
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /auth/login | 微信登录（mock 或真实） |

### 学校
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /school/list | 学校列表（keyword 模糊搜索） |
| POST | /school/confirm | 确认学校（更新当前用户 school_id） |

### 商品
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /goods/list | 本校商品（schoolId + category + keyword，已售出置灰不隐藏） |
| GET | /goods/detail/{id} | 商品详情（含卖家信息与评价） |
| GET | /goods/mine | 我发布的商品 |
| POST | /goods/publish | 发布商品（未结清手续费时禁止） |
| POST | /goods/apply | 买家提交购买申请（创建待确认订单） |
| POST | /goods/conversation/start | 发起站内沟通（复用或新建会话） |

### 订单
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /order/list | 订单列表（role=buyer/seller + status 筛选） |
| GET | /order/detail/{id} | 订单详情（含商品/买卖双方/评价） |
| POST | /order/seller-confirm | 卖家确认 → 待线下交易（展示联系方式） |
| POST | /order/seller-reject | 卖家拒绝申请 |
| POST | /order/seller-delivered | 卖家标记已交付 → 待买家确认 |
| POST | /order/buyer-confirm | 买家确认 → 进入 48h 申诉期 |
| POST | /order/seller-objection | 卖家申诉期提出异议 |
| POST | /order/cancel | 取消订单（双方可，未线下阶段释放商品） |
| POST | /order/review | 买家评价（订单完成后） |

### 站内沟通
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /chat/conversations | 会话列表（对端用户 + 商品 + 未读数） |
| GET | /chat/messages | 聊天记录（进入即清未读） |
| POST | /chat/send | 发送消息（对端未读+1） |

### 手续费
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /fee/summary | 未结清金额/笔数 + 全部账单 |
| GET | /fee/bills | 账单列表 |
| POST | /fee/pay | 支付手续费 |
| GET | /fee/check-publish | 是否可发布（allowed + unpaidAmount） |

> 除白名单外全部接口需请求头 `token: <jwt>`，响应 401 时前端应跳登录。

## 8. 配置与部署

### 8.1 环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| DB_HOST / DB_PORT / DB_NAME | 127.0.0.1 / 3306 / campus_cycle | 数据库 |
| DB_USERNAME / DB_PASSWORD | devuser / 123456 | 数据库账号（dev 默认） |
| CAMPUS_JWT_SECRET | dev 默认值 | 生产必须覆盖，≥32 字节 |
| CAMPUS_JWT_EXPIRE_MS | 2592000000（30 天） | token 有效期 |
| WX_APPID / WX_SECRET | 空 | 微信小程序凭证 |
| WX_MOCK | true | 登录 mock 开关 |
| SERVER_PORT | 8080 | 服务端口 |

### 8.2 启动

```bash
# 1. 建库建表（一次性）
mysql -uroot -p < sql/schema.sql

# 2. 启动（dev 环境默认连接本机 devuser/123456）
mvn spring-boot:run
# 或打包后运行
mvn package -DskipTests
java -jar target/campus-cycle-server-0.0.1-SNAPSHOT.jar

# 3. 验证
curl http://127.0.0.1:8080/v3/api-docs   # OpenAPI JSON
```

### 8.3 前端联调

- 前端 vite 已配置 `/api` 代理 → `http://localhost:8080`，前端请求 `/api/auth/login` 等即可
- 登录响应 `data.token` 存入本地（前端 key：`member_token`），后续请求自动带 `token` header

## 9. 后续规划（待开发）

- 商品图片上传接口（当前 images 仅存 URL，需对象存储对接）
- 微信支付手续费（当前 pay 仅置 PAID）
- 申诉人工处理后台
- 商品浏览量计数
