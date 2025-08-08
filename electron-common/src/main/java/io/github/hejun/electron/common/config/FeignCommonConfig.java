package io.github.hejun.electron.common.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * Feign 自动配置
 *
 * @author HeJun
 */
@Slf4j
@Configuration
@ConditionalOnClass(Feign.class)
public class FeignCommonConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({HttpServletRequest.class, RequestInterceptor.class, RequestTemplate.class})
    public RequestInterceptor feignAuthorizationRequestInterceptor() {
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({SimpleModule.class, Jackson2ObjectMapperBuilderCustomizer.class, IPage.class})
    Jackson2ObjectMapperBuilderCustomizer iPageJackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            ObjectMapper mapper = new ObjectMapper();
            module.addDeserializer(IPage.class, new StdDeserializer<>(IPage.class) {
                @Override
                public IPage<?> deserialize(JsonParser p, DeserializationContext context) throws IOException {
                    TreeNode node = p.getCodec().readTree(p);
                    return mapper.readValue(node.toString(), Page.class);
                }
            });
            builder.modules(modules -> modules.add(module));
        };
    }

}
