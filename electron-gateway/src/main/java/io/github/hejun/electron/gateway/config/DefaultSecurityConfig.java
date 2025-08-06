package io.github.hejun.electron.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * 默认安全配置
 *
 * @author HeJun
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class DefaultSecurityConfig {

	@Bean
	public SecurityWebFilterChain defaultSecurityWebFilterChain(ServerHttpSecurity http,
																GlobalCorsProperties globalCorsProperties,
																ReactiveClientRegistrationRepository reactiveClientRegistrationRepository) {
		UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
		corsConfigurationSource.setCorsConfigurations(globalCorsProperties.getCorsConfigurations());
		http
			.authorizeExchange((exchange) -> exchange.anyExchange().authenticated())
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			.oauth2Login(oauth2Login -> oauth2Login
				.authorizationRequestResolver(createServerOAuth2AuthorizationRequestResolver(reactiveClientRegistrationRepository)))
			.oauth2Client(Customizer.withDefaults());
		return http.build();
	}

	public ServerOAuth2AuthorizationRequestResolver createServerOAuth2AuthorizationRequestResolver(ReactiveClientRegistrationRepository reactiveClientRegistrationRepository) {
		return new DefaultServerOAuth2AuthorizationRequestResolver(reactiveClientRegistrationRepository) {

			/**
			 * 对应 WebSessionServerRequestCache 的 sessionAttrName
			 * @see WebSessionServerRequestCache
			 */
			private final String sessionAttrName = "SPRING_SECURITY_SAVED_REQUEST";

			@Override
			public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
				return super.resolve(exchange).doOnNext(oAuth2AuthorizationRequest ->
					Optional.ofNullable(exchange)
						.map(ServerWebExchange::getRequest)
						.map(ServerHttpRequest::getQueryParams)
						.map(queryParams -> queryParams.get("redirect_uri"))
						.map(List::getFirst)
						.ifPresent(redirectUri -> exchange.getSession().subscribe(session -> {
							session.getAttributes().put(sessionAttrName, redirectUri);
							if (log.isDebugEnabled()) {
								log.debug("QueryParams Redirect URI: {}", redirectUri);
							}
						}))
				);
			}
		};
	}

}
