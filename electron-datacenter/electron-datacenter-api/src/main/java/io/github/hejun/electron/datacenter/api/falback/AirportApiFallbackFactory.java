package io.github.hejun.electron.datacenter.api.falback;

import feign.FeignException;
import io.github.hejun.electron.datacenter.api.AirportApi;
import io.github.hejun.electron.datacenter.vo.AirportVO;
import io.github.hejun.electron.datacenter.vo.ZoneVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息Feign回滚
 *
 * @author HeJun
 */
@Slf4j
@Component
public class AirportApiFallbackFactory implements FallbackFactory<AirportApi> {

	@Override
	public AirportApi create(Throwable cause) {
		return new AirportApi() {

			@Override
			public List<AirportVO> findAirportByCityCode(String cityCode) {
				log.error("AirportApi findAirportByCityCode Error: {}", cause.getMessage());
				if (cause instanceof FeignException e) {
					throw e;
				}
				throw new RuntimeException(cause);
			}

			@Override
			public ZoneVO findZoneByAirportCode(String airportCode) {
				log.error("AirportApi findZoneByAirportCode Error: {}", cause.getMessage());
				if (cause instanceof FeignException e) {
					throw e;
				}
				throw new RuntimeException(cause);
			}

		};
	}

}
