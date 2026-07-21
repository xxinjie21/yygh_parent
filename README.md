# 医院预约挂号系统

<div align="center">

![JDK](https://img.shields.io/badge/JDK-17-blue.svg?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg?style=flat-square)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023-yellow.svg?style=flat-square)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-orange.svg?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue.svg?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-6.x-red.svg?style=flat-square)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.9-green.svg?style=flat-square)
![Vue](https://img.shields.io/badge/Vue-2-green.svg?style=flat-square&logo=vue.js)

**基于 Spring Cloud 微服务架构的分布式医疗预约挂号平台，实现医院信息管理、科室排班、线上挂号、微信支付、订单管理全流程数字化**

[核心特性](#-核心特性) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [项目结构](#-项目结构) • [面试考点](#-面试考点)

</div>

---

## 项目介绍

本项目是一个 **分布式医疗预约挂号平台**，基于 Spring Cloud 微服务架构，实现医院信息管理、科室排班、线上挂号、微信支付、订单管理全流程数字化。

### 前台用户功能

- **就诊人管理**：一个用户可以添加多个就诊人，支持增删改操作
- **医院详情**：显示医院基本信息、科室列表、排班信息
- **预约挂号**：选择科室 → 选择号源 → 选择就诊人 → 生成订单 → 微信支付
- **订单管理**：查看订单详情、支付、取消预约、退款

### 后台管理功能

- **医院设置管理**：医院信息 CRUD、医院上下线控制
- **数据管理**：Excel 批量导入导出
- **用户管理**：用户列表、实名认证审批
- **统计管理**：挂号量统计

---

## 核心特性

### 1. 号源高并发防护

```
Redis 原子扣减 → Redisson 分布式锁 → 数据库乐观锁（三层防护，万级并发零超卖）
```

### 2. 微信支付 APIv3

```
用户下单 → 生成支付二维码 → 用户扫码支付 → 支付回调验签 → 更新订单状态
                                         → 退款（取消预约时）
```

- 基于微信官方 `wechatpay-java` SDK
- 支持 Native 扫码支付、支付回调验签、退款
- RabbitMQ + 定时任务实现支付幂等与对账补偿

### 3. JWT 网关鉴权

```
请求 → Gateway 全局 Filter → JWT Token 校验 → 路由转发到对应服务
```

- Gateway 统一拦截未授权请求
- 接口签名校验

### 4. Feign 熔断降级

- 全部 Feign 客户端配置 FallbackFactory
- 集成 Sentinel 流量控制
- 防止服务雪崩

### 5. EasyExcel 批量导入

- 流式读取 Excel
- 异步批量插入
- 支持大数据量医院/科室数据导入

---

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
| 服务调用 | OpenFeign + Sentinel | - |
| 支付 | 微信支付官方 SDK (wechatpay-java) | APIv3 |
| API 文档 | springdoc-openapi | 2.6.0 |
| Excel | EasyExcel | 2.2.0-beta2 |
| 认证 | JWT (jjwt 0.7.0) | - |
| 前端 | Vue 2 + Nuxt + Element UI | - |

### 服务职责

| 服务 | 端口 | 职责 |
|------|------|------|
| `server_gateway` | 8888 | API 网关：请求路由、JWT 认证、跨域处理 |
| `service_user` | 8160 | 用户服务：注册登录、就诊人管理、微信 OAuth |
| `service_hosp` | 8201 | 医院服务：医院/科室/排班管理、签名校验 |
| `service_order` | 8206 | 订单服务：预约下单、号源扣减、微信支付、退款 |
| `service_cmn` | 8202 | 公共服务：数据字典、EasyExcel 导入导出 |
| `service_oss` | 8205 | 文件服务：阿里云 OSS 图片上传 |
| `service_statistics` | 8208 | 统计服务：订单数据统计 |
| `hospital-manage` | 9998 | 医院后台：医院端订单处理、排班管理 |

---

## 快速开始

### 1. 环境准备

```bash
# JDK 17+
java -v

# Maven 3.6+
mvn -v
```

需要安装以下中间件：
- MySQL 8.x
- Redis 6.x
- RabbitMQ 3.9.x
- Nacos 2.x

### 2. 克隆项目

```bash
git clone https://github.com/xxinjie21/yygh-master.git
cd yygh-master
```

### 3. 初始化数据库

```bash
mysql -u root -p < sql/yygh_cmn.sql
mysql -u root -p < sql/yygh_hosp.sql
mysql -u root -p < sql/yygh_manage.sql
mysql -u root -p < sql/yygh_order.sql
mysql -u root -p < sql/yygh_user.sql
```

### 4. 配置环境变量

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

# 阿里云 OSS
export ALIYUN_OSS_ACCESS_KEY_ID=your_ak
export ALIYUN_OSS_ACCESS_KEY_SECRET=your_sk
export ALIYUN_OSS_BUCKET=your_bucket

# 微信开放平台（登录）
export WECHAT_OPEN_APP_ID=your_app_id
export WECHAT_OPEN_APP_SECRET=your_app_secret

# JWT 签名密钥
export JWT_SIGN_KEY=your_jwt_secret
```

### 5. 启动服务

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

---

## 项目结构

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

### 前台项目

```
前台代码/
├── yygh-site/                   # Nuxt 用户前台
├── vue-admin-template-master/   # Vue 管理后台
└── images/                      # 截图
```

---

## 技术特点

| 特点 | 说明 |
|------|------|
| **号源高并发防护** | Redis 原子扣减 + Redisson 分布式锁 + 乐观锁三层防护 |
| **微信支付 APIv3** | 官方 SDK + 回调验签 + 退款 + 对账补偿 |
| **JWT 网关鉴权** | Gateway 全局 Filter + JWT Token 统一拦截 |
| **Feign 熔断降级** | FallbackFactory + Sentinel 流量控制 |
| **构造函数注入** | `@RequiredArgsConstructor` + `final` 消除 `@Autowired` |
| **统一 JSON 通信** | `@RequestBody` JSON 格式，请求头 `token` 传递 JWT |
| **springdoc-openapi** | 替代 springfox，生成 OpenAPI 3.0 文档 |
| **EasyExcel 批量导入** | 流式读取 + 异步批量插入 |

---

## 面试考点

### 1. 高并发号源

**Q1: 如何保证号源不超卖？**

**参考答案**：
> 1. **Redis 原子扣减**：DECR 命令原子操作
> 2. **Redisson 分布式锁**：防止并发重复扣减
> 3. **数据库乐观锁**：UPDATE 时加库存条件
> 4. **三层防护**：任一层拦截即可保证准确

### 2. 微信支付

**Q2: 支付回调如何保证幂等？**

**参考答案**：
> 1. **Redis 预校验**：先查是否已处理过该回调
> 2. **订单状态校验**：只有待支付状态才处理
> 3. **数据库唯一索引**：兜底防重
> 4. **对账补偿**：定时任务兜底

### 3. 微服务架构

**Q3: 服务间调用失败怎么办？**

**参考答案**：
> 1. **FallbackFactory**：Feign 降级逻辑
> 2. **Sentinel**：流量控制 + 熔断
> 3. **重试机制**：配置合理重试次数
> 4. **超时控制**：避免长耗时调用

---

## 常见问题

### Q: 服务启动失败？

检查 Nacos、MySQL、Redis、RabbitMQ 是否启动，检查各服务 `application.yml` 配置。

### Q: 号源扣减失败？

检查 Redis 是否启动，检查 Redisson 配置，查看服务日志。

### Q: 微信支付回调失败？

检查微信支付配置（AppID、商户号、APIv3Key），检查回调 URL 是否可访问。

---

## 许可证

仅供学习参考

---

<div align="center">

**如果本项目对你有帮助，请给个 Star 支持！**

</div>
