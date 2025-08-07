package io.github.hejun.electron.notifications.config;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign配置
 *
 * @author HeJun
 */
@ConditionalOnClass(Feign.class)
@EnableFeignClients(basePackages = "io.github.hejun.electron.notifications.api")
public class MessageFeignConfig {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass({HttpServletRequest.class, RequestInterceptor.class, RequestTemplate.class, Feign.class})
	public RequestInterceptor requestInterceptor() {
		return template -> {
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
			HttpServletRequest request = servletRequestAttributes.getRequest();
			String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
			if (StringUtils.hasText(authorizationHeader)) {
				template.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
			}
		};
	}

}
