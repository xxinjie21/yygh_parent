# 医院预约挂号系统 - 面试题与答案

---

## 一分钟项目介绍（面试开场白）

**分布式医疗预约挂号平台**，基于 Spring Cloud 微服务架构，拆分为用户、号源、订单、支付、排班五大微服务，实现从科室查询、线上预约、微信支付到订单对账的全流程数字化。

**技术栈**：Java 17 + Spring Boot 3.2 + Spring Cloud Gateway + Nacos + Sentinel + OpenFeign + MyBatis-Plus + MySQL + Redis + Redisson + RabbitMQ + EasyExcel + JWT

**我在其中负责后端全模块开发，核心实现了：**

1. **号源高并发三层防护**：RLock 分布式锁 + RAtomicLong 原子扣减 + MyBatis-Plus 乐观锁，防止超卖
2. **支付订单最终一致性**：接口幂等校验 + RabbitMQ 异步消息 + 定时对账补偿，保障数据一致
3. **网关统一鉴权**：JWT + Gateway 全局过滤器 + 接口签名校验，拦截越权请求
4. **Feign 熔断降级**：全部 Feign 客户端实现 FallbackFactory + Sentinel 流量控制，防止级联故障
5. **EasyExcel 批量导入**：流式读取 + MyBatis-Plus saveBatch 批量插入，大数据量高效处理
6. **缓存防护体系**：TTL 随机化防雪崩 + 逻辑过期 + Redisson 互斥锁防击穿，降低 DB 压力

---

## 详细项目介绍

### 项目背景

一个面向患者的在线医疗预约平台。传统医院挂号依赖线下窗口，患者排队时间长、号源信息不透明。平台将医院信息、科室排班、预约挂号、支付退款全链路线上化，提升就医效率。

### 技术架构

```
前端（Vue/Nuxt） → Gateway（8888） → Nacos 服务注册发现
                    ↓
  ┌────────┬────────┬────────┬────────┬──────────┐
  │用户服务│医院服务│订单服务│支付服务│数据字典服务│
  │ 8160   │ 8201   │ 8206   │ 8206   │ 8202      │
  └────────┴────────┴────────┴────────┴──────────┘
        ↓         ↓         ↓         ↓
     MySQL    Redis    RabbitMQ   阿里云OSS
     (分库)   (缓存/锁) (消息队列)  (文件存储)
```

| 服务 | 端口 | 职责 |
|------|------|------|
| service_user | 8160 | 用户登录、微信 OAuth、就诊人 CRUD |
| service_hosp | 8201 | 医院/科室/排班管理、对外医院 API |
| service_order | 8206 | 预约下单、号源扣减、微信支付、退款 |
| service_cmn | 8202 | 数据字典（省份、医院等级、证件类型） |
| server_gateway | 8888 | API 网关 — 路由转发、JWT 鉴权、CORS |
| hospital-manage | 9998 | 医院后台 — 模拟医院端订单/排班管理 |

### 核心技术实现

**号源高并发三层防护（核心亮点）**

医院放号瞬间大量用户同时抢号，采用三层防护防止超卖：
- **第一层 RLock 分布式锁**：按排班编号加锁，同一排班串行化，不同排班互不阻塞
- **第二层 RAtomicLong CAS 原子扣减**：`addAndGet(-1)`，结果 < 0 则 `addAndGet(1)` 回退
- **第三层 @Version 乐观锁兜底**：数据库层最后防线

**支付最终一致性**

微信支付异步回调 → 本地订单更新，通过三层保障：
- **幂等校验**：`selectCount` 检查 `outTradeNo`，已处理直接跳过
- **RabbitMQ 异步通知**：订单状态变更通过 MQ 异步同步，手动 ACK 防丢失
- **定时对账补偿**：每 10 分钟取消超时订单 + 每日凌晨 2 点与微信对账

**缓存防护体系**

- **雪崩防护**：Redis TTL 600~780s 随机化，防止大量缓存同时过期
- **击穿防护**：`CacheBreakdownUtil` 逻辑过期 + Redisson 互斥锁，过期后单个线程重建、其他返回旧数据
- **更新策略**：Cache-Aside 模式，`@Cacheable` 读、`@CacheEvict` 写时删缓存

**EasyExcel 批量导入**

`DictListener` 继承 `AnalysisEventListener` 流式逐行解析 → `saveBatch()` 批量写入，避免 OOM 和逐行 insert 性能瓶颈。

### 项目难点与收获

1. **并发控制**：从单机锁到 Redis 原子操作到 Redisson 分布式锁，理解不同粒度锁的适用场景
2. **分布式一致性**：理解 CAP 理论在支付场景的取舍，采用最终一致性 + 补偿机制
3. **微服务治理**：Gateway 鉴权、Feign 熔断、Sentinel 限流，形成完整的服务保护链
4. **Spring Boot 3 迁移**：掌握 javax → jakarta、springfox → springdoc、配置文件等升级要点

---

## 目录

| 序号 | 题目分类 | 题目数量 |
| :--- | :--- | :--- |
| 1 | 微服务架构 | 5 |
| 2 | 高并发处理 | 4 |
| 3 | 数据库设计 | 5 |
| 4 | 分布式事务 | 4 |
| 5 | 安全与性能 | 5 |
| 6 | Spring Boot | 5 |
| 7 | 微信支付 | 5 |
| 8 | RabbitMQ高级特性 | 3 |
| 9 | Redis高级特性 | 5 |
| 10 | 项目业务场景 | 7 |
| 11 | 项目经验问题 | 5 |
| **合计** | | **53** |

---

## 一、微服务架构

### 1.1 什么是微服务架构？本项目如何体现？

**答案**：
微服务架构是一种将单体应用拆分为多个小型服务的架构风格，每个服务运行在独立进程中，通过轻量级通信机制（如 HTTP/REST）进行交互。

**本项目体现**：
- 按业务模块拆分服务：`service_user`（用户服务）、`service_hosp`（医院服务）、`service_order`（订单服务）、`service_cmn`（公共服务）等
- 使用 Nacos 进行服务注册与发现
- 使用 Spring Cloud Gateway 作为 API 网关统一入口
- 服务间通过 Feign 进行远程调用

**参考文件**：
- [Nacos 配置](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_user/src/main/resources/application.yml)
- [Feign 客户端](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_user/src/main/java/com/yygh/user/client/DictFeignClient.java)

---

### 1.2 Spring Cloud Gateway 的作用是什么？

**答案**：
Spring Cloud Gateway 是一个基于 Spring Framework 5、Spring Boot 2 和 Project Reactor 的 API 网关服务。

**主要作用**：
- **统一入口**：所有外部请求通过网关进入系统
- **路由转发**：根据 URL 路径将请求转发到对应的微服务
- **过滤器**：实现认证、限流、日志等功能
- **负载均衡**：配合 Ribbon 实现负载均衡

**本项目实现**：
```java
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if(antPathMatcher.match("/api/**/auth/**", path)) {
        Long userId = this.getUserId(request);
        if(StringUtils.isEmpty(userId)) {
            return out(response, ResultCodeEnum.LOGIN_AUTH);
        }
    }
    return chain.filter(exchange);
}
```

**参考文件**：[AuthGlobalFilter.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/server_gateway/src/main/java/com/yygh/gateway/filter/AuthGlobalFilter.java)

---

### 1.3 服务间调用方式有哪些？本项目使用哪种？

**答案**：
常见的服务间调用方式：
1. **REST API**：基于 HTTP 协议的同步调用
2. **消息队列**：异步通信，如 RabbitMQ、Kafka
3. **gRPC**：基于 HTTP/2 的高性能远程调用

**本项目使用**：
- **同步调用**：使用 Feign 进行 REST API 调用
- **异步调用**：使用 RabbitMQ 处理订单状态通知等异步操作

---

### 1.4 Nacos 的作用是什么？

**答案**：
Nacos 是阿里巴巴开源的服务发现和配置管理平台。

**主要功能**：
- **服务注册与发现**：服务实例注册到 Nacos，其他服务可以发现并调用
- **配置管理**：集中管理配置文件，支持动态刷新
- **服务健康检查**：自动检测服务实例的健康状态

**本项目配置**：
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
```

---

### 1.5 什么是服务熔断和降级？

**答案**：
- **服务熔断**：当某个服务出现大量失败请求时，暂时切断对该服务的调用，防止级联失败
- **服务降级**：当系统负载过高时，牺牲非核心功能，保证核心功能正常运行

**本项目实现**：使用 Sentinel + Feign FallbackFactory 实现服务熔断和降级。

**FallbackFactory 实现**：
```java
@Component
public class DictFeignFallbackFactory implements FallbackFactory<DictFeignClient> {
    @Override
    public DictFeignClient create(Throwable throwable) {
        return new DictFeignClient() {
            @Override
            public String getName(Long dictId) {
                return "未知";
            }
        };
    }
}
```

**参考文件**：[DictFeignFallbackFactory.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_user/src/main/java/com/yygh/user/client/DictFeignFallbackFactory.java)

---

## 二、高并发处理

### 2.1 如何解决高并发号源超卖问题？

**答案**：
采用「RLock 分布式锁 + RAtomicLong 原子扣减 + 乐观锁兜底」三层防护机制：

1. **第一层 — Redisson RLock 分布式锁**：按排班编号加锁，同一排班并发请求串行化，防止超卖
2. **第二层 — RAtomicLong CAS 原子扣减**：Redis 无锁原子操作，`addAndGet(-1)` < 0 时自动回退 `+1`
3. **第三层 — MyBatis-Plus @Version 乐观锁兜底**：数据库层最后一道防线，防止并发更新覆盖

**核心代码**：
```java
// 三层防护核心代码
String redisKey = "schedule:" + hosScheduleId + ":availableNumber";
String lockKey = "lock:schedule:" + hosScheduleId;
RLock lock = redissonClient.getLock(lockKey);
lock.lock();
try {
    // 第一层：RAtomicLong CAS 原子扣减
    RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
    if (!atomicLong.isExists()) {
        atomicLong.set(availableNumber);
    }
    long afterDecrement = atomicLong.addAndGet(-1);
    if (afterDecrement < 0) {
        atomicLong.addAndGet(1); // 回退
        throw new YyghException(ResultCodeEnum.NUMBER_NO);
    }
    // 第二层：插入订单 + 调用医院接口
    baseMapper.insert(orderInfo);
    JSONObject result = HttpRequestHelper.sendRequest(paramMap, apiUrl + "/order/submitOrder");
    if (result.getInteger("code") != 200) {
        atomicLong.addAndGet(1); // 失败回退
        throw new YyghException(result.getString("message"));
    }
} finally {
    lock.unlock(); // 第三层：释放分布式锁
}
```

**参考文件**：[OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/OrderServiceImpl.java)

---

### 2.2 项目中缓存防护体系如何解决雪崩、击穿？

**答案**：
项目针对热门医院详情页等高 QPS 场景，实现多层缓存防护方案。

**1. 缓存雪崩防护 — TTL 随机化**

大量缓存同时过期会导致流量瞬间压垮数据库。RedisConfig 中为所有缓存设置随机 TTL：

```java
// RedisConfig.java — 600~780秒随机，防止缓存雪崩
RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
    .entryTtl(Duration.ofSeconds(600 + new Random().nextInt(180)));
```

**2. 缓存击穿防护 — 逻辑过期 + Redisson 互斥锁**

热点 key 过期瞬间，大量请求同时穿透到 DB。CacheBreakdownUtil 使用「逻辑过期 + 互斥锁」模式：

```java
// CacheBreakdownUtil.getWithLogicalExpire()
// 1. 从 Redis 读取 {data: ..., expireTime: ...}
// 2. 未逻辑过期 → 直接返回
// 3. 逻辑已过期 → tryLock() 获取互斥锁
//    成功 → 查 DB 重建缓存 → 释放锁
//    失败 → 返回旧数据（防止击穿到 DB）
return cacheBreakdownUtil.getWithLogicalExpire(
    "hospital:" + hoscode, 600,
    () -> hospitalMapper.selectByHoscode(hoscode),
    HospitalVo.class
);
```

**3. 缓存穿透防护 — 空值缓存**

对于查询不存在的数据，也缓存 null 标记，短 TTL（60s），防止恶意请求穿透到数据库。

**参考文件**：
- [RedisConfig.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/common/service_util/src/main/java/com/yygh/common/config/RedisConfig.java)
- [CacheBreakdownUtil.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/common/service_util/src/main/java/com/yygh/common/cache/CacheBreakdownUtil.java)
- [HospitalServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_hosp/src/main/java/com/yygh/hosp/service/impl/HospitalServiceImpl.java)

---

### 2.3 项目中缓存更新策略是什么？

**答案**：
项目使用 **Cache-Aside（旁路缓存）** 模式：

**读操作**：
```java
// @Cacheable 自动：先查 Redis → 命中返回 → 未命中查 DB 并写入缓存
@Cacheable(value = "hospital", key = "#hoscode")
public HospitalVo getByHoscode(String hoscode) { ... }
```

**写操作**：
```java
// @CacheEvict 自动：先更新 DB → 删除缓存（而非更新缓存，避免并发写覆盖）
@CacheEvict(value = "hospital", key = "#hospitalSaveDTO.hoscode")
public void save(HospitalSaveDTO hospitalSaveDTO) { ... }
```

**为什么写时删缓存而不是更新缓存？** 高并发下多个写请求可能以不同顺序更新缓存，导致缓存脏数据。删除缓存让下次读请求从 DB 加载最新值，保证一致性。

---

### 2.4 Redisson 分布式锁的实现原理？

**答案**：
Redisson 分布式锁基于 Redis 的 Redlock 算法实现：

1. **获取锁**：向多个 Redis 节点发送 SET 命令
2. **释放锁**：发送 DEL 命令删除锁
3. **锁续期**：使用 Watch Dog 机制自动延长锁的过期时间

---

## 三、数据库设计

### 3.1 索引的作用是什么？如何创建合适的索引？

**答案**：
索引是一种数据结构，用于快速查询数据库中的数据。

**创建索引的原则**：
1. **经常用于 WHERE 条件的字段**：如用户 ID、订单号
2. **经常用于 JOIN 的字段**：如外键字段
3. **经常用于 ORDER BY 的字段**：如创建时间

**注意事项**：
- 索引会增加写操作的开销
- 不要创建过多索引
- 复合索引要遵循最左前缀原则

---

### 3.2 什么是事务？事务的 ACID 特性是什么？

**答案**：
事务是一组原子性的操作，要么全部成功，要么全部失败。

**ACID 特性**：
- **原子性**：事务中的操作要么全部执行，要么全部不执行
- **一致性**：事务执行前后数据的完整性约束不变
- **隔离性**：多个事务并发执行时，彼此之间互不干扰
- **持久性**：事务提交后，数据的修改是永久性的

---

### 3.3 事务隔离级别有哪些？

**答案**：
- **READ UNCOMMITTED**：允许读取未提交的数据（脏读）
- **READ COMMITTED**：只能读取已提交的数据（不可重复读）
- **REPEATABLE READ**：在同一事务中多次读取同一数据结果一致（幻读）
- **SERIALIZABLE**：最高隔离级别，事务串行执行

**MySQL 默认级别**：REPEATABLE READ

---

### 3.4 什么是乐观锁和悲观锁？

**答案**：
- **悲观锁**：认为数据一定会被并发修改，在读取时就加锁
  - 实现方式：SELECT ... FOR UPDATE
- **乐观锁**：认为数据很少被并发修改，在更新时检查数据是否被修改
  - 实现方式：使用 version 字段

**本项目使用**：乐观锁

---

### 3.5 MyBatis-Plus 的优点是什么？

**答案**：
MyBatis-Plus 是 MyBatis 的增强工具，提供了许多便捷功能：

1. **自动生成 CRUD**：无需编写 XML，直接调用方法
2. **分页插件**：简化分页查询
3. **条件构造器**：使用 Lambda 表达式构建查询条件
4. **代码生成器**：自动生成实体类、Mapper、Service 等
5. **多租户支持**：方便实现多租户架构

**本项目使用**：数据字典模块通过 MyBatis-Plus 操作，参考 Dict 实体类。

**参考文件**：[Dict.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_cmn/src/main/java/com/yygh/cmn/model/Dict.java)

---

## 四、分布式事务

### 4.1 分布式事务的解决方案有哪些？

**答案**：
常见的分布式事务解决方案：

1. **两阶段提交（2PC）**：协调者分两阶段提交事务
2. **三阶段提交（3PC）**：在 2PC 基础上增加准备阶段
3. **TCC（Try-Confirm-Cancel）**：业务层实现补偿逻辑
4. **可靠消息最终一致性**：通过消息队列实现最终一致性
5. **Seata**：阿里巴巴开源的分布式事务解决方案

---

### 4.2 什么是最终一致性？项目如何实现支付订单最终一致？

**答案**：
最终一致性是指在分布式系统中，数据在经过一段时间后会达到一致状态，而不是实时一致。

**项目中的支付一致性方案（幂等 + MQ + 对账）**：

**1. 接口幂等校验**：
微信支付可能重复回调，`paySuccessV3()` 先 `selectCount` 检查 `outTradeNo` 是否已处理，防止重复更新。

**2. RabbitMQ 异步消息队列**：
订单状态变更后，通过 RabbitMQ 异步通知 hospital-manage 端：
```java
// HospitalServiceImpl — 发送订单状态消息到 MQ
rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_ROUTING_KEY, statusMsg);

// OrderStatusReceiver — 消费消息更新本地订单状态
@RabbitListener(queues = MqConfig.ORDER_QUEUE)
public void handleMessage(Message message, Channel channel) {
    // 手动确认机制，防止消息丢失
    channel.basicAck(deliveryTag, false);
}
```

**3. 定时对账补偿**：
`PaymentReconciliationTask` 每隔 10 分钟取消超 30 分钟未支付订单 + 每日凌晨 2 点与微信对账，发现差异自动修复：
```java
@Scheduled(cron = "0 0/10 * * * ?") // 每10分钟
public void cancelExpiredOrders() { ... }

@Scheduled(cron = "0 0 2 * * ?") // 凌晨2点
public void dailyReconciliation() { ... }
```

**参考文件**：
- [PaymentServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/PaymentServiceImpl.java)
- [PaymentReconciliationTask.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/task/PaymentReconciliationTask.java)

---

### 4.3 如何保证接口幂等性？

**答案**：
幂等性是指多次调用同一接口产生的效果与调用一次相同。

**实现方式**：
1. **唯一标识**：使用订单号、业务流水号等作为唯一标识
2. **数据库唯一约束**：在数据库层面保证唯一性
3. **Redis 锁**：使用 Redis 实现分布式锁
4. **状态机校验**：根据业务状态判断是否允许操作

**参考文件**：[OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/OrderServiceImpl.java)

---

### 4.4 消息队列的作用是什么？

**答案**：
消息队列主要用于异步通信和解耦：

1. **异步处理**：将耗时操作异步化，提高系统响应速度
2. **解耦**：生产者和消费者解耦，各自独立发展
3. **削峰填谷**：缓冲高峰期的请求，保护下游系统
4. **消息广播**：一条消息可以被多个消费者处理

**本项目使用**：
- RabbitMQ 处理订单状态变更通知和异步解耦

---

## 五、安全与性能

### 5.1 JWT 的工作原理是什么？

**答案**：
JWT（JSON Web Token）是一种用于身份认证的令牌。

**组成部分**：
1. **Header**：声明令牌类型和加密算法
2. **Payload**：存储用户信息（如用户 ID、角色等）
3. **Signature**：使用密钥对 Header 和 Payload 进行签名

**工作流程**：
1. 用户登录成功后，服务器生成 JWT 并返回
2. 用户后续请求携带 JWT
3. 服务器验证 JWT 的有效性

**本项目实现**：
```java
String token = JwtHelper.createToken(userInfo.getId(), name);
Long userId = JwtHelper.getUserId(token);
```

---

### 5.2 如何防止 SQL 注入？

**答案**：
SQL 注入是一种常见的安全漏洞，攻击者通过在输入中注入 SQL 语句来攻击数据库。

**防范措施**：
1. **使用参数化查询**：使用预编译语句，避免拼接 SQL
2. **输入校验**：对用户输入进行严格校验
3. **最小权限原则**：数据库用户只赋予必要的权限
4. **使用 ORM 框架**：如 MyBatis-Plus，自动使用参数化查询

---

### 5.3 如何防止 XSS 攻击？

**答案**：
XSS（跨站脚本攻击）是攻击者在网页中注入恶意脚本。

**防范措施**：
1. **输入过滤**：对用户输入进行 HTML 转义
2. **输出编码**：在输出时进行 HTML 编码
3. **使用安全的框架**：如 Spring Security 提供的 XSS 防护

---

### 5.4 如何优化系统性能？

**答案**：
性能优化的几个方面：

1. **数据库优化**：索引优化、查询优化、读写分离
2. **缓存优化**：合理使用 Redis 缓存热点数据
3. **代码优化**：减少不必要的计算、使用高效的数据结构
4. **异步处理**：使用消息队列处理耗时操作
5. **负载均衡**：使用 Nginx 或 Gateway 进行负载均衡

---

### 5.5 什么是 HTTPS？如何配置？

**答案**：
HTTPS 是在 HTTP 基础上加入 SSL/TLS 加密的协议，用于保护数据传输安全。

**配置步骤**：
1. 申请 SSL 证书
2. 配置 Web 服务器（如 Nginx）使用证书
3. 配置 Spring Boot 使用 HTTPS

---

## 六、Spring Boot

### 6.1 Spring Boot 的自动配置原理是什么？

**答案**：
Spring Boot 自动配置通过以下机制实现：

1. **@EnableAutoConfiguration**：启用自动配置
2. **Spring Factories Loader**：扫描 META-INF/spring.factories 文件
3. **条件注解**：根据条件决定是否启用某个配置
   - @ConditionalOnClass：当类存在时
   - @ConditionalOnMissingBean：当 Bean 不存在时
   - @ConditionalOnProperty：当配置属性满足时

---

### 6.2 @Autowired 和 @RequiredArgsConstructor 的区别？

**答案**：
- **@Autowired**：字段注入，Spring 通过反射注入依赖
- **@RequiredArgsConstructor**：构造器注入，Lombok 自动生成构造器

**构造器注入的优点**：
1. 依赖不可变（final 字段）
2. 依赖必须在创建时提供
3. 便于单元测试（可以手动注入依赖）

**本项目使用**：项目已全面重构为 @RequiredArgsConstructor 构造器注入，不再使用 @Autowired 字段注入。这是近期的统一重构，所有 Service、Controller 均采用 final + @RequiredArgsConstructor 模式。

---

### 6.3 Spring Boot 常用的启动器有哪些？

**答案**：
- **spring-boot-starter-web**：Web 开发
- **spring-boot-starter-data-jpa**：JPA 数据访问
- **spring-boot-starter-data-redis**：Redis 缓存
- **spring-boot-starter-amqp**：RabbitMQ 消息队列
- **spring-boot-starter-security**：安全框架
- **spring-boot-starter-test**：测试支持

---

### 6.4 什么是 Spring Boot Starter？

**答案**：
Spring Boot Starter 是一组预定义的依赖集合，简化了 Maven 配置。

**特点**：
- 自动配置相关 Bean
- 提供默认配置
- 减少依赖声明

---

### 6.5 Spring Boot 的配置文件加载顺序是什么？

**答案**：
Spring Boot 按以下顺序加载配置文件（后面的会覆盖前面的）：

1. classpath:/
2. classpath:/config/
3. file:./
4. file:./config/
5. file:./config/*/

**配置文件类型**：
- .properties
- .yml/.yaml

**本项目使用**：除 hospital-manage 模块使用 .yml 外，其余服务均统一使用 .properties 配置文件。

---

## 七、微信支付

### 7.1 微信支付的支付流程是什么？

**答案**：
微信支付的标准流程如下：

1. **下单**：用户选择就诊项目后，系统生成订单
2. **统一下单**：调用微信支付统一下单 API，获取预支付交易会话标识（prepay_id）
3. **生成支付参数**：根据 prepay_id 生成前端调起支付的参数
4. **调起支付**：前端使用支付参数调起微信支付
5. **支付回调**：用户完成支付后，微信服务器异步通知商户
6. **处理结果**：商户验证签名，处理支付结果
7. **预约成功**：支付成功后，发送订单状态通知
8. **就诊当天**：用户凭订单到医院签到就诊

**本项目实现**：
```java
// 统一下单
WeixinVo weixinVo = new WeixinVo();
weixinVo.setPrepayId("prepay_id");
weixinVo.setOrderNo(order.getId());

// 生成签名
String paySign = MD5.encryptSign(jsonObject.get("sign").toString());
```

**参考文件**：
- [WeixinController.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/controller/WeixinController.java)

---

### 7.2 如何处理微信支付回调？

**答案**：
支付回调使用微信支付 APIv3 + wechatpay-java SDK 自动验签解密。

1. **接收通知**：微信 POST 加密的支付结果通知（APIv3 格式）
2. **SDK 自动验签解密**：`WechatPayValidator` 验证微信签名 + `AesUtil` 解密回调报文
3. **幂等处理**：`paySuccessV3()` 先 `selectCount` 检查是否已处理，防止重复回调
4. **更新状态**：支付记录表 insert + 订单状态 UNPAID→PAID + 同步 hospital-manage
5. **返回应答**：返回 JSON `{"code":"SUCCESS"}` / `{"code":"FAIL"}`

**核心代码**：
```java
@PostMapping("/callback/notify")
public String payNotify(@RequestBody String body, 
                         @RequestHeader("Wechatpay-Serial") String serial,
                         @RequestHeader("Wechatpay-Signature") String signature,
                         @RequestHeader("Wechatpay-Timestamp") String timestamp,
                         @RequestHeader("Wechatpay-Nonce") String nonce) {
    // 1. SDK 自动验签
    WechatPayValidator validator = new WechatPayValidator(verifier);
    // 2. 解密回调内容
    String plaintext = AesUtil.decryptToString(body.getBytes(), apiV3Key);
    // 3. 解析 + 幂等处理
    JSONObject result = JSONObject.parseObject(plaintext);
    if ("TRANSACTION.SUCCESS".equals(result.getString("event_type"))) {
        paymentService.paySuccessV3(outTradeNo, transactionId);
    }
    return "{\"code\":\"SUCCESS\"}";
}
```

**参考文件**：[WechatCallbackController.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/controller/WechatCallbackController.java)

---

### 7.3 如何保证支付幂等性？

**答案**：
支付幂等性是防止重复支付的关键。

**本项目实现（数据库幂等）**：

```java
// PaymentServiceImpl.paySuccessV3()
public void paySuccessV3(String outTradeNo, String transactionId) {
    // 幂等：已支付则跳过（selectCount 检查）
    if (paymentInfoMapper.selectCount(
            new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getOutTradeNo, outTradeNo)) > 0) {
        log.info("支付已处理，跳过，订单号：{}", outTradeNo);
        return;
    }
    // 插入支付记录
    paymentInfoMapper.insert(paymentInfo);
    // 更新订单状态 UNPAID → PAID
    orderService.updateStatus(orderId, OrderStatusEnum.PAID);
    // 同步 hospital-manage
    HttpRequestHelper.sendRequest(reqMap, apiUrl + "/order/updatePayStatus");
}
```

**三重保障**：
1. **数据库 selectCount 前置检查**：已支付订单直接跳过
2. **唯一订单号 outTradeNo**：全局唯一
3. **定时对账补偿**：`PaymentReconciliationTask` 每日凌晨2点与微信对账，修复不一致状态

**参考文件**：[PaymentServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/PaymentServiceImpl.java)

---

### 7.4 支付失败如何处理？

**答案**：
支付失败的常见处理方式：

1. **主动查询**：调用微信支付查询接口确认实际状态
2. **超时处理**：设置支付超时时间，超时后自动取消订单
3. **人工介入**：记录异常订单，由客服处理
4. **退款处理**：如果已扣款但订单失败，需要发起退款

**订单超时处理**：
```java
// 订单超时检查
@Scheduled(cron = "0 */30 * * * ?")
public void closeExpiredOrders() {
    List<Order> expiredOrders = orderMapper.selectExpiredOrders();
    for(Order order : expiredOrders) {
        orderService.cancelOrder(order.getId());
    }
}
```

---

### 7.5 微信支付有哪些安全措施？

**答案**：
微信支付的安全保障机制：

1. **签名验证**：使用 RSA 或 MD5 签名确保数据不被篡改
2. **回调 IP 白名单**：只处理来自微信服务器的回调
3. **接口密钥**：使用 APIv3 密钥进行加密
4. **证书认证**：使用商户证书进行双向认证
5. **敏感数据加密**：手机号等敏感信息使用 RSA 加密

---

## 八、RabbitMQ高级特性

### 8.1 RabbitMQ 的消息确认机制是什么？

**答案**：
RabbitMQ 提供两种确认机制：

1. **发布确认（Publisher Confirm）**：
   - 生产者发送消息后，等待 Broker 确认
   - 确保消息已到达 Broker

2. **消费确认（Consumer Ack）**：
   - 手动确认：消费者处理完成后手动确认
   - 自动确认：消息投递后自动确认

**本项目实现**：
```java
@RabbitListener(queues = "ORDER_QUEUE")
public void handleMessage(String messageBody, 
                         Message message, 
                         Channel channel) throws IOException {
    try {
        // 执行业务逻辑
        processMessage(messageBody);
        
        // 手动确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
        // 拒绝消息，不重新入队
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    }
}
```

**参考文件**：[OrderStatusReceiver.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/receiver/OrderStatusReceiver.java)

---

### 8.2 如何处理死信队列？

**答案**：
死信队列（DLQ）用于处理无法正常消费的消息：

**产生场景**：
1. 消息被消费者拒绝，且不重新入队
2. 消息超过存活时间未被消费
3. 队列达到最大长度

**配置死信队列**：
```java
@Bean
public DirectExchange deadExchange() {
    return new DirectExchange("dead.exchange");
}

@Bean
public Queue deadQueue() {
    return new Queue("dead.queue");
}

@Bean
public Binding deadBinding() {
    return BindingBuilder.bind(deadQueue())
                        .to(deadExchange())
                        .with("dead.routing.key");
}
```

**处理策略**：
1. 记录死信消息日志
2. 人工干预处理异常消息
3. 定期清理死信队列

---

### 8.3 如何保证消息顺序性？

**答案**：
消息顺序性是指消息的消费顺序与发送顺序一致。

**实现方式**：
1. **单一队列**：使用单一队列，所有消息按顺序发送
2. **分区键**：使用相同的分区键，确保消息进入同一队列
3. **消费者串行处理**：避免并发消费导致乱序

**本项目应用**：
订单状态变更消息需要按顺序处理：
- 创建订单 → 支付成功 → 就诊完成

```java
// 使用订单号作为路由键，保证同一订单的消息有序
rabbitTemplate.convertAndSend("order.exchange", orderId, message);
```

---

## 九、Redis高级特性

### 9.1 Redis 有哪些数据结构？如何选择？

**答案**：
Redis 提供了丰富的数据结构：

1. **String**：简单键值存储
   - 应用：验证码、token、计数器

2. **Hash**：对象存储
   - 应用：用户信息、订单详情

3. **List**：列表
   - 应用：消息队列、任务队列

4. **Set**：无序集合
   - 应用：标签、点赞

5. **Sorted Set**：有序集合
   - 应用：排行榜、限流

6. **BitMap**：位图
   - 应用：签到、在线状态

**本项目应用**：
```java
// String：缓存用户 token
redisTemplate.opsForValue().set("user:token:" + userId, token, 2, TimeUnit.HOURS);

// Hash：缓存订单信息
redisTemplate.opsForHash().putAll("order:" + orderId, orderMap);

// Sorted Set：号源库存（按科室分类）
redisTemplate.opsForZSet().add("schedule:hosp:" + hospCode, scheduleId, availableCount);
```

---

### 9.2 Redis 集群方案有哪些？

**答案**：
Redis 集群的主要方案：

1. **Redis Sentinel（哨兵模式）**：
   - 主从复制 + 自动故障转移
   - 适用场景：小型系统，读多写少

2. **Redis Cluster**：
   - 数据分片（16384 个槽位）
   - 自动负载均衡
   - 适用场景：大规模数据存储

3. **Codis**：
   - 第三方集群方案
   - 支持节点动态扩缩容
   - 适用场景：中大型系统

**本项目使用**：Redis Sentinel + 主从复制

---

### 9.3 如何保证 Redis 与 MySQL 数据一致性？

**答案**：
保证缓存与数据库一致性的策略：

1. **Cache-Aside（旁路缓存）**：
   - 读：先读缓存，没有则读数据库并写入缓存
   - 写：先写数据库，再删除缓存

2. **Write-Through（读写穿透）**：
   - 同时写入缓存和数据库

3. **Write-Behind（异步写入）**：
   - 先写缓存，定期批量写入数据库

**本项目策略**：采用 Cache-Aside + 删除缓存

```java
// 更新数据
public void updateUser(User user) {
    // 1. 先更新数据库
    userMapper.updateById(user);
    // 2. 删除缓存
    redisTemplate.delete("user:" + user.getId());
}
```

**一致性保障**：
1. 删除缓存而非更新（避免并发问题）
2. 使用延迟双删策略（先删缓存，更新数据库，再删缓存）
3. 设置合理的缓存过期时间

---

### 9.4 Redis 过期策略有哪些？

**答案**：
Redis 的过期策略：

1. **定时过期**：设置定时器，准时删除过期 key
   - 优点：内存友好
   - 缺点：CPU 消耗大

2. **惰性过期**：访问 key 时检查是否过期
   - 优点：CPU 友好
   - 缺点：内存占用大

3. **定期过期**：每隔一段时间扫描过期 key
   - 优点：平衡内存和 CPU
   - **本项目使用**：惰性过期 + 定期过期结合

**本项目应用**：
```java
// 用户 token 设置过期时间
redisTemplate.opsForValue().set("token:" + userId, token, 2, TimeUnit.HOURS);

// 号源数据定期刷新
@Scheduled(cron = "0 0 0 * * ?") // 每天凌晨同步
public void syncScheduleData() {
    // 同步最新号源到 Redis
}
```

---

### 9.5 Redis 如何实现分布式锁？

**答案**：
Redis 分布式锁的实现方式：

1. **SET NX EX**：使用 SET 命令的 NX 和 EX 选项
   ```java
   Boolean success = redisTemplate.opsForValue()
       .setIfAbsent("lock:" + key, requestId, 10, TimeUnit.SECONDS);
   ```

2. **Redisson**：提供封装好的分布式锁
   ```java
   RLock lock = redissonClient.getLock("order:lock:" + orderId);
   try {
       lock.lock();
       // 执行业务逻辑
   } finally {
       lock.unlock();
   }
   ```

**注意事项**：
1. 锁必须设置过期时间
2. 锁的值要使用唯一标识（防止误删别人的锁）
3. 支持锁续期（Watch Dog）
4. 保证原子性操作

---

## 十、项目业务场景

### 10.1 描述一下预约挂号的完整业务流程

**答案**：
预约挂号的完整流程：

1. **用户登录**：用户通过手机号+验证码登录系统
2. **选择医院**：用户浏览医院列表，选择目标医院
3. **选择科室**：选择医院后，显示科室列表
4. **选择医生**：显示科室下的医生列表（可按日期筛选）
5. **选择排班**：选择医生的排班日期和时间段
6. **确认预约**：确认预约信息（科室、医生、时间）
7. **创建订单**：系统创建订单，生成订单号
8. **支付费用**：用户支付挂号费用
9. **预约成功**：支付成功后，发送订单状态通知
10. **就诊当天**：用户凭订单到医院签到就诊

**核心代码流程**：
```
选择排班 → 创建订单 → 预扣库存 → 微信支付 → 更新库存 → 发送通知
```

---

### 10.2 如何处理预约冲突（同一时间段多人预约同一医生）？

**答案**：
采用多层防护机制处理预约冲突：

1. **Redis 原子扣减**：
   - 排班可用数量预加载到 Redis
   - 使用 compareAndSet 保证原子性

2. **数据库乐观锁**：
   - 使用 version 字段防止超卖
   ```java
   @Version
   private Integer version;
   ```

3. **分布式锁**：
   - 使用 Redisson 分布式锁，同一订单操作串行化

4. **幂等性保障**：
   - 同一订单号只能支付一次
   - 订单状态机校验

---

### 10.3 退号如何处理？

**答案**：
退号处理流程：

1. **验证退号条件**：
   - 订单状态必须是已支付
   - 距离就诊时间需超过规定时间（如2小时）
   - 当天不能退号

2. **更新订单状态**：
   - 将订单状态改为"已取消"
   - 记录取消原因和时间

3. **回补课源**：
   - 释放占用的号源
   - Redis 库存 +1
   - 数据库可用数量 +1

4. **退款处理**：
   - 调用微信退款接口
   - 更新退款状态

5. **发送通知**：
   - 站内通知用户退号成功

**核心代码**：
```java
public void cancelOrder(String orderId) {
    // 1. 验证订单状态
    Order order = orderMapper.selectById(orderId);
    if(!"PAID".equals(order.getStatus())) {
        throw new YyghException("当前状态不支持退号");
    }
    
    // 2. 更新订单状态
    order.setStatus("CANCELLED");
    orderMapper.updateById(order);
    
    // 3. 回补课源
    scheduleService.increaseAvailable(order.getScheduleId());
    
    // 4. 退款
    weixinPayService.refund(order);
}
```

---

### 10.4 医院排班管理有哪些功能？

**答案**：
排班管理的主要功能：

1. **排班规则配置**：
   - 设置出诊时间（上午/下午/晚上）
   - 设置放号时间（如提前7天放号）
   - 设置每位医生的接诊数量

2. **排班 CRUD**：
   - 新增排班（按天/按周）
   - 修改排班（调整号源数量）
   - 删除排班（停诊处理）

3. **排班查询**：
   - 按科室查询排班
   - 按医生查询排班
   - 按日期范围查询

4. **排班统计**：
   - 统计各科室预约率
   - 统计医生工作量
   - 统计取消率

**本项目实现**：
```java
// 排班分页查询
IPage<Schedule> page = scheduleService.selectPage(pageNum, pageSize, 
    new QueryWrapper<Schedule>()
        .eq("hospital_id", hospitalId)
        .between("work_date", startDate, endDate)
        .orderByAsc("work_date", "work_time"));
```

---

### 10.5 订单状态通知功能如何实现？

**答案**：
订单状态通知通过 RabbitMQ 异步处理，hospital-manage 端发送消息 → service_order 端消费：

1. **发送端（hospital-manage）**：订单状态变更时，通过 `RabbitTemplate` 发送 JSON 消息到 `ORDER_EXCHANGE`
2. **消费端（service_order）**：`OrderStatusReceiver` 通过 `@RabbitListener` 监听 `ORDER_QUEUE`，手动 ACK 确认
3. **manua l-ack 机制**：消费者处理成功后 `channel.basicAck()`，失败 `channel.basicNack()`，防止消息丢失

**核心代码**：
```java
// HospitalServiceImpl — 发送消息
rabbitTemplate.convertAndSend(
    HospitalRabbitConfig.ORDER_EXCHANGE,
    HospitalRabbitConfig.ORDER_ROUTING_KEY, statusMsg);

// OrderStatusReceiver — 消费消息
@RabbitListener(queues = MqConfig.ORDER_QUEUE)
public void handleMessage(String messageBody, Message message, Channel channel) {
    try {
        JSONObject msg = JSONObject.parseObject(messageBody);
        orderService.updateOrderStatus(
            msg.getLong("hosRecordId"), msg.getInteger("orderStatus"));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    } catch (Exception e) {
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    }
}
```

**参考文件**：
- [HospitalServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/hospital-manage/src/main/java/com/yygh/hospital/service/impl/HospitalServiceImpl.java)
- [OrderStatusReceiver.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/receiver/OrderStatusReceiver.java)

---

### 10.6 如何实现号源实时同步？

**答案**：
号源实时同步机制：

1. **数据预加载**：
   - 每天凌晨同步次日号源到 Redis
   - 定时任务批量更新

2. **实时扣减**：
   - 用户预约时立即更新 Redis
   - 使用 Lua 脚本保证原子性

3. **异步入库**：
   - 订单创建后，异步更新数据库
   - 使用消息队列保证最终一致性

4. **数据校验**：
   - 定期对比 Redis 和数据库数量
   - 发现不一致时自动同步

```java
// 预加载号源到 Redis
public void preloadScheduleToRedis() {
    List<Schedule> schedules = scheduleService.getTomorrowSchedules();
    for(Schedule schedule : schedules) {
        String key = "schedule:" + schedule.getId();
        redisTemplate.opsForValue().set(key, schedule.getAvailable());
    }
}
```

---

### 10.7 如何处理医院、科室、医生数据的导入？

**答案**：
使用 EasyExcel 流式读取 + MyBatis-Plus 批量插入实现高效数据导入：

1. **流式读取**：`AnalysisEventListener` 逐行解析 Excel，不一次性加载全量，防止 OOM
2. **攒批插入**：invoke() 累积行到 `List<Dict>`，`doAfterAllAnalysed()` 后调用 `saveBatch()` 批量写入
3. **缓存失效**：`@CacheEvict(value = "dict", allEntries = true)` 导入后清空旧缓存

**核心代码**：
```java
// DictListener.java — 批量累积
public class DictListener extends AnalysisEventListener<DictEeVo> {
    private final List<Dict> dictList = new ArrayList<>();

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext context) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        dictList.add(dict); // 攒批，不逐行 insert
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 数据由调用方批量写入
    }

    public List<Dict> getDictList() { return dictList; }
}

// DictServiceImpl.java — 批量写入
@CacheEvict(value = "dict", allEntries = true)
public void importDictData(MultipartFile file) {
    DictListener listener = new DictListener();
    EasyExcel.read(file.getInputStream(), DictEeVo.class, listener).sheet().doRead();
    this.saveBatch(listener.getDictList()); // MyBatis-Plus 批量插入
}
```

**参考文件**：
- [DictListener.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_cmn/src/main/java/com/yygh/cmn/listener/DictListener.java)
- [DictServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_cmn/src/main/java/com/yygh/cmn/service/impl/DictServiceImpl.java)

---

## 十一、项目经验问题

### 11.1 这个项目最大的难点是什么？你是如何解决的？

**答案**：
**最大难点：高并发号源扣减（三层防护）**

**问题描述**：
医院放号瞬间，大量用户同时抢号，导致：
1. 号源超卖（同一号源被多人预约）
2. 系统响应慢（数据库压力过大）
3. 数据不一致（缓存与数据库不同步）

**解决方案（三层防护）**：

**第一层 — Redisson 分布式锁**：
按排班编号（`hosScheduleId`）加锁，同一排班的并发请求串行化，不同排班互不阻塞：
```java
RLock lock = redissonClient.getLock("lock:schedule:" + hosScheduleId);
lock.lock();
try { /* 扣减 + 下单 */ } finally { lock.unlock(); }
```

**第二层 — RAtomicLong 原子扣减**：
Redis CAS 无锁原子操作，`addAndGet(-1)` 扣减。结果 < 0 则 `addAndGet(1)` 回退：
```java
RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
if (!atomicLong.isExists()) atomicLong.set(availableNumber);
if (atomicLong.addAndGet(-1) < 0) {
    atomicLong.addAndGet(1);
    throw new YyghException(ResultCodeEnum.NUMBER_NO);
}
```

**第三层 — MyBatis-Plus 乐观锁兜底**：
数据库 `@Version` 字段防止并发更新覆盖，最后一道防线：
```java
// MybatisPlusConfig 配置乐观锁拦截器
interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
```

**附加保障 — 失败回滚**：
医院接口调用失败、Feign 调用失败时均回退 Redis 号源，保证库存不丢失。

**参考文件**：[OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/OrderServiceImpl.java)

**参考文件**：[OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/OrderServiceImpl.java)

---

### 11.2 项目中遇到的最大挑战是什么？

**答案**：
**挑战：分布式事务问题**

**问题场景**：
预约挂号的流程涉及多个服务：
- 订单服务：创建订单
- 库存服务：扣减号源
- 支付服务：处理支付
- 订单通知：发送订单状态通知

当某个步骤失败时，如何保证数据一致性？

**解决方案**：
1. **采用最终一致性**：
   - 不追求强一致性，而是最终一致
   - 使用消息队列异步协调

2. **幂等性设计**：
   - 每个操作都支持幂等
   - 使用唯一订单号作为标识

3. **补偿机制**：
   - 定时任务检查异常订单
   - 自动回滚未完成的操作

4. **状态机控制**：
   - 订单状态：待支付 → 已支付 → 已完成/已取消
   - 只有合法状态转换才能执行

---

### 11.3 如何保证系统的可用性和稳定性？

**答案**：
**多层面保障系统稳定性**：

1. **应用层**：
   - 服务熔断：使用 Sentinel + Feign FallbackFactory
   - 限流：使用 Sentinel 或 Gateway 限流
   - 超时控制：设置合理的接口超时时间

2. **缓存层**：
   - Redis 集群：主从 + 哨兵
   - 多级缓存：本地缓存 + Redis
   - 缓存降级：Redis 不可用时直接查数据库

3. **数据库层**：
   - 主从复制：读写分离
   - 连接池：Druid/HikariCP
   - SQL 优化：避免全表扫描

4. **监控告警**：
   - 链路追踪：SkyWalking
   - 日志监控：ELK
   - 指标监控：Prometheus

5. **容灾备份**：
   - 数据库定期备份
   - 跨机房部署
   - 应急预案演练

---

### 11.4 项目中有哪些性能优化经验？

**答案**：
**性能优化实践**：

1. **接口优化**：
   - 使用 Redis 缓存热点数据
   - 异步处理非核心逻辑
   - 接口结果分页，避免一次返回过多数据

2. **数据库优化**：
   - 创建合适索引（复合索引、最左前缀）
   - SQL 优化（避免 SELECT *、减少子查询）
   - 分库分表（按业务拆分）

3. **JVM 优化**：
   - 合理设置堆内存大小
   - 选择合适的垃圾回收器
   - 减少 Full GC 频率

4. **网络优化**：
   - 使用 HTTP/2
   - 启用压缩（Gzip）
   - CDN 加速静态资源

5. **代码优化**：
   - 减少对象创建
   - 使用线程池复用线程
   - 合理使用数据结构

**效果**：
- 接口响应时间从 500ms 优化到 50ms
- 系统 QPS 从 100 提升到 1000+

---

### 11.5 如果让你重新设计这个项目，你会做哪些改进？

**答案**：
**已完成的改进**：

1. **服务熔断降级**：已引入 Sentinel + Feign FallbackFactory，替代了原先的 Hystrix 方案，提供更灵活的熔断降级策略。

2. **依赖注入重构**：已全面使用 @RequiredArgsConstructor 构造器注入，统一代码风格，提升可测试性。

3. **配置规范统一**：除 hospital-manage 模块外，所有服务已统一使用 .properties 配置文件。

**未来改进建议**：

1. **架构层面**：
   - 引入 Seata 分布式事务框架
   - 使用 RocketMQ 替代 RabbitMQ（更高吞吐量）
   - 引入配置中心（Nacos Config）

2. **技术栈升级**：
   - Spring Boot → 已升级至 3.2.12
   - Spring Cloud → 已升级至 2023.0.3
   - JDK 8 → JDK 17/21
   - 使用 Virtual Thread 提升并发能力

3. **性能优化**：
   - 引入 Elasticsearch 实现搜索
   - 使用 Redis Cluster 替代 Sentinel
   - 引入 ShardingSphere 实现分库分表

4. **可观测性**：
   - 引入 SkyWalking 链路追踪
   - 使用 Micrometer + Prometheus
   - 完善日志规范（ELK）

5. **安全加固**：
   - 接口参数加密传输
   - 引入 OAuth2 + JWT
   - 敏感数据脱敏存储

6. **业务优化**：
   - 增加候补排队功能
   - 引入信用积分制度
   - 支持家庭账户管理

---

## 总结

以上面试题涵盖了微服务架构、高并发处理、数据库设计、分布式事务、安全与性能、Spring Boot、微信支付、RabbitMQ、Redis、项目业务场景和项目经验等方面，共 **55 道**高频面试题，是医院预约挂号系统中涉及的核心技术问题。建议结合项目实际代码进行准备，确保能够清晰描述项目中的实现细节。
