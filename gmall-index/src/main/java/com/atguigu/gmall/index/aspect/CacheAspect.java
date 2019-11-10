package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class CacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 1:方法返回值为Objcet
     * 2:方法的参数ProceedingJoinPoint
     * 3:方法必须抛出Throwable
     * 4:通过joinPoint.proceed(args)执行原始方法
     */
    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")// 注解所在包的全路径
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解的属性值
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);// 获取哪一个注解类的对象
        Class returnType = signature.getReturnType();// 获取方法的返回值类型
        String prefix = annotation.prefix();// 对象.属性名获取前缀
        String args = Arrays.asList(joinPoint.getArgs()).toString();// 获取方法参数
        String key = prefix + ":" + args;
        // 查询缓存
        Object result = this.cacheHit(key, returnType);
        if (result != null){
            return result;
        }

        // 分布式锁
        RLock lock = this.redissonClient.getLock("lock" + args);
        lock.lock();
        // 查询缓存
        result = this.cacheHit(key, returnType);
        // 如果以后缓存直接返回
        if (result != null){
            lock.unlock();
            return result;
        }


        result = joinPoint.proceed(joinPoint.getArgs());

        // 存入缓存，释放分布式锁
        long timeout = annotation.timeout();
        timeout = (long)(timeout + Math.random() * annotation.random());
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout, TimeUnit.SECONDS);
        lock.unlock();

        return result;
    }
    private Object cacheHit(String key, Class returnType){
        String jsonString = this.redisTemplate.opsForValue().get(key);
        // 如果有直接返回
        if (StringUtils.isNotBlank(jsonString)){
            return JSON.parseObject(jsonString, returnType);
        }
        return null;
    }
}
