package org.huan.demo.core;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class RedisDistributedLock implements Lock {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final String lockName;
    private final String lockValue;
    private final long expireTime;

    public RedisDistributedLock(RedisTemplate<Object, Object> redisTemplate, String lockName, String flag) {
        this.redisTemplate = redisTemplate;
        this.lockName = lockName;
        this.lockValue = flag + "-" + Thread.currentThread().getId();
        this.expireTime = 50L;
    }

    @Override
    public void lock() {
        tryLock();
    }

    @Override
    public boolean tryLock() {
        return tryLock(-1, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        if (time == -1) {
            String script = "if redis.call('exists',KEYS[1]) == 0  or " +
                    "redis.call('hexists',KEYS[1],ARGV[1]) == 1 then " +
                    " redis.call('hincrby',KEYS[1],ARGV[1],1) " +
                    " redis.call('expire',KEYS[1],ARGV[2]) " +
                    " return 1 else return 0 end";
            while (Boolean.FALSE.equals(redisTemplate
                    .execute(new DefaultRedisScript<>(script, Boolean.class),
                            Collections.singletonList(lockName), lockValue, expireTime))) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void unlock() {
        String script = "if redis.call('hexists',KEYS[1],ARGV[1]) == 0" +
                " then return nil elseif " +
                " redis.call('hincrby',KEYS[1],ARGV[1],-1) == 0" +
                " then return redis.call('del',KEYS[1])" +
                " else return 0 end";
        Long result = redisTemplate
                .execute(new DefaultRedisScript<>(script, Long.class),
                        Collections.singletonList(lockName), lockValue);
        if (result == null) {
            throw new RuntimeException("lock not exists");
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
