package com.atguigu.gmall.index.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class GmallJedisConfig {

    @Bean
    public JedisPool jedisPool(){
        return new JedisPool("47.102.100.157", 6379);
    }
}
