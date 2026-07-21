package com.yygh.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存防击穿工具类 — 逻辑过期 + Redisson 互斥锁
 *
 * 适用场景：热点数据，高 QPS，不希望缓存过期瞬间大量请求击穿到 DB。
 * 原理：
 *   1. 缓存永不过期（物理 TTL 设得很长），但存储时附带逻辑过期时间戳
 *   2. 读取时检查逻辑过期时间，未过期直接返回
 *   3. 若逻辑过期，尝试获取互斥锁：获取成功则重建缓存，失败则返回旧数据
 *
 * @author XXJ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheBreakdownUtil {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    /** 互斥锁默认等待时间（秒） */
    private static final long LOCK_WAIT_SECONDS = 3;
    /** 互斥锁默认持有时间（秒） */
    private static final long LOCK_LEASE_SECONDS = 10;

    /**
     * 逻辑过期读缓存 — 防止缓存击穿
     *
     * @param cacheKey      Redis 缓存键
     * @param expireSeconds 逻辑过期时间（秒），到期后返回旧数据 + 异步刷新
     * @param dbLoader      数据库加载回调（仅在获取锁成功时执行）
     * @param clazz         缓存值类型
     * @param <T>           泛型
     * @return 缓存值，过期时返回旧值
     */
    public <T> T getWithLogicalExpire(String cacheKey, long expireSeconds,
                                       Supplier<T> dbLoader, Class<T> clazz) {
        // 1. 从 Redis 读取逻辑过期包装数据
        String json = stringRedisTemplate.opsForValue().get(cacheKey);
        if (json == null || json.isEmpty()) {
            // 缓存不存在，直接查库并写入缓存
            return loadAndSetCache(cacheKey, expireSeconds, dbLoader);
        }

        // 2. 解析逻辑过期时间
        JSONObject cacheData = JSON.parseObject(json);
        long expireTime = cacheData.getLongValue("expireTime");
        T data = cacheData.getObject("data", clazz);

        // 3. 未过期，直接返回
        if (expireTime > System.currentTimeMillis()) {
            return data;
        }

        // 4. 已逻辑过期，尝试获取互斥锁重建缓存
        String lockKey = "lock:cache:" + cacheKey;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                try {
                    // 双重检查：其他线程可能已重建
                    String doubleCheckJson = stringRedisTemplate.opsForValue().get(cacheKey);
                    if (doubleCheckJson != null) {
                        JSONObject doubleCheckData = JSON.parseObject(doubleCheckJson);
                        if (doubleCheckData.getLongValue("expireTime") > System.currentTimeMillis()) {
                            return doubleCheckData.getObject("data", clazz);
                        }
                    }
                    // 重建缓存
                    return loadAndSetCache(cacheKey, expireSeconds, dbLoader);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 5. 获取锁失败，返回旧数据（防止击穿到数据库）
        log.info("缓存逻辑过期但获取锁失败，返回旧数据，key：{}", cacheKey);
        return data;
    }

    /** 查库 + 写入 Redis（附带逻辑过期时间戳） */
    private <T> T loadAndSetCache(String cacheKey, long expireSeconds, Supplier<T> dbLoader) {
        T data = dbLoader.get();
        if (data != null) {
            JSONObject cacheData = new JSONObject();
            cacheData.put("data", data);
            cacheData.put("expireTime", System.currentTimeMillis() + expireSeconds * 1000);
            stringRedisTemplate.opsForValue().set(cacheKey, cacheData.toJSONString());
        }
        return data;
    }
}
