package com.item.sms.redis;

import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 简单的 Redis 分布式锁（安全释放使用 Lua 脚本）
 */
@Component
public class RedisLockUtil {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisLockUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取锁
     * @param key 锁 key
     * @param expireSeconds 过期时间（秒）
     * @return lockValue（成功时是随机uuid），失败返回 null
     */
    public String tryLock(String key, long expireSeconds) {
        String value = UUID.randomUUID().toString();
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(ok)) return value;
        return null;
    }

    /**
     * 安全释放锁：只有持有者（value相同）才能删除
     */
    public boolean releaseLock(String key, String value) {
        // Lua 脚本： if redis.call("get",KEYS[1]) == ARGV[1] then return redis.call("del",KEYS[1]) else return 0 end
        String lua = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        Object res = redisTemplate.execute((RedisCallback<Object>) connection ->
                connection.eval(lua.getBytes(StandardCharsets.UTF_8),
                        ReturnType.INTEGER,
                        1,
                        key.getBytes(StandardCharsets.UTF_8),
                        value.getBytes(StandardCharsets.UTF_8))
        );
        return res != null && Long.valueOf(1).equals(((Number)res).longValue());
    }
}