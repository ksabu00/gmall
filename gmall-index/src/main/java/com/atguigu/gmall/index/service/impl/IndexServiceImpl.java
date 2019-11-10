package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.TIMEOUT;
import org.redisson.Redisson;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.jest.JestProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import sun.misc.Cache;

import java.security.PublicKey;
import java.sql.Time;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

/*@Service
public class IndexServiceImpl{
}*/

@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private GmallPmsFeign gmallPmsFeign;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:category";
    private static final long TIME_OUT = 300001;

    @Override
    public List<CategoryEntity> queryLevel1Category() {

        this.stringRedisTemplate.opsForValue().set("redis", "value1");
        System.out.println("===========" + this.stringRedisTemplate.opsForValue().get("redis"));

        Resp<List<CategoryEntity>> categoryResp = this.gmallPmsFeign.queryCategory(1, null);
        return categoryResp.getData();
    }

    // AOP+反射
    @Override
    @GmallCache(value = KEY_PREFIX, timeout = TIME_OUT, random = 50000001)
    public List<CategoryVO> querySubCategory(Long pid) {

        // 1:查询缓存，缓存中有直接返回
//        String categoryString = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if (StringUtils.isNotBlank(categoryString)){
//           return JSON.parseArray(categoryString, CategoryVO.class);
//        }
        // 2:缓存中没有，查询数据库
        Resp<List<CategoryVO>> listResp = this.gmallPmsFeign.queryCategoryWithSub(pid);
        List<CategoryVO> categoryVOS = listResp.getData();

        // 3:查询完之后，放入缓存(防止短时间内多条数据同时过期(雪崩)，每条设置进去的值都加入保存数据的随机值)
//        if (!CollectionUtils.isEmpty(categoryVOS)) {
//            this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryVOS), (int)(Math.random() * 5), TimeUnit.DAYS);
//        }
        return categoryVOS;
    }

    @Override
    public String testLock() {
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock(10, TimeUnit.SECONDS);

        String val = this.stringRedisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(val)) {
                return null;
            }
            int num = Integer.parseInt(val);
            this.stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));

            lock.unlock();
            return "已经添加成功！！！";
    }

    @Override
    public String read() {
        RReadWriteLock readWriteLock = this.redissonClient.getReadWriteLock("readWriteLock");
        readWriteLock.readLock().lock(10, TimeUnit.SECONDS);

        String msg = this.stringRedisTemplate.opsForValue().get("msg");

        //readWriteLock.readLock().unlock();
        return msg;
    }

    @Override
    public String write() {
        RReadWriteLock readWriteLock = this.redissonClient.getReadWriteLock("readWriteLock");
        readWriteLock.writeLock().lock(10, TimeUnit.SECONDS);

        String msg = UUID.randomUUID().toString();
        this.stringRedisTemplate.opsForValue().set("msg", msg);

        //readWriteLock.writeLock().unlock();

        return "写入数据成功" + msg;
    }

    @Override
    public String latch() {
        RCountDownLatch latchDown = this.redissonClient.getCountDownLatch("latchDown");

        String countString = this.stringRedisTemplate.opsForValue().get("count");
        int count = Integer.parseInt(countString);
        latchDown.trySetCount(count);


        try {
            latchDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "班长锁门";
    }

    @Override
    public String out() {

        RCountDownLatch latchDown = this.redissonClient.getCountDownLatch("latchDown");
        String countString = this.stringRedisTemplate.opsForValue().get("count");
        int count = Integer.parseInt(countString);
        this.stringRedisTemplate.opsForValue().set("count", String.valueOf(--count));
        latchDown.countDown();

        return "出来一个人啦";
    }

    // 个人使用完美方案
    /*@Override
    public String testLock() {
        // 所有请求竞争锁(且设置锁的过期时间，时间到了自动释放锁)
        String uuid = UUID.randomUUID().toString();
        //Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock",uuid, 10, TimeUnit.SECONDS);
        Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        // 获取到锁就执行业务逻辑
        if (lock) {
            String val = this.stringRedisTemplate.opsForValue().get("key1");
            if (StringUtils.isEmpty(val)) {
                return null;
            }
            int num = Integer.parseInt(val);

            this.stringRedisTemplate.opsForValue().set("key1", String.valueOf(++num));


            // 释放锁
            // 为了保证误伤锁，必须使用全局唯一的标志，删除前先进行判断是否为自己的锁
            Jedis jedis = null;
            try {
                jedis = this.jedisPool.getResource();
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Arrays.asList("lock"), Arrays.asList(uuid));
            }finally {
                if (jedis != null){
                    jedis.close();
                }
            }
            //this.stringRedisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), uuid);
//            if (StringUtils.equals(uuid, this.stringRedisTemplate.opsForValue().get("lock"))){
//                this.stringRedisTemplate.delete("lock");
//            }

        }else {
            // 没有获取到锁继续尝试获取锁
            try {
                TimeUnit.SECONDS.sleep(1);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //testLock();
        }
        return "已经添加成功！！！";
    }*/


}
