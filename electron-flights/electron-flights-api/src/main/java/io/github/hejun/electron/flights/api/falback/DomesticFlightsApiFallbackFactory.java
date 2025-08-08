package io.github.hejun.electron.flights.api.falback;

import feign.FeignException;
import io.github.hejun.electron.flights.api.DomesticFlightsApi;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
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
public class DomesticFlightsApiFallbackFactory implements FallbackFactory<DomesticFlightsApi> {

	@Override
	public DomesticFlightsApi create(Throwable cause) {
		return new DomesticFlightsApi() {

			@Override
			public FlightsSearchVO search(FlightsSearchVO flightsSearchVO) {
				log.error("DomesticFlightsApi search Error: {}", cause.getMessage());
				if (cause instanceof FeignException e) {
					throw e;
				}
				throw new RuntimeException(cause);
			}

		};
	}

}
