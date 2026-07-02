package org.huan.demo.service.impl;

import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.huan.demo.core.RedisDistributedLock;
import org.huan.demo.service.RedisTestService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Service
public class RedisTestServiceImpl implements RedisTestService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public String reentrantDisLock() {
        Lock lock = new RedisDistributedLock(redisTemplate,"inventoryLock",IdUtil.fastSimpleUUID());
        String msg ="";
        try {
            lock.lock();
            String result = stringRedisTemplate.opsForValue().get("inventory");
            int inventoryNum = result==null ? 0 : Integer.parseInt(result);
            if(inventoryNum>0){
                stringRedisTemplate.opsForValue().set("inventory", String.valueOf(--inventoryNum));
                msg="商品剩余："+inventoryNum+"个";
            }else{
                msg = "商品卖完了！";
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return msg;
    }

    @Override
    public String disLock()  {
        String msg;
        String lockKey = "inventoryLock";
        String value = IdUtil.fastSimpleUUID();
        while (Boolean.FALSE.equals(stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, value,10,TimeUnit.SECONDS))){
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            String result = stringRedisTemplate.opsForValue().get("inventory");
            int inventoryNum = result==null ? 0 : Integer.parseInt(result);
            if(inventoryNum>0){
                stringRedisTemplate.opsForValue()
                        .set("inventory", String.valueOf(--inventoryNum));
                msg="商品剩余："+inventoryNum+"个";
            }else{
                msg = "商品卖完了！";
            }
            return msg;
        }finally {
            //A线程业务超过过期时间，B线程获取锁，A执行完成删除了B的锁 增加判断
//            if(value.equals(stringRedisTemplate.opsForValue().get(lockKey))){
//                stringRedisTemplate.delete(lockKey);
//            }
            String luaScript ="if redis.call('get',KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del',KEYS[1]) else return 0 end";
            stringRedisTemplate
                    .execute(new DefaultRedisScript<>(luaScript,Boolean.class),
                    Lists.newArrayList(lockKey),value);
        }
    }
}
