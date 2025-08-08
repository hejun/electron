package io.github.hejun.electron.datacenter.api.falback;

import com.baomidou.mybatisplus.core.metadata.IPage;
import feign.FeignException;
import io.github.hejun.electron.datacenter.api.ZoneApi;
import io.github.hejun.electron.datacenter.vo.ZoneVO;
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
public class ZoneApiFallbackFactory implements FallbackFactory<ZoneApi> {

	@Override
	public ZoneApi create(Throwable cause) {
		return new ZoneApi() {

			@Override
			public IPage<ZoneVO> findPage(Integer pageNum, Integer pageSize) {
				log.error("ZoneApi findPage Error: {}", cause.getMessage());
				if (cause instanceof FeignException e) {
					throw e;
				}
				throw new RuntimeException(cause);
			}

		};
	}

}
