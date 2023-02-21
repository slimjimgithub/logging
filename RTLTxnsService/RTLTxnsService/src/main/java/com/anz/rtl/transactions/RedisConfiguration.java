package com.anz.rtl.transactions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.anz.rtl.transactions.response.TransactionsResponse;

@Configuration
public class RedisConfiguration {
    @Value("${spring.redis.host}")
    private String hostName;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    RedisConnectionFactory jedisConnectionFactory() {

        /* default */ RedisStandaloneConfiguration redisStandalonConfig = new RedisStandaloneConfiguration(hostName,
                port);
        redisStandalonConfig.setPassword(password);
        JedisClientConfiguration clientConfiguration = JedisClientConfiguration.builder().usePooling().build();
        JedisConnectionFactory factory = new JedisConnectionFactory(redisStandalonConfig, clientConfiguration);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    RedisTemplate<String, TransactionsResponse> redisTemplate() {
        /* default */ RedisTemplate<String, TransactionsResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());

        return redisTemplate;
    }
}
