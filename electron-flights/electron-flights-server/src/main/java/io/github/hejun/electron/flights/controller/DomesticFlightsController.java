package io.github.hejun.electron.flights.controller;

import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.service.IDomesticFlightService;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 国内航班 Controller
 *
 * @author HeJun
 */
@RestController
@RequestMapping("/domestic")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DomesticFlightsController {

	private final IDomesticFlightService domesticFlightService;

	@PostMapping("/search")
	public FlightsSearchVO search(@Valid @RequestBody FlightsSearchDTO flightsSearchDTO) {
		return domesticFlightService.search(flightsSearchDTO);
	}

}
