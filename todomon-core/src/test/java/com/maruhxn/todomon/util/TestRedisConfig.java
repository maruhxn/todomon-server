package com.maruhxn.todomon.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.time.LocalDateTime;

@Profile("test")
@Configuration
public class TestRedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    @Primary
    public CacheManager dailyCacheManager(RedisConnectionFactory redisConnectionFactory) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next1AM = now.toLocalDate().atStartOfDay().plusDays(1).plusHours(1);
        Duration ttl = Duration.between(now, next1AM);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(ttl)
                .disableCachingNullValues(); // Null 캐싱 제외

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    @Bean
    public CacheManager weeklyCacheManager(RedisConnectionFactory redisConnectionFactory) {
        LocalDateTime now = LocalDateTime.now();

        // 다음 주 월요일 오전 1시를 계산
        LocalDateTime nextMonday1AM = now.with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay().plusHours(1);

        // 현재 시간부터 다음 주 월요일 오전 1시까지의 시간 계산
        Duration ttl = Duration.between(now, nextMonday1AM);

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(ttl)
                .disableCachingNullValues(); // Null 캐싱 제외

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}
