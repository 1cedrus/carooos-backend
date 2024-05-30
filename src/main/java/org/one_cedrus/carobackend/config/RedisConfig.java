package org.one_cedrus.carobackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    @Value("${SPRING_REDIS_HOST}")
    private String redisHost;

    @Value("${SPRING_REDIS_PORT}")
    private Integer redisPort;

    @Value("${SPRING_REDIS_USERNAME}")
    private String username;

    @Value("${SPRING_REDIS_PASSWORD}")
    private String password;

    @Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        var redisConfig = new RedisStandaloneConfiguration(
            redisHost,
            redisPort
        );

        if (!username.isEmpty() && !password.isEmpty()) {
            redisConfig.setPassword(password);
            redisConfig.setUsername(username);
        }

        var lettuceClientConfig = LettuceClientConfiguration.builder()
            .useSsl()
            .build();

        return new LettuceConnectionFactory(redisConfig, lettuceClientConfig);
    }

    @Bean
    public RedisTemplate<String, ?> redisTemplate() {
        RedisTemplate<String, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory());
        return template;
    }
}
