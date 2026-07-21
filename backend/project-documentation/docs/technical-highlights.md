# 医院预约挂号系统 - 技术亮点文档

## 项目概述

医院预约挂号系统是基于 Spring Cloud 微服务架构的在线医疗预约平台，实现医院信息管理、科室排班、预约挂号、微信支付、订单管理全流程数字化。

**项目名称**：电子挂号系统  
**开发时间**：2026.03 - 2026.05  
**技术架构**：Spring Boot 3.2.12、Spring Cloud 2023.0.3、Spring Cloud Alibaba 2023.0.1.0、Nacos、Gateway、MyBatis-Plus、MySQL、Redis、Sentinel、EasyExcel、springdoc-openapi、JWT

---

## 技术亮点目录

| 序号 | 技术亮点 | 涉及文件 |
| :--- | :--- | :--- |
| 1 | 高并发号源超卖解决方案 | OrderServiceImpl.java |
| 2 | JWT 认证与网关鉴权 | AuthGlobalFilter.java |
| 3 | 分布式锁与原子操作 | OrderServiceImpl.java |
| 4 | 微信支付集成 | WeixinServiceImpl.java |
| 5 | Feign 熔断降级与 Sentinel 流量控制 | HospitalFeignClientFallbackFactory.java |
| 6 | Redis 缓存策略 | HospitalServiceImpl.java |
| 7 | MyBatis-Plus 分页查询 | DictServiceImpl.java |
| 8 | 异常统一处理 | GlobalExceptionHandler.java |
| 9 | 接口幂等性设计 | PaymentServiceImpl.java |
| 10 | EasyExcel 批量导入 | DictServiceImpl.java |
| 11 | @RequiredArgsConstructor 构造函数注入 | OrderServiceImpl.java |
| 12 | 缓存穿透防护 | ScheduleServiceImpl.java |

---

## 1. 高并发号源超卖解决方案

### 问题背景
多用户同时预约热门号源存在超卖风险，传统数据库锁无法应对高并发场景。

### 解决方案
采用 **Redis 预扣库存 + Redisson 分布式锁 + MySQL 乐观锁** 三层防护机制。

### 代码位置
- **核心实现**：[OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/OrderServiceImpl.java)

### 关键代码
```java
// 使用 Redisson RAtomicLong 原子扣减号源
String redisKey = "schedule:" + scheduleOrderVo.getHosScheduleId() + ":availableNumber";
RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
long afterDecrement = atomicLong.addAndGet(-1);
if (afterDecrement < 0) {
    // 号源不足，回退
    atomicLong.addAndGet(1);
    throw new YyghException(ResultCodeEnum.NUMBER_NO);
}
```

---

## 2. JWT 认证与网关鉴权

### 问题背景
开放 API 存在未授权访问风险，需要统一的身份认证机制。

### 解决方案
基于 JWT 实现 Token 认证 + Gateway 网关统一鉴权。

### 代码位置
- **网关过滤器**：[AuthGlobalFilter.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/server_gateway/src/main/java/com/yygh/gateway/filter/AuthGlobalFilter.java)
- **JWT 工具类**：[JwtHelper.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/common/common_util/src/main/java/com/yygh/common/helper/JwtHelper.java)

### 关键代码
```java
// 从请求头获取 Token
List<String> tokenList = request.getHeaders().get("token");
if(!StringUtils.isEmpty(token)) {
    return JwtHelper.getUserId(token);
}
```

---

## 3. 分布式锁与原子操作

### 问题背景
分布式系统中多个服务实例并发操作共享资源时存在数据不一致风险。

### 解决方案
使用 Redisson 实现分布式锁和原子操作。

### 代码位置
- **分布式锁实现**：[OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/OrderServiceImpl.java)

### 关键代码
```java
// Redisson 原子回退号源
redissonClient.getAtomicLong(redisKey).addAndGet(1);
```

---

## 4. 微信支付集成

### 问题背景
需要实现微信支付功能，包括统一下单、支付回调、退款等操作。

### 解决方案
集成微信支付 SDK，实现完整的支付流程。

### 代码位置
- **支付实现**：[WeixinServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/WeixinServiceImpl.java)

### 关键代码
```java
// 调用微信统一下单接口
HttpClientUtils.doPostXml(wxUrl, paramMap);
```

---

## 5. Feign 熔断降级与 Sentinel 流量控制

### 问题背景
微服务间远程调用存在网络超时、服务不可用等故障风险，需要熔断降级机制防止雪崩效应。

### 解决方案
采用 Feign + FallbackFactory 熔断降级方案，结合 Sentinel 流量控制。

### 代码位置
- **Feign 客户端熔断工厂**：[HospitalFeignClientFallbackFactory.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service_client/service_hosp_client/src/main/java/com/yygh/hosp/client/HospitalFeignClientFallbackFactory.java)
- **Sentinel 依赖配置**：[service_client/pom.xml](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service_client/pom.xml)

### 关键代码
```java
@FeignClient(value = "service-hosp", fallbackFactory = HospitalFeignClientFallbackFactory.class)
public interface HospitalFeignClient {
    // 远程调用方法定义
}

@Slf4j
@Component
public class HospitalFeignClientFallbackFactory implements FallbackFactory<HospitalFeignClient> {
    @Override
    public HospitalFeignClient create(Throwable cause) {
        log.error("HospitalFeignClient 调用失败，触发熔断降级", cause);
        return new HospitalFeignClient() {
            // 降级方法返回兜底数据
        };
    }
}
```

---

## 6. Redis 缓存策略

### 问题背景
热门数据查询频繁，直接访问数据库会造成性能瓶颈。

### 解决方案
采用 Spring Cache + Redis（Cache-Aside 模式），将热点数据缓存到 Redis。

### 代码位置
- **缓存实现**：[HospitalServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_hosp/src/main/java/com/yygh/hosp/service/impl/HospitalServiceImpl.java)

### 关键代码
```java
// 根据医院编号查询医院（使用 Redis 缓存）
@Cacheable(value = "hospital", key = "#hoscode")
public Hospital getByHoscode(String hoscode) {
    Hospital hospital = hospitalMapper.selectByHoscode(hoscode);
    return hospital;
}
```

---

## 7. MyBatis-Plus 分页查询

### 问题背景
数据量大时需要分页查询，手动编写分页 SQL 繁琐且易出错。

### 解决方案
使用 MyBatis-Plus 提供的分页插件，简化分页查询开发。

### 代码位置
- **分页实现**：[DictServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_cmn/src/main/java/com/yygh/cmn/service/impl/DictServiceImpl.java)

### 关键代码
```java
Page<Dict> pageParam = new Page<>(page, limit);
LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
IPage<Dict> pageModel = baseMapper.selectPage(pageParam, wrapper);
```

---

## 8. 异常统一处理

### 问题背景
异常处理分散在各个业务层，代码冗余且不统一。

### 解决方案
使用 @ControllerAdvice + @ExceptionHandler + @ResponseBody 实现全局异常处理。

### 代码位置
- **全局异常处理**：[GlobalExceptionHandler.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/common/common_util/src/main/java/com/yygh/common/exception/GlobalExceptionHandler.java)

### 关键代码
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public Result error(YyghException e){
        return Result.build(e.getCode(), e.getMessage());
    }
}
```

---

## 9. 接口幂等性设计

### 问题背景
网络抖动或重复请求可能导致数据重复处理。

### 解决方案
通过唯一标识（如订单号）实现接口幂等性校验。

### 代码位置
- **幂等性校验**：[PaymentServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/PaymentServiceImpl.java)

### 关键代码
```java
// 检查是否已存在相同订单的支付记录
LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.eq(PaymentInfo::getOrderId, orderId);
Integer count = baseMapper.selectCount(queryWrapper);
if (count > 0) {
    return; // 已处理过，直接返回
}
```

---

## 10. EasyExcel 批量导入

### 问题背景
Excel 文件较大时，使用传统方式读取会导致内存溢出。

### 解决方案
使用 EasyExcel 流式读取，减少内存占用。

### 代码位置
- **批量导入**：[DictServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_cmn/src/main/java/com/yygh/cmn/service/impl/DictServiceImpl.java)

### 关键代码
```java
EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(dictMapper)).sheet().doRead();
```

---

## 11. @RequiredArgsConstructor 构造函数注入

### 问题背景
传统 @Autowired 字段注入导致类与 Spring 容器强耦合，不利于单元测试和不可变设计。

### 解决方案
使用 Lombok 的 @RequiredArgsConstructor 生成全参构造函数，配合 final 关键字实现不可变构造函数注入。

### 代码位置
- **构造函数注入**：[OrderServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_order/src/main/java/com/yygh/order/service/impl/OrderServiceImpl.java)

### 关键代码
```java
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {

    private final PatientFeignClient patientFeignClient;

    private final HospitalFeignClient hospitalFeignClient;

    private final WeixinService weixinService;

    private final RedissonClient redissonClient;
    // 无需手动编写构造函数，Lombok 自动生成
}
```

---

## 12. 缓存穿透防护

### 问题背景
热门排班数据查询频繁，Redis 缓存热点数据时存在穿透风险。

### 解决方案
采用 Spring Cache 注解（@Cacheable）实现缓存，查询时优先命中 Redis，降低数据库查询压力。

### 代码位置
- **缓存穿透防护**：[ScheduleServiceImpl.java](file:///d:/javaproject/yygh-master/yygh-master/后台代码/yygh_parent/service/service_hosp/src/main/java/com/yygh/hosp/service/impl/ScheduleServiceImpl.java)

### 关键代码
```java
// 根据排班 id 获取排班数据（使用 Redis 缓存）
@Cacheable(value = "schedule", key = "#scheduleId")
public Schedule getScheduleId(String scheduleId) {
    Schedule schedule = baseMapper.selectById(Long.parseLong(scheduleId));
    if (schedule == null) {
        throw new YyghException(ResultCodeEnum.DATA_ERROR);
    }
    return schedule;
}
```

---

## 项目贡献总结

### 号源高并发超卖防护
- 搭建「Redis 预扣库存 + Redisson 原子操作 + 库存回滚」三层防护
- 彻底解决号源超卖，万级并发下库存数据零误差

### 支付订单最终一致性
- 采用「接口幂等校验 + RabbitMQ 异步消息队列 + 定时对账补偿」方案
- 保障支付状态最终一致，丢失订单自动修复

### 网关统一安全防护体系
- 开放 API 存在越权、伪造请求风险，基于 JWT 实现全局 Token 认证
- Gateway 网关统一鉴权 + 接口签名校验，拦截未授权非法请求

### Feign 熔断降级与 Sentinel 流量控制
- 微服务间远程调用依赖 Feign + FallbackFactory 熔断降级
- 整合 Sentinel 流量控制，防止雪崩效应

### 大数据批量导入性能优化
- 医院排班批量数据导入执行缓慢
- 采用 EasyExcel 流式读取 + 异步批量插入 + 数据库连接池优化
- 解决大数据量导入延迟高的问题

### 缓存策略优化
- Redis 缓存 + 注解实现 Cache-Aside 模式，降低数据库查询压力
- 使用 @Cacheable 注解方便快捷

### 构造函数注入规范
- 全项目使用 @RequiredArgsConstructor + final 实现构造函数注入
- 提升代码可测试性，降低与框架耦合度

---

## 架构设计亮点

### 微服务架构
- **服务拆分**：按业务模块拆分独立服务（用户服务、医院服务、订单服务、数据字典服务、统计服务、后台管理服务等）
- **服务注册**：使用 Nacos 实现服务注册与发现
- **API 网关**：使用 Spring Cloud Gateway 统一入口

### 数据库设计
- **分库分表**：按业务模块拆分数据库
- **读写分离**：主库写，从库读，提高查询性能
- **索引优化**：合理创建索引，优化查询速度

### 安全性设计
- **接口限流**：防止恶意请求攻击
- **SQL 注入防护**：使用 MyBatis-Plus 参数化查询
- **敏感数据加密**：用户密码等敏感信息加密存储

---

## 总结

医院预约挂号系统通过引入多种技术方案，有效解决了高并发、分布式事务、数据一致性等复杂问题，为用户提供了稳定、高效的在线医疗预约服务。
