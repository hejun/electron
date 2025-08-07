package io.github.hejun.electron.trade.config;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringEncoder;
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
@EnableFeignClients(basePackages = "io.github.hejun.electron.trade.api")
public class OrderFeignConfig {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass({HttpServletRequest.class, RequestInterceptor.class, RequestTemplate.class})
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

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass({Feign.class})
	Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
		return new SpringFormEncoder(new SpringEncoder(converters));
	}

}
