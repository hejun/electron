package io.github.hejun.electron.datacenter.controller;

import io.github.hejun.electron.datacenter.vo.AirportVO;
import io.github.hejun.electron.datacenter.vo.ZoneVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * 消息 Controller
 *
 * @author HeJun
 */
@Slf4j
@RestController
@RequestMapping("/airport")
public class AirportController {

	@GetMapping("/findAirportByCityCode")
	public List<AirportVO> findAirportByCityCode(@Valid @NotBlank(message = "城市代码不可为空") String cityCode, Principal principal) {
		log.info("Airport findAirportByCityCode, cityCode: {}, name: {}", cityCode, principal.getName());

		if ("BJS".equals(cityCode)) {
			AirportVO pek = new AirportVO();
			pek.setCode("PEK");
			pek.setName("首都机场");
			AirportVO pkx = new AirportVO();
			pkx.setCode("PKX");
			pkx.setName("北京大兴机场");
			return List.of(pek, pkx);
		}

		AirportVO vo = new AirportVO();
		vo.setCode(cityCode);
		vo.setName("");
		return List.of(vo);
	}

	@GetMapping("/findZoneByAirportCode")
	public ZoneVO findZoneByAirportCode(@Valid @NotBlank(message = "机场代码不可为空") String airportCode, Principal principal) {
		log.info("Airport findZoneByAirportCode, airportCode: {}, name: {}", airportCode, principal.getName());
		if ("PKX".equals(airportCode) || "PEK".equals(airportCode)) {
			ZoneVO bjs = new ZoneVO();
			bjs.setCode("010100");
			bjs.setThreeCode("BJS");
			bjs.setName("北京市");
			return bjs;
		}
		ZoneVO vo = new ZoneVO();
		vo.setCode("");
		vo.setThreeCode(airportCode);
		vo.setName("");
		return vo;
	}

}
