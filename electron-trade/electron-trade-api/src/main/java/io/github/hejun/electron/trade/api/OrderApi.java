package io.github.hejun.electron.trade.api;

import io.github.hejun.electron.trade.api.falback.OrderApiFallbackFactory;
import io.github.hejun.electron.trade.dto.OrderDTO;
import io.github.hejun.electron.trade.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 消息服务Api
 *
 * @author HeJun
 */
@FeignClient(name = "electron-trade", contextId = "order", fallbackFactory = OrderApiFallbackFactory.class)
public interface OrderApi {

	@PostMapping
	OrderVO create(@Valid @RequestBody OrderDTO orderDTO);

}
