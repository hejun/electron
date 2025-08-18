package io.github.hejun.electron.datacenter.api;

import io.github.hejun.electron.datacenter.api.falback.AirportApiFallbackFactory;
import io.github.hejun.electron.datacenter.vo.AirportVO;
import io.github.hejun.electron.datacenter.vo.ZoneVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 机场服务Api
 *
 * @author HeJun
 */
@FeignClient(name = "electron-datacenter", contextId = "airport", fallbackFactory = AirportApiFallbackFactory.class)
public interface AirportApi {

	@GetMapping("/airport/findAirportByCityCode")
	List<AirportVO> findAirportByCityCode(@RequestParam String cityCode);

	@GetMapping("/airport/findZoneByAirportCode")
	ZoneVO findZoneByAirportCode(@RequestParam String airportCode);

}
