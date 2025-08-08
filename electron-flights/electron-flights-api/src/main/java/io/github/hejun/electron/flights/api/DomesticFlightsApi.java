package io.github.hejun.electron.flights.api;

import io.github.hejun.electron.flights.api.falback.DomesticFlightsApiFallbackFactory;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 国内航班服务Api
 *
 * @author HeJun
 */
@FeignClient(name = "electron-flights", contextId = "flights", fallbackFactory = DomesticFlightsApiFallbackFactory.class)
public interface DomesticFlightsApi {

	@PostMapping("/domestic/search")
	FlightsSearchVO search(@RequestBody FlightsSearchVO flightsSearchVO);

}
