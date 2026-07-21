# 医院预约挂号系统

<div align="center">

![JDK](https://img.shields.io/badge/JDK-17-blue.svg?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.12-brightgreen.svg?style=flat-square)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-yellow.svg?style=flat-square)
![Spring Cloud Alibaba](https://img.shields.io/badge/Alibaba-2023.0.1.0-orange.svg?style=flat-square)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.5-blue.svg?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue.svg?style=flat-square)
![Redis](https://img.shields.io/badge/Redis-6.x-red.svg?style=flat-square)
![Vue](https://img.shields.io/badge/Vue-2-green.svg?style=flat-square&logo=vue.js)

**基于 Spring Cloud 微服务架构的分布式医疗预约挂号平台，实现医院信息管理、科室排班、线上挂号、微信支付、订单管理全流程数字化**

[核心特性](#-核心特性) · [技术栈](#-技术栈) · [快速开始](#-快速开始) · [项目结构](#-项目结构) · [API 接口](#-api-接口) · [面试考点](#-面试考点)

</div>

---

## 项目介绍

本项目是一个 **分布式医疗预约挂号平台**，基于 Spring Cloud 微服务架构，采用前后端分离设计，包含 7 个微服务 + 1 个独立服务 + 2 个前端应用。

### 前台用户功能（Nuxt.js 用户门户）

- **就诊人管理**：一个用户可添加多个就诊人，支持实名认证与增删改查
- **医院详情**：显示医院基本信息、科室列表、排班日历与号源余量
- **预约挂号**：选择科室 → 选择日期 → 选择号源 → 选择就诊人 → 生成订单 → 微信扫码支付
- **订单管理**：查看订单详情、支付、取消预约、申请退款
- **微信登录**：支持微信开放平台 OAuth2.0 扫码登录

### 后台管理功能（Vue 管理后台）

- **医院设置管理**：医院信息 CRUD、API 签名密钥管理、医院上下线控制
- **医院管理**：医院详情、科室列表、排班数据管理
- **数据字典管理**：省市区、医院等级、科室分类等基础数据 Excel 批量导入导出
- **用户管理**：用户列表、实名认证审批
- **订单管理**：订单列表、退款处理
- **统计管理**：挂号量统计与数据可视化

---

## 核心特性

### 1. 号源高并发防护（三层防护，万级并发零超卖）

````
Redis 原子扣减（DECR） → Redisson 分布式锁 → 数据库乐观锁（UPDATE WHERE available > 0）
````

- **第一层**：Redis `decr` 原子操作预扣减号源，拦截绝大部分并发请求
- **第二层**：Redisson 分布式锁防止同一用户重复提交
- **第三层**：数据库 `UPDATE schedule SET available_number = available_number - 1 WHERE available_number > 0` 兜底

### 2. 微信支付 APIv3 全流程

````
用户下单 → service_order 生成订单 → WeixinController.createNative() 生成支付二维码
         → 用户扫码支付 → WechatCallbackController 接收回调（APIv3 SDK 自动验签解密）
         → 更新订单状态 → 退款（取消预约时调用微信退款接口）
````

- 基于微信官方 `wechatpay-java:0.2.12` SDK（APIv3）
- 支持 Native 扫码支付、支付回调自动验签+解密、退款
- `PaymentInfo` 记录每次支付流水，`RefundInfo` 记录退款流水
- 定时任务 `ScheduleTask` 自动查询未支付订单状态

### 3. JWT 网关统一鉴权

````
客户端请求 → Gateway AuthGlobalFilter → 拦截 /api/**/auth/** 路径
           → 从 Header 提取 token → JwtHelper 解析 JWT
           → 提取 userId 存入 ThreadLocal → 路由转发到对应微服务
````

- 内部接口 `/**/inner/**` 直接拦截，返回 `PERMISSION` 错误
- 未携带 token 或 token 无效返回 `LOGIN_AUTH` 错误
- 服务间调用通过 Feign Header 传递 `token`

### 4. Feign + Sentinel 熔断降级

``java
@FeignClient(value = "service-hosp", fallbackFactory = HospitalFeignClientFallback.class)
``

- 4 个 Feign 客户端均配置 `FallbackFactory` 降级逻辑
- 集成 Sentinel 流量控制，防止服务雪崩
- 服务间通过 `AuthContextHolder` 传递用户上下文

### 5. EasyExcel 数据字典批量导入

- 流式读取 Excel 文件，支持大数据量
- 异步批量插入数据库
- 支持医院信息、科室数据、数据字典的批量导入导出

### 6. 阿里云 OSS 文件上传

- 支持图片上传至阿里云 OSS
- 文件名 UUID 重命名防冲突
- 按日期目录分组存储

---

## 技术栈

### 核心框架

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 语言 | Java | 17 | LTS 版本 |
| 框架 | Spring Boot | 3.2.12 | Servlet + WebFlux |
| 微服务 | Spring Cloud | 2023.0.3 | Gateway / OpenFeign |
| 微服务 | Spring Cloud Alibaba | 2023.0.1.0 | Nacos / Sentinel |
| 注册中心 | Nacos | 2.x | 服务注册与发现、配置中心 |
| 网关 | Spring Cloud Gateway | 3.x | 响应式网关 |

### 数据层

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| ORM | MyBatis-Plus | 3.5.5 | 分页插件、乐观锁插件 |
| 数据库 | MySQL | 8.x | 5 个业务库 |
| 缓存 | Redis | 6.x | 号源缓存、分布式锁、JWT 黑名单 |
| 分布式锁 | Redisson | 3.23.5 | 号源扣减、缓存击穿防护 |
| 消息队列 | RabbitMQ | 3.9.x | 异步解耦 |

### 业务组件

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 支付 | 微信支付 SDK | APIv3 (0.2.12) | Native 支付、回调验签、退款 |
| 文件存储 | 阿里云 OSS | 3.9.1 | 图片上传 |
| API 文档 | springdoc-openapi | 2.6.0 | OpenAPI 3.0 |
| Excel | EasyExcel | 2.2.11 | 批量导入导出 |
| HTTP 客户端 | Apache HttpClient | 4.5.14 | 签名校验请求 |
| JSON | Fastjson | 1.2.83 | 医院接口 JSON 处理 |
| JWT | JJWT | 0.7.0 | Token 签发与验证 |
| 日期 | Joda-Time | 2.10.1 | 日期时间处理 |

### 安全版本覆盖（Spring Boot 3.2.12 BOM）

| 组件 | 覆盖版本 | 说明 |
|------|----------|------|
| Spring Framework | 6.1.15 | 安全补丁 |
| Tomcat | 10.1.34 | 安全补丁 |
| Netty | 4.1.118.Final | 安全补丁 |
| Jackson BOM | 2.17.3 | JSON 序列化 |
| Thymeleaf | 3.1.3.RELEASE | hospital-manage 模板引擎 |

### 前端技术

| 分类 | 技术 | 说明 |
|------|------|------|
| 用户前台 | Nuxt.js 2 + Vue 2 + Element UI | SSR 服务端渲染 |
| 管理后台 | Vue 2 + Element UI + Vuex | SPA 单页应用 |

---

## 服务架构

### 微服务列表

| 服务 | 端口 | 数据库 | 职责 |
|------|------|--------|------|
| ``server_gateway`` | 8888 | 无 | API 网关：路由、JWT 鉴权、跨域处理 |
| ``service_hosp`` | 8201 | ``yygh_hosp`` | 医院/科室/排班管理、签名校验、号源缓存 |
| ``service_user`` | 8160 | ``yygh_user`` | 用户注册登录、就诊人管理、微信 OAuth |
| ``service_order`` | 8206 | ``yygh_order`` | 预约下单、号源扣减、微信支付、退款 |
| ``service_cmn`` | 8202 | ``yygh_cmn`` | 数据字典、EasyExcel 导入导出 |
| ``service_oss`` | 8205 | 无 | 阿里云 OSS 文件上传 |
| ``service_statistics`` | 8208 | 无 | 挂号量统计与数据可视化 |
| ``hospital-manage`` | 9998 | ``yygh_manage`` | 医院后台管理系统（独立服务，Thymeleaf） |

> **注意**：`hospital-manage` 是独立的 Spring Boot 应用，不注册到 Nacos，使用 Thymeleaf 模板引擎，模拟医院端的订单处理和排班管理。

### Feign 远程调用

| Feign 客户端 | 目标服务 | 关键方法 |
|-------------|----------|----------|
| ``DictFeignClient`` | service-cmn | ``getName(dictCode, value)`` |
| ``HospitalFeignClient`` | service-hosp | ``getScheduleOrderVo(id)``, ``getSignInfoVo(hoscode)``, ``updateAvailableNumber(scheduleId, availableNumber)`` |
| ``PatientFeignClient`` | service-user | ``getPatient(id)`` |
| ``OrderFeignClient`` | service-order | ``getCountMap(orderVo)`` |

---

## 数据库设计

### 数据库总览

| 数据库 | 表数量 | 说明 |
|--------|--------|------|
| ``yygh_hosp`` | 1 | 医院设置（对接医院端的签名密钥） |
| ``yygh_user`` | 3 | 用户信息、就诊人、登录记录 |
| ``yygh_order`` | 3 | 订单、支付流水、退款记录 |
| ``yygh_cmn`` | 1 | 数据字典（树形结构，省级 → 医院 → 科室） |
| ``yygh_manage`` | 3 | 医院管理端（医院设置、排班、订单） |

### 表结构详情

#### yygh_hosp（医院服务库）

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| ``hospital_set`` | 医院设置 | id, hoscode, hosname, api_url, sign_key, contacts_phone, status |

#### yygh_user（用户服务库）

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| ``user_info`` | 用户信息 | id, login_name, phone, name, card_type, cert_no, auth_status, status |
| ``patient`` | 就诊人 | id, user_id, name, cert_type, cert_no, sex, birth_date, phone, contacts_name, contacts_phone |
| ``user_login_record`` | 登录记录 | id, user_id, ip, user_agent, auth_type, create_time |

#### yygh_order（订单服务库）

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| ``order_info`` | 订单 | id, order_no, user_id, patient_id, hoscode, hosname, depcode, depname, schedule_id, amount, order_status |
| ``payment_info`` | 支付流水 | id, order_no, transaction_id, trade_no, total_amount, trade_status, callback_content |
| ``refund_info`` | 退款记录 | id, order_no, refund_no, refund_id, refund_status, refund_amount, callback_content |

#### yygh_cmn（数据字典库）

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| ``dict`` | 数据字典（树形） | id, dict_code, parent_id, name, value, dict_id, has_children |

#### yygh_manage（医院管理端库）

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| ``hospital_set`` | 医院设置（镜像） | id, hoscode, hosname, api_url, sign_key |
| ``schedule`` | 排班（镜像） | id, hospital_id, department_id, title, schedule_date, start_time, end_time, available_number |
| ``order_info`` | 订单（镜像） | id, order_no, patient_id, hospital_id, department_id, schedule_id, amount, order_status |

---

## 项目结构

```
yygh-master/
├── 后台代码/                              # 后端全部代码
│   └── yygh_parent/                       # Maven 父工程
│       ├── pom.xml                         # 根 POM（统一版本管理）
│       ├── common/                         # 公共模块
│       │   ├── common_util/                # 公共工具（Result/异常/JWT/MD5）
│       │   └── service_util/               # 服务工具（配置/签名/RabbitMQ/Redis）
│       ├── model/                          # 数据模型
│       │   ├── base/                       #   BaseEntity
│       │   ├── hosp/                       #   Hospital/HospitalSet/Department/Schedule
│       │   ├── user/                       #   UserInfo/Patient/UserLoginRecord
│       │   ├── order/                      #   OrderInfo/PaymentInfo/RefundInfo
│       │   ├── cmn/                        #   Dict
│       │   ├── vo/                         #   22 个 Value Object
│       │   ├── dto/                        #   15 个 Data Transfer Object
│       │   └── enums/                      #   6 个枚举类
│       ├── service_client/                 # Feign 远程调用客户端
│       │   ├── service_cmn_client          #   DictFeignClient
│       │   ├── service_hosp_client         #   HospitalFeignClient
│       │   ├── service_user_client         #   PatientFeignClient
│       │   └── service_order_client        #   OrderFeignClient
│       ├── server_gateway/                 # API 网关 (:8888)
│       │   └── filter/AuthGlobalFilter     #   JWT 鉴权全局过滤器
│       ├── service/                        # 业务微服务
│       │   ├── service_hosp/               # 医院服务 (:8201)
│       │   ├── service_user/               # 用户服务 (:8160)
│       │   ├── service_order/              # 订单服务 (:8206)
│       │   ├── service_cmn/                # 数据字典服务 (:8202)
│       │   ├── service_oss/                # 文件服务 (:8205)
│       │   └── service_statistics/         # 统计服务 (:8208)
│       └── hospital-manage/                # 医院后台 (:9998，独立服务)
│
├── 前台代码/                              # 前端全部代码
│   ├── yygh-site/                          # Nuxt.js 用户前台（SSR）
│   └── vue-admin-template-master/          # Vue 管理后台（SPA）
│
└── 数据库/                                # SQL 初始化脚本
    ├── yygh_cmn.sql                        #   数据字典
    ├── yygh_hosp.sql                       #   医院设置
    ├── yygh_manage.sql                     #   医院管理端
    ├── yygh_order.sql                      #   订单/支付/退款
    └── yygh_user.sql                       #   用户/就诊人/登录记录
```

---

## 快速开始

### 1. 环境准备

| 中间件 | 版本要求 | 用途 |
|--------|----------|------|
| JDK | 17+ | Java 运行环境 |
| Maven | 3.8+ | 项目构建 |
| MySQL | 8.x | 5 个业务数据库 |
| Redis | 6.x+ | 号源缓存、分布式锁 |
| RabbitMQ | 3.9.x | 异步消息 |
| Nacos | 2.x | 服务注册与配置中心 |

### 2. 克隆项目

```bash
git clone https://github.com/xxinjie21/yygh-master.git
cd yygh-master
```

### 3. 初始化数据库

```bash
mysql -u root -p < 数据库/yygh_cmn.sql
mysql -u root -p < 数据库/yygh_hosp.sql
mysql -u root -p < 数据库/yygh_manage.sql
mysql -u root -p < 数据库/yygh_order.sql
mysql -u root -p < 数据库/yygh_user.sql
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
export WECHAT_PRIVATE_KEY_PATH=cert/apiclient_key.pem
export WECHAT_PAY_NOTIFY_URL=https://your-domain/api/order/weixin/callback/notify

# 阿里云 OSS
export ALIYUN_OSS_ACCESS_KEY_ID=your_ak
export ALIYUN_OSS_ACCESS_KEY_SECRET=your_sk
export ALIYUN_OSS_BUCKET=your_bucket
export ALIYUN_OSS_ENDPOINT=your_endpoint

# 微信开放平台（登录）
export WECHAT_OPEN_APP_ID=your_app_id
export WECHAT_OPEN_APP_SECRET=your_app_secret

# JWT 签名密钥
export JWT_SIGN_KEY=your_jwt_secret
```

### 5. 启动服务

按顺序启动：

```
1. mvn clean compile -DskipTests          # 编译整个项目
2. 启动 Nacos 注册中心
3. 启动基础服务（顺序：cmn → hosp → user → order → oss → statistics）
   - service_cmn        :8202
   - service_hosp       :8201
   - service_user       :8160
   - service_order      :8206
   - service_oss        :8205
   - service_statistics :8208
4. 启动网关 server_gateway     :8888
5. 启动医院后台 hospital-manage :9998（独立服务）
6. 启动前端
   - cd 前台代码/yygh-site && npm install && npm run dev
   - cd 前台代码/vue-admin-template-master && npm install && npm run dev
```

---

## API 接口

### service-hosp（医院服务 :8201）

| 路径 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/api/hosp/hospital/list` | GET | 医院分页列表 | 无 |
| `/api/hosp/hospital/{hosCode}` | GET | 医院详情 | 无 |
| `/api/hosp/hospital/department/{hosCode}` | GET | 科室列表 | 无 |
| `/api/hosp/schedule/getScheduleList/{hosCode}/{depId}/{workDate}` | GET | 排班列表 | 无 |
| `/api/hosp/schedule/getSchedule/{scheduleId}` | GET | 排班详情 | 无 |
| `/admin/hosp/hospital/list` | POST | 后台-医院分页查询 | JWT |
| `/admin/hosp/hospital/save` | POST | 后台-新增/修改医院 | JWT |
| `/admin/hosp/hospital/remove/{id}` | DELETE | 后台-删除医院 | JWT |
| `/admin/hosp/hospital/status/{id}/{status}` | GET | 后台-上线/下线 | JWT |
| `/admin/hosp/hospitalSet/list` | POST | 后台-医院设置分页 | JWT |
| `/admin/hosp/hospitalSet/save` | POST | 后台-新增医院设置 | JWT |
| `/admin/hosp/hospitalSet/update` | POST | 后台-修改医院设置 | JWT |
| `/admin/hosp/hospitalSet/remove/{id}` | DELETE | 后台-删除医院设置 | JWT |
| `/admin/hosp/department/{hosCode}` | GET | 后台-科室列表 | JWT |
| `/admin/hosp/schedule/{hosCode}/{depId}` | GET | 后台-排班列表 | JWT |

### service-user（用户服务 :8160）

| 路径 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/api/user/auth/sendCode/{phone}` | GET | 发送验证码 | 无 |
| `/api/user/auth/login/{phone}/{code}` | POST | 手机号验证码登录 | 无 |
| `/api/user/auth/login` | POST | 微信扫码登录 | 无 |
| `/api/user/auth/getLoginStatus` | GET | 查询微信登录状态 | 无 |
| `/api/user/patient/auth/findAll` | GET | 就诊人列表 | JWT |
| `/api/user/patient/auth/getById/{id}` | GET | 就诊人详情 | JWT |
| `/api/user/patient/auth/save` | POST | 添加就诊人 | JWT |
| `/api/user/patient/auth/update` | POST | 修改就诊人 | JWT |
| `/api/user/patient/auth/remove/{id}` | DELETE | 删除就诊人 | JWT |
| `/admin/user/info/list` | POST | 后台-用户列表 | JWT |
| `/admin/user/info/userInfoStatus/{userId}/{status}` | GET | 后台-用户认证审批 | JWT |

### service-order（订单服务 :8206）

| 路径 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/api/order/orderInfo/auth/submitOrder/{scheduleId}/{patientId}` | POST | 提交预约订单 | JWT |
| `/api/order/orderInfo/auth/getOrders/{page}/{limit}` | GET | 用户订单列表 | JWT |
| `/api/order/orderInfo/auth/getOrderInfo/{orderId}` | GET | 订单详情 | JWT |
| `/api/order/orderInfo/auth/cancelOrder/{orderId}` | POST | 取消订单 | JWT |
| `/api/order/weixin/createNative/{orderId}` | GET | 创建微信支付二维码 | JWT |
| `/api/order/weixin/queryPayStatus/{orderId}` | GET | 查询支付状态 | JWT |
| `/api/order/weixin/callback/notify` | POST | 微信支付回调通知 | 无（签名验证） |
| `/admin/order/info/list` | POST | 后台-订单列表 | JWT |
| `/admin/order/info/getCountMap` | POST | 后台-订单统计 | JWT |

### service-cmn（数据字典服务 :8202）

| 路径 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/api/cmn/dict/{dictCode}` | GET | 根据字典编码获取子列表 | 无 |
| `/api/cmn/dict/getName/{dictCode}/{value}` | GET | 根据编码+值获取名称 | 无 |
| `/admin/cmn/dict/list` | GET | 后台-字典列表 | JWT |
| `/admin/cmn/dict/findByDictCode/{dictCode}` | GET | 后台-字典详情 | JWT |
| `/admin/cmn/dict/save` | POST | 后台-新增字典 | JWT |
| `/admin/cmn/dict/update` | POST | 后台-修改字典 | JWT |
| `/admin/cmn/dict/remove/{id}` | DELETE | 后台-删除字典 | JWT |
| `/admin/cmn/dict/importData` | POST | 后台-Excel 导入 | JWT |
| `/admin/cmn/dict/exportData/{dictCode}` | GET | 后台-Excel 导出 | JWT |

### service-oss（文件服务 :8205）

| 路径 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/admin/oss/file/upload` | POST | 上传文件到阿里云 OSS | JWT |

### service-statistics（统计服务 :8208）

| 路径 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/admin/statistics/dailyCount/{day}` | GET | 某日挂号统计 | JWT |
| `/admin/statistics/count/{startDate}/{endDate}` | GET | 区间挂号统计 | JWT |

---

## 枚举与状态码

### OrderStatusEnum（订单状态）

| 枚举值 | 编码 | 说明 |
|--------|------|------|
| INIT | -1 | 初始状态 |
| SUCCESS | 1 | 预约成功 |
| CANCEL | 0 | 取消预约 |
| REFUND | 2 | 退款 |

### PaymentStatusEnum（支付状态）

| 枚举值 | 编码 | 说明 |
|--------|------|------|
| NOT支付 | 0 | 未支付 |
| SUCCESS | 1 | 支付成功 |
| REFUND | 2 | 退款 |

### PaymentTypeEnum（支付方式）

| 枚举值 | 编码 | 说明 |
|--------|------|------|
| WEIXIN | 1 | 微信支付 |

### RefundStatusEnum（退款状态）

| 枚举值 | 编码 | 说明 |
|--------|------|------|
| REFUND | 0 | 退款中 |
| SUCCESS | 1 | 退款成功 |
| FAIL | 2 | 退款失败 |

### AuthStatusEnum（认证状态）

| 枚举值 | 编码 | 说明 |
|--------|------|------|
| NO_AUTH | 0 | 未认证 |
| AUTH_SUCCESS | 1 | 认证成功 |
| AUTH_FAIL | -1 | 认证失败 |

### ResultCodeEnum（统一返回码）

| 枚举值 | 编码 | 说明 |
|--------|------|------|
| SUCCESS | 200 | 成功 |
| ERROR | 201 | 失败 |
| LOGIN_AUTH | 208 | 未登录 |
| PERMISSION | 209 | 无权限 |
| ACCOUNT_ERROR | 1001 | 账号错误 |
| ACCOUNT_DISABLED | 1002 | 账号停用 |
| ACCOUNT_NOT_EXISTS | 1003 | 账号不存在 |
| CODE_ERROR | 1004 | 验证码错误 |
| ACCOUNT_NAME_DISABLED | 1005 | 名字被占用 |
| ACCOUNT_EXISTS | 1006 | 账号已存在 |

---

## 面试考点

### 1. 高并发号源防超卖

**Q: 如何保证号源不超卖？**

> 采用三层防护机制：
> 1. **Redis 原子扣减**：`RedisTemplate.opsForValue().decr()` 原子操作预扣减号源，拦截绝大部分并发请求
> 2. **Redisson 分布式锁**：`CacheBreakdownUtil` 基于 Redisson 的 `RLock` 防止同一用户重复提交
> 3. **数据库乐观锁**：`UPDATE schedule SET available_number = available_number - 1 WHERE available_number > 0` 兜底保证最终一致性
>
> 三层中任意一层拦截即可保证不超卖，实现万级并发下零超卖。

### 2. 微信支付回调幂等

**Q: 支付回调如何保证幂等？**

> 1. **Redis 预校验**：先查询是否已处理过该回调，避免重复处理
> 2. **订单状态校验**：只有待支付状态（INIT）的订单才处理支付回调
> 3. **数据库唯一索引**：`order_no` + `transaction_id` 唯一索引兜底防重
> 4. **APIv3 SDK 自动验签**：`wechatpay-java` SDK 自动完成签名验证和解密，防止伪造回调

### 3. 微服务间鉴权

**Q: 服务间调用如何传递用户身份？**

> 1. Gateway 解析 JWT 后将 `userId` 存入请求头
> 2. Feign 拦截器在请求头中自动传递 `token`
> 3. 各服务通过 `AuthContextHolder`（ThreadLocal）获取当前用户 ID
> 4. 内部接口路径包含 `inner` 关键字，Gateway 直接拦截外部访问

### 4. 缓存击穿防护

**Q: 热点 key 过期如何处理？**

> - `CacheBreakdownUtil` 基于 Redisson 实现
> - 使用分布式锁保证只有一个线程查询数据库并回填缓存
> - 其他线程等待锁释放后读取缓存
> - 防止大量请求同时穿透到数据库

### 5. 医院接口签名校验

**Q: 如何保证医院端接口安全？**

> - 每个医院有独立的 `sign_key`
> - 请求参数按字典序排序后拼接，加 `&secret=` 拼接签名密钥
> - 使用 MD5 对拼接串进行签名
> - `HttpRequestHelper` 统一处理签名验证
> - `hospital-manage` 模拟医院端，使用相同签名算法

### 6. Feign 熔断降级

**Q: 服务间调用失败怎么办？**

> 1. **FallbackFactory**：每个 Feign 客户端配置降级类，返回友好错误信息
> 2. **Sentinel**：流量控制 + 熔断降级，防止服务雪崩
> 3. **合理超时**：Feign 配置连接超时和读取超时
> 4. **重试机制**：可配置重试次数（生产环境建议谨慎使用）

---

## 常见问题

### Q: 服务启动失败？

检查 Nacos、MySQL、Redis、RabbitMQ 是否已启动，检查各服务 `application.properties` 中的数据库连接、Nacos 地址等配置。

### Q: 号源扣减失败？

检查 Redis 是否启动，检查 Redisson 连接配置，查看服务日志中的锁获取情况。

### Q: 微信支付回调失败？

1. 检查微信支付配置（AppID、商户号、APIv3Key）是否正确
2. 检查回调 URL 是否可公网访问（开发环境可使用内网穿透）
3. 检查证书文件 ``cert/apiclient_key.pem`` 是否存在于 classpath

### Q: hospital-manage 无法注册到 Nacos？

`hospital-manage` 是独立服务，设计上不注册到 Nacos。它通过 `HttpRequestHelper` 直接 HTTP 调用 `service_hosp` 的接口（需要签名验证）。

---

## 许可证

仅供学习参考

---

<div align="center">

**如果本项目对你有帮助，请给个 Star 支持！**

</div>
