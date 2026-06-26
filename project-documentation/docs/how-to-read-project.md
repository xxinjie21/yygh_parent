# 医院预约挂号系统 - 项目阅读指南

## 目录

| 章节 | 内容 | 预计时间 |
| :--- | :--- | :--- |
| 第一章 | 项目结构概览 | 30分钟 |
| 第二章 | 开发环境与依赖 | 30分钟 |
| 第三章 | 核心服务分析（上） | 2小时 |
| 第四章 | 核心服务分析（下） | 2小时 |
| 第五章 | 关键技术实现 | 3小时 |
| 第六章 | 业务流程追踪 | 2小时 |
| 第七章 | 总结与扩展 | 1小时 |

---

## 第一章：项目结构概览

### 1.1 项目整体架构

```
yygh_parent/                           # Maven 父工程
├── common/                            # 公共模块
│   └── service_util/                  # 工具类、通用配置
├── server_gateway/                    # API 网关（统一入口）
└── service/                           # 业务服务
    ├── service_cmn/                   # 公共数据服务
    ├── service_hosp/                  # 医院管理服务
    ├── service_order/                 # 订单服务（核心）
    ├── service_oss/                   # 文件存储服务
    └── service_user/                  # 用户服务
```

### 1.2 服务职责划分

| 服务模块 | 职责描述 | 核心功能 |
| :--- | :--- | :--- |
| `server_gateway` | API 网关 | 请求路由、认证授权、限流 |
| `service_user` | 用户服务 | 用户注册、登录、短信验证 |
| `service_hosp` | 医院服务 | 医院、科室、医生、排班管理 |
| `service_order` | 订单服务 | 预约订单、库存扣减、微信支付 |
| `service_cmn` | 公共服务 | 数据字典、Excel导入导出 |
| `service_oss` | 文件服务 | 图片上传、文件管理 |

### 1.3 技术栈清单

| 分类 | 技术 | 版本 |
| :--- | :--- | :--- |
| 语言 | Java | 8+ |
| 框架 | Spring Boot | 2.7.x |
| 微服务 | Spring Cloud Alibaba | 2021.x |
| 注册中心 | Nacos | 2.x |
| 网关 | Spring Cloud Gateway | 3.x |
| ORM | MyBatis-Plus | 3.5.x |
| 缓存 | Redis | 6.x |
| 分布式锁 | Redisson | 3.x |
| 消息队列 | RabbitMQ | 3.9.x |
| 数据库 | MySQL | 8.x |
| Excel处理 | EasyExcel | 3.x |
| 支付 | 微信支付 SDK | 3.x |

---

## 第二章：开发环境与依赖

### 2.1 环境准备

**必须安装的服务：**
1. **JDK 8+** - Java 开发环境
2. **MySQL 8.x** - 关系型数据库
3. **Redis 6.x** - 缓存服务
4. **RabbitMQ 3.9.x** - 消息队列
5. **Nacos 2.x** - 服务注册中心

### 2.2 依赖关系分析

查看 `pom.xml` 了解项目依赖：

**父工程依赖管理**：
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.5</version>
</parent>

<dependencyManagement>
    <dependencies>
        <!-- Spring Cloud Alibaba -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2021.0.4.0</version>
        </dependency>
        <!-- Spring Cloud -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2021.0.4</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**参考文件**：[pom.xml](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/pom.xml)

### 2.3 配置文件说明

每个服务都有 `application.yml` 配置文件，包含：
- 数据库连接配置
- Nacos 注册配置
- Redis 配置
- RabbitMQ 配置

**关键配置示例**（service_user）：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
  datasource:
    url: jdbc:mysql://localhost:3306/yygh_user?useSSL=false
    username: root
    password: password
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
```

---

## 第三章：核心服务分析（上）

### 3.1 从网关开始 - server_gateway

**为什么先看网关？**
- 网关是所有请求的入口
- 理解认证授权机制
- 了解路由规则

**关键文件**：

| 文件 | 作用 |
| :--- | :--- |
| `AuthGlobalFilter.java` | JWT 认证过滤器 |
| `application.yml` | 路由配置 |

**核心代码分析**：
```java
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getURI().getPath();
    
    // 跳过不需要认证的路径
    if(antPathMatcher.match("/api/**/auth/**", path)) {
        Long userId = this.getUserId(request);
        if(StringUtils.isEmpty(userId)) {
            return out(response, ResultCodeEnum.LOGIN_AUTH);
        }
    }
    return chain.filter(exchange);
}
```

**参考文件**：[AuthGlobalFilter.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/server_gateway/src/main/java/com/atguigu/yygh/gateway/filter/AuthGlobalFilter.java)

### 3.2 用户服务 - service_user

**核心功能**：
- 用户注册/登录
- 短信验证码发送
- 用户信息管理

**关键文件结构**：
```
service_user/
├── controller/          # REST API 控制层
│   └── UserInfoController.java
├── service/             # 业务逻辑层
│   ├── UserInfoService.java
│   └── impl/UserInfoServiceImpl.java
├── mapper/              # 数据访问层
│   └── UserInfoMapper.java
├── entity/              # 数据库实体
│   └── UserInfo.java
├── receiver/            # 消息消费者
│   └── SmsReceiver.java
└── utils/               # 工具类
    ├── HttpClientUtils.java
    └── JwtHelper.java
```

**用户登录流程**：
```
POST /api/user/auth/login
    ↓
UserInfoController.login()
    ↓
UserInfoServiceImpl.login()
    ↓
验证手机号和验证码
    ↓
生成 JWT Token
    ↓
返回用户信息 + Token
```

**参考文件**：[UserInfoServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_user/src/main/java/com/atguigu/yygh/user/service/impl/UserInfoServiceImpl.java)

### 3.3 公共服务 - service_cmn

**核心功能**：
- 数据字典管理
- Excel 数据导入导出

**数据字典设计**：
```java
@Data
@TableName("dict")
public class Dict {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;      // 父节点ID
    private String name;        // 名称
    private String value;       // 值
    private Integer dictCode;   // 编码
}
```

**参考文件**：[Dict.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_cmn/src/main/java/com/atguigu/yygh/cmn/entity/Dict.java)

---

## 第四章：核心服务分析（下）

### 4.1 医院服务 - service_hosp

**核心功能**：
- 医院信息管理
- 科室管理
- 医生管理
- 排班管理

**关键实体关系**：
```
Hospital (医院)
    ↓ 1:N
Department (科室)
    ↓ 1:N
Doctor (医生)
    ↓ 1:N
Schedule (排班)
    ↓ 1:N
Order (订单)
```

**排班管理核心代码**：
```java
public IPage<Schedule> selectPage(Long page, Long limit, ScheduleQueryVo scheduleQueryVo) {
    QueryWrapper<Schedule> wrapper = new QueryWrapper<>();
    
    if(StringUtils.isNotEmpty(scheduleQueryVo.getHoscode())) {
        wrapper.eq("hoscode", scheduleQueryVo.getHoscode());
    }
    if(StringUtils.isNotEmpty(scheduleQueryVo.getDepcode())) {
        wrapper.eq("depcode", scheduleQueryVo.getDepcode());
    }
    
    return scheduleMapper.selectPage(new Page<>(page, limit), wrapper);
}
```

**参考文件**：[ScheduleServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_hosp/src/main/java/com/atguigu/yygh/hosp/service/impl/ScheduleServiceImpl.java)

### 4.2 文件服务 - service_oss

**核心功能**：
- 图片上传
- 文件管理

**上传流程**：
```java
@PostMapping("/fileUpload")
public Result fileUpload(MultipartFile file) {
    String url = fileService.upload(file);
    return Result.ok(url);
}
```

**参考文件**：[FileApiController.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_oss/src/main/java/com/atguigu/yygh/oss/controller/FileApiController.java)

### 4.3 订单服务 - service_order（核心）

**核心功能**：
- 订单创建
- 库存扣减
- 微信支付
- 订单状态管理

**关键文件结构**：
```
service_order/
├── controller/
│   ├── OrderController.java
│   └── WeixinController.java
├── service/
│   ├── OrderService.java
│   └── impl/OrderServiceImpl.java
├── mapper/
│   └── OrderMapper.java
└── entity/
    └── Order.java
```

**订单状态流转**：
```
待支付 → 已支付 → 已就诊 → 已完成
    ↓           ↓
    └── 已取消 ←──
```

---

## 第五章：关键技术实现

### 5.1 JWT 认证机制

**Token 生成**：
```java
public String createToken(Long userId, String userName) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("userName", userName);
    
    return Jwts.builder()
            .setClaims(claims)
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
            .compact();
}
```

**参考文件**：[JwtHelper.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/common/service_util/src/main/java/com/atguigu/yygh/common/jwt/JwtHelper.java)

### 5.2 高并发库存扣减

**三层防护机制**：

1. **Redis 原子扣减**：
```java
RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
boolean success = atomicLong.compareAndSet(expected, expected - 1);
```

2. **分布式锁**：
```java
RLock lock = redissonClient.getLock("order:lock:" + orderId);
try {
    lock.lock();
    // 业务逻辑
} finally {
    lock.unlock();
}
```

3. **数据库乐观锁**：
```java
@Version
private Integer version;
```

**参考文件**：[OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/atguigu/yygh/order/service/impl/OrderServiceImpl.java)

### 5.3 RabbitMQ 消息处理

**消息发送**：
```java
rabbitTemplate.convertAndSend(exchange, routingKey, message);
```

**消息消费**：
```java
@RabbitListener(queues = "SMS_QUEUE")
public void handleMessage(String messageBody, Message message, Channel channel) {
    try {
        processMessage(messageBody);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    }
}
```

**参考文件**：[SmsReceiver.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_user/src/main/java/com/atguigu/yygh/user/receiver/SmsReceiver.java)

### 5.4 微信支付集成

**支付流程**：
```
1. 统一下单 → 获取 prepay_id
2. 生成支付签名 → 返回前端
3. 前端调起微信支付
4. 支付回调 → 更新订单状态
```

**回调处理**：
```java
@PostMapping("/callback/notify")
public String callbackNotify(HttpServletRequest request) {
    // 验证签名
    // 解析支付结果
    // 更新订单状态
    return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
}
```

**参考文件**：[WeixinController.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/atguigu/yygh/order/controller/WeixinController.java)

### 5.5 Redis 缓存策略

**缓存使用场景**：
- 用户 Token（过期时间 2 小时）
- 号源库存（实时更新）
- 数据字典（定期刷新）

**Cache-Aside 策略**：
```java
// 读取
public UserInfo getUser(Long userId) {
    String key = "user:" + userId;
    UserInfo user = redisTemplate.opsForValue().get(key);
    if(user == null) {
        user = userMapper.selectById(userId);
        redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
    }
    return user;
}

// 更新
public void updateUser(UserInfo user) {
    userMapper.updateById(user);
    redisTemplate.delete("user:" + user.getId());
}
```

---

## 第六章：业务流程追踪

### 6.1 预约挂号完整流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant Gateway as API Gateway
    participant Order as OrderService
    participant Schedule as ScheduleService
    participant Redis as Redis
    participant DB as MySQL
    participant RabbitMQ as RabbitMQ
    participant SMSService as 短信服务

    User->>Gateway: POST /api/order/auth/submitOrder
    Gateway->>Gateway: JWT认证
    Gateway->>Order: submitOrder(orderVO)
    
    Order->>Redis: 获取号源库存
    Redis-->>Order: 返回库存数量
    
    Order->>Redis: 原子扣减库存
    alt 扣减成功
        Redis-->>Order: 扣减成功
        
        Order->>DB: 创建订单
        DB-->>Order: 订单创建成功
        
        Order->>RabbitMQ: 发送订单消息
        RabbitMQ-->>Order: 消息发送成功
        
        Order-->>Gateway: 返回订单信息
        Gateway-->>User: {"code":200,"data":{...}}
        
        RabbitMQ->>SMSService: 异步发送短信
        SMSService-->>RabbitMQ: 确认收到
        
    else 扣减失败
        Redis-->>Order: 扣减失败
        Order-->>Gateway: 返回失败
        Gateway-->>User: {"code":500,"message":"号源不足"}
    end
```

### 6.2 支付流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant Gateway as API Gateway
    participant Order as OrderService
    participant Weixin as 微信支付
    participant DB as MySQL
    participant RabbitMQ as RabbitMQ

    User->>Gateway: POST /api/order/auth/pay/weixin/{orderId}
    Gateway->>Order: createWxPay(orderId)
    
    Order->>DB: 查询订单
    DB-->>Order: 返回订单
    
    Order->>Weixin: 统一下单API
    Weixin-->>Order: prepay_id
    
    Order->>Gateway: 返回支付参数
    Gateway-->>User: {"prepayId":"xxx","sign":"xxx"}
    
    User->>Weixin: 微信客户端支付
    Weixin-->>User: 支付成功
    
    Weixin->>Gateway: POST /api/order/wxPay/callback/notify
    Gateway->>Order: handlePayCallback(data)
    
    Order->>DB: 更新订单状态为已支付
    DB-->>Order: 更新成功
    
    Order->>Order: 扣减数据库库存
    
    Order->>RabbitMQ: 发送支付成功消息
    
    Order-->>Gateway: 返回SUCCESS
    Gateway-->>Weixin: <xml>SUCCESS</xml>
```

### 6.3 退号流程

```
1. 用户发起退号请求
2. 验证订单状态（必须是已支付）
3. 验证退号时间（就诊前2小时）
4. 更新订单状态为已取消
5. 回补课源（Redis + MySQL）
6. 发起微信退款
7. 发送退号通知
```

---

## 第七章：总结与扩展

### 7.1 项目亮点总结

| 技术点 | 实现方式 | 解决的问题 |
| :--- | :--- | :--- |
| 高并发 | Redis原子扣减 + Redisson锁 + 乐观锁 | 号源超卖问题 |
| 认证授权 | JWT + Gateway Filter | 统一认证 |
| 异步处理 | RabbitMQ消息队列 | 短信发送解耦 |
| 缓存策略 | Cache-Aside + 延迟双删 | 数据一致性 |
| 分布式事务 | 最终一致性 + 补偿机制 | 跨服务数据一致 |
| Excel导入 | EasyExcel | 大数据量导入 |

### 7.2 代码优化建议

**潜在优化点**：
1. **引入 Sentinel 限流**：防止恶意请求
2. **添加接口文档**：使用 Swagger/OpenAPI
3. **引入链路追踪**：使用 SkyWalking
4. **配置中心**：使用 Nacos Config
5. **单元测试**：增加测试覆盖率

### 7.3 扩展功能建议

**可新增功能**：
1. **候补排队**：号源释放时自动通知候补用户
2. **家庭账户**：一人绑定多个家庭成员
3. **信用积分**：预约履约记录积分
4. **智能推荐**：根据历史记录推荐医生
5. **在线问诊**：图文/视频问诊功能

### 7.4 学习路线建议

| 阶段 | 目标 | 时间 |
| :--- | :--- | :--- |
| 第一周 | 理解项目结构和基础服务 | 5天 |
| 第二周 | 深入订单服务和支付流程 | 5天 |
| 第三周 | 掌握高并发和分布式技术 | 5天 |
| 第四周 | 动手实践和功能扩展 | 5天 |

---

## 附录：快速定位表

| 功能 | 文件位置 |
| :--- | :--- |
| JWT 认证 | [JwtHelper.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/common/service_util/src/main/java/com/atguigu/yygh/common/jwt/JwtHelper.java) |
| 订单创建 | [OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/atguigu/yygh/order/service/impl/OrderServiceImpl.java) |
| 库存扣减 | [OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/atguigu/yygh/order/service/impl/OrderServiceImpl.java) |
| 微信支付 | [WeixinController.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/atguigu/yygh/order/controller/WeixinController.java) |
| 消息消费 | [SmsReceiver.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_user/src/main/java/com/atguigu/yygh/user/receiver/SmsReceiver.java) |
| 数据字典 | [DictServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_cmn/src/main/java/com/atguigu/yygh/cmn/service/impl/DictServiceImpl.java) |
| 排班管理 | [ScheduleServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_hosp/src/main/java/com/atguigu/yygh/hosp/service/impl/ScheduleServiceImpl.java) |
| 认证过滤 | [AuthGlobalFilter.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/server_gateway/src/main/java/com/atguigu/yygh/gateway/filter/AuthGlobalFilter.java) |

---

**阅读建议**：从 `server_gateway` 开始，理解请求入口和认证机制，然后依次阅读 `service_user`、`service_hosp`，最后重点分析 `service_order`。每个服务先看 Controller 了解 API，再看 Service 理解业务逻辑，最后看 Mapper 了解数据访问。