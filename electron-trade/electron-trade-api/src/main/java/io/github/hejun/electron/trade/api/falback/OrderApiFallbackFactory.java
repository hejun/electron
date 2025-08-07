package io.github.hejun.electron.trade.api.falback;

import feign.FeignException;
import io.github.hejun.electron.trade.api.OrderApi;
import io.github.hejun.electron.trade.dto.OrderDTO;
import io.github.hejun.electron.trade.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 消息Feign回滚
 *
 * @author HeJun
 */
@Slf4j
@Component
public class OrderApiFallbackFactory implements FallbackFactory<OrderApi> {

	@Override
	public OrderApi create(Throwable cause) {
		return new OrderApi() {

			@Override
			public OrderVO create(OrderDTO orderDTO) {
				log.error("OrderApi create Error: {}", cause.getMessage());
				if (cause instanceof FeignException e) {
					throw e;
				}
				throw new RuntimeException(cause);
			}

		};
	}

}
