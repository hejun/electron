package io.github.hejun.electron.flights.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {

	@Bean
	public <T> RedisTemplate<String, T> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, T> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(RedisSerializer.string());
		template.setValueSerializer(RedisSerializer.json());
		template.setHashKeySerializer(RedisSerializer.string());
		template.setHashValueSerializer(RedisSerializer.json());
		template.afterPropertiesSet();
		return template;
	}

}
