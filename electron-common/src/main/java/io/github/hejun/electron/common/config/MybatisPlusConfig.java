package io.github.hejun.electron.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * MybatisPlus 自动配置
 *
 * @author HeJun
 */
@Slf4j
@Configuration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class MybatisPlusConfig {

	@Bean
	@ConditionalOnMissingBean
	public MybatisPlusInterceptor mybatisPlusInterceptor(List<InnerInterceptor> interceptors) {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		interceptor.setInterceptors(interceptors);
		return interceptor;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass({PaginationInnerInterceptor.class})
	public PaginationInnerInterceptor paginationInnerInterceptor() {
		return new PaginationInnerInterceptor();
	}

}
