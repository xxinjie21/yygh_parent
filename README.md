# 电子挂号系统

分布式医疗预约平台，基于 Spring Cloud 微服务架构，实现医院信息管理、科室排班、线上挂号、微信支付、订单管理全流程数字化。

---

## 项目架构

```
yygh_parent/
├── common/                      # 公共模块
│   ├── common_util/             # 公共工具（异常、JWT、Result、短信）
│   └── service_util/            # 服务工具（配置、HTTP签名、RabbitMQ）
├── model/                       # 实体类、VO、枚举
├── service_client/              # Feign 远程调用客户端
│   ├── service_cmn_client/      # 数据字典 Feign
│   ├── service_hosp_client/     # 医院服务 Feign
│   ├── service_order_client/    # 订单服务 Feign
│   └── service_user_client/     # 用户服务 Feign
├── server_gateway/              # API 网关（统一入口、JWT鉴权、路由）
├── service/                     # 业务微服务
│   ├── service_cmn/             # 数据字典服务 :8202
│   ├── service_hosp/            # 医院管理服务 :8201
│   ├── service_order/           # 预约订单服务 :8206（核心）
│   ├── service_oss/             # 文件存储服务 :8205
│   ├── service_statistics/      # 统计服务 :8208
│   └── service_user/            # 用户服务 :8160
├── hospital-manage/             # 医院后台管理系统
├── sql/                         # 数据库初始化脚本
└── project-documentation/       # 项目文档
```

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.12 |
| 微服务 | Spring Cloud | 2023.0.3 |
| 微服务 | Spring Cloud Alibaba | 2023.0.1.0 |
| 注册中心 | Nacos | 2.x |
| 网关 | Spring Cloud Gateway | 3.x |
| ORM | MyBatis-Plus | 3.5.3.1 |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | 6.x |
| 分布式锁 | Redisson | 3.23.5 |
| 消息队列 | RabbitMQ | 3.9.x |
| 服务调用 | OpenFeign + Sentinel + FallbackFactory | - |
| 支付 | 微信支付官方SDK (wechatpay-java v0.2.12) | APIv3 |
| API文档 | springdoc-openapi | 2.6.0 |
| Excel | EasyExcel | 2.2.0-beta2 |
| 认证 | JWT (jjwt 0.7.0) | - |

## 服务职责

| 服务 | 端口 | 职责 |
|------|------|------|
| `server_gateway` | 8888 | API网关：请求路由、JWT认证、跨域处理 |
| `service_user` | 8160 | 用户服务：注册登录、就诊人管理、微信OAuth |
| `service_hosp` | 8201 | 医院服务：医院/科室/排班管理、签名校验 |
| `service_order` | 8206 | 订单服务：预约下单、号源扣减、微信支付、退款 |
| `service_cmn` | 8202 | 公共服务：数据字典、EasyExcel导入导出 |
| `service_oss` | 8205 | 文件服务：阿里云OSS图片上传 |
| `service_statistics` | 8208 | 统计服务：订单数据统计 |
| `hospital-manage` | 9998 | 医院后台：医院端订单处理、排班管理 |

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.x
- Redis 6.x
- RabbitMQ 3.9.x
- Nacos 2.x

### 1. 初始化数据库

执行 `sql/` 目录下的SQL脚本创建数据库和表：

```bash
mysql -u root -p < sql/yygh_cmn.sql
mysql -u root -p < sql/yygh_hosp.sql
mysql -u root -p < sql/yygh_manage.sql
mysql -u root -p < sql/yygh_order.sql
mysql -u root -p < sql/yygh_user.sql
```

### 2. 配置环境变量

```bash
# 数据库
export DB_USERNAME=root
export DB_PASSWORD=your_password

# 微信支付（APIv3）
export WECHAT_APPID=your_appid
export WECHAT_PARTNER=your_mchid
export WECHAT_API_V3_KEY=your_api_v3_key
export WECHAT_MERCHANT_SERIAL_NUMBER=your_serial_no
export WECHAT_PAY_NOTIFY_URL=https://your-domain/api/order/weixin/callback/notify

# 阿里云OSS
export ALIYUN_OSS_ACCESS_KEY_ID=your_ak
export ALIYUN_OSS_ACCESS_KEY_SECRET=your_sk
export ALIYUN_OSS_BUCKET=your_bucket

# 微信开放平台（登录）
export WECHAT_OPEN_APP_ID=your_app_id
export WECHAT_OPEN_APP_SECRET=your_app_secret

# JWT签名密钥
export JWT_SIGN_KEY=your_jwt_secret
```

### 3. 启动服务

按顺序启动：Nacos → 基础服务 → 网关 → 后台

```bash
# 1. 编译
mvn clean compile

# 2. 启动基础服务
# service_cmn → service_hosp → service_user → service_order → service_oss → service_statistics

# 3. 启动网关
# server_gateway

# 4. 启动医院后台
# hospital-manage
```

## 核心特性

### 号源高并发防护
Redis 原子扣减 + Redisson 分布式锁 + 乐观锁三层防护，万级并发零超卖。

### 微信支付 APIv3
基于微信官方 `wechatpay-java` SDK，支持 Native 扫码支付、支付回调验签、退款，搭配 RabbitMQ + 定时任务实现支付幂等与对账补偿。

### JWT 网关鉴权
Gateway 全局 Filter + JWT Token，统一拦截未授权请求，搭配接口签名校验。

### Feign 熔断降级
全部 Feign 客户端配置 FallbackFactory，集成 Sentinel 流量控制，防止服务雪崩。

### 构造函数注入
全项目使用 `@RequiredArgsConstructor` + `final` 实现构造函数注入，消除 `@Autowired` 字段注入。

### 统一 JSON 通信
全项目 HTTP 接口统一使用 `@RequestBody` JSON 格式接收参数，前端请求头 `token` 传递 JWT，无需 `HttpServletRequest` 解析。

### springdoc-openapi 文档
替代已停更的 springfox，使用 springdoc-openapi 生成 OpenAPI 3.0 文档，访问 `/swagger-ui/index.html` 和 `/v3/api-docs`。

### EasyExcel 批量导入
流式读取 Excel，异步批量插入，支持大数据量医院/科室数据导入。

## 项目文档

- [技术亮点](project-documentation/docs/technical-highlights.md)
- [项目阅读指南](project-documentation/docs/how-to-read-project.md)
- [面试题与答案](project-documentation/docs/interview-questions.md)

---

**开发周期**：2026.03 - 2026.05  
**作者**：XXJ
