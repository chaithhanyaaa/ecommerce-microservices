package com.ecommerce.inventoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());

        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Integer.class));

        return redisTemplate;
    }


    @Bean
    public RedisScript<Long> deductStockScript() {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();

        redisScript.setLocation(new ClassPathResource("scripts/deduct-stock.lua"));

        redisScript.setResultType(Long.class);

        return redisScript;
    }
}