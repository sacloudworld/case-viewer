package com.example.case_viewer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()

                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new GenericJackson2JsonRedisSerializer()));

                                                

                RedisCacheConfiguration usersConfig =
                        defaultConfig.entryTtl(Duration.ofMinutes(10));
        
                RedisCacheConfiguration productsConfig =
                        defaultConfig.entryTtl(Duration.ofHours(2));
        
                RedisCacheConfiguration casesConfig =
                        defaultConfig.entryTtl(Duration.ofMinutes(3));
        
                Map<String, RedisCacheConfiguration> cacheConfigurations =
                        Map.of(
                                "users", usersConfig,
                                "products", productsConfig,
                                "cases", casesConfig
                        );
        
                return RedisCacheManager.builder(connectionFactory)
                        .cacheDefaults(defaultConfig)
                        .withInitialCacheConfigurations(cacheConfigurations)
                        .enableStatistics()
                        .build();
    }
}