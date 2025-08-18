package io.github.hejun.electron.flights.service;

import io.github.hejun.electron.flights.dto.FlightPricesDTO;
import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.vo.FlightPricesVO;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;

/**
 * 国内航班 Service
 *
 * @author HeJun
 */
public interface IDomesticFlightService {

	FlightsSearchVO searchFlights(FlightsSearchDTO search);

	FlightPricesVO searchPrices(FlightPricesDTO flightPricesDTO);

}
