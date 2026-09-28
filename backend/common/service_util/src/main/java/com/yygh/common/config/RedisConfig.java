package com.yygh.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Redis配置类
 *
 * @author XXJ
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 自定义key规则
     * @return
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }

    /**
     * 设置RedisTemplate规则
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        //序列号key value
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 各业务缓存的基准有效期（秒）。
     *
     * <p>不同业务的过期时间刻意错开，避免大量 key 在同一时刻集中失效引发缓存雪崩。
     */
    private static final Map<String, Long> CACHE_TTL_SECONDS = Map.of(
            "dict", 1800L,      // 数据字典变更极少
            "hospital", 600L,   // 医院基础信息
            "dept", 600L,       // 科室信息
            "schedule", 300L    // 排班/号源变化频繁，过期时间最短
    );

    /** 未单独配置的缓存使用的默认有效期（秒） */
    private static final long DEFAULT_TTL_SECONDS = 600L;

    /** 叠加到基准有效期上的随机抖动上限（秒），打散同一业务 key 的过期时刻 */
    private static final int TTL_JITTER_SECONDS = 120;

    /**
     * 设置CacheManager缓存规则
     *
     * <p>此前 {@code entryTtl(600 + random(180))} 写在 defaultCacheConfig 里，
     * 而随机数在 Bean 初始化时只计算一次 —— 结果是<b>所有 key 共用同一个 TTL</b>，
     * 注释声称的随机化防雪崩实际没有生效。改为按 cacheName 分别配置，并各自叠加随机抖动。
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));

        // 为每个业务缓存单独设置过期时间（各自叠加随机抖动）
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        for (Map.Entry<String, Long> entry : CACHE_TTL_SECONDS.entrySet()) {
            long ttl = entry.getValue() + ThreadLocalRandom.current().nextInt(TTL_JITTER_SECONDS);
            cacheConfigurations.put(entry.getKey(), baseConfig.entryTtl(Duration.ofSeconds(ttl)));
        }

        return RedisCacheManager.builder(factory)
                .cacheDefaults(baseConfig.entryTtl(Duration.ofSeconds(DEFAULT_TTL_SECONDS)))
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
