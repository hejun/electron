package io.github.hejun.electron.notifications.config;

import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Feign配置
 *
 * @author HeJun
 */
@ConditionalOnClass(Feign.class)
@EnableFeignClients(basePackages = "io.github.hejun.electron.notifications.api")
public class MessageFeignConfig {
}
