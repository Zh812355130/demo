package org.huan.demo.controller;

import jakarta.annotation.Resource;
import org.huan.demo.entity.KV;
import org.huan.demo.service.RedisTestService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/redis")
public class RedisTestController {

    @Resource
    private RedisTemplate<Object,Object> template;

    @Resource
    private RedisTestService service;

    @PostMapping("/add")
    public String addKV(@RequestBody KV kv){
        template.opsForValue().set(kv.getKey(),kv.getValue());
        return "success";
    }

    @GetMapping("/{key}")
    public String get(@PathVariable String key){
        return (String) template.opsForValue().get(key);
    }

    @GetMapping("/disLock")
    public String disLock(){
        return service.disLock();
    }

    @GetMapping("/reentrantDisLock")
    public String reentrantDisLock(){
        return service.reentrantDisLock();
    }



}
