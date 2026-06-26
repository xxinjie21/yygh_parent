package com.yygh.example;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * 高并发号源扣减示例
 * 演示如何使用 Redisson 实现原子性号源扣减
 */
@Service
public class HighConcurrencyExample {

    private final RedissonClient redissonClient;

    public HighConcurrencyExample(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 原子扣减号源
     * @param scheduleId 排班ID
     * @param expected 期望的库存数量
     * @return 是否扣减成功
     */
    public boolean deductStock(Long scheduleId, long expected) {
        // 构建 Redis Key
        String redisKey = "yygh:schedule:" + scheduleId;
        
        // 使用 Redisson RAtomicLong 实现原子扣减
        RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
        
        // compareAndSet 保证原子性：只有当当前值等于 expected 时才扣减
        return atomicLong.compareAndSet(expected, expected - 1);
    }

    /**
     * 原子回退号源
     * @param scheduleId 排班ID
     */
    public void revertStock(Long scheduleId) {
        String redisKey = "yygh:schedule:" + scheduleId;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
        // 原子增加
        atomicLong.addAndGet(1);
    }
}
