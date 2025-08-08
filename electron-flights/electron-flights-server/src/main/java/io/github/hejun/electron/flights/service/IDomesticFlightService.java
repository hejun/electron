package io.github.hejun.electron.flights.service;

import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;

/**
 * 国内航班 Service
 *
 * @author HeJun
 */
public interface IDomesticFlightService {

	FlightsSearchVO search(FlightsSearchDTO search);

}
