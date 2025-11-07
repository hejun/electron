package io.github.hejun.electron.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 默认安全配置
 *
 * @author HeJun
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class DefaultSecurityConfig {

	@Bean
	@Order(SecurityProperties.BASIC_AUTH_ORDER)
	public SecurityWebFilterChain defaultSecurityWebFilterChain(ServerHttpSecurity http,
																GlobalCorsProperties globalCorsProperties) {
		UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
		corsConfigurationSource.setCorsConfigurations(globalCorsProperties.getCorsConfigurations());
		http
			.authorizeExchange((exchange) -> exchange.anyExchange().authenticated())
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
			.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
		return http.build();
	}

}
