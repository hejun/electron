package io.github.hejun.electron.flights.supplier;

import io.github.hejun.electron.flights.dto.FlightPricesDTO;
import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.vo.FlightPricesVO;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;

/**
 * 供方实现
 *
 * @author HeJun
 */
public interface ISupplierSupport {

	FlightsSearchVO searchDomesticFlights(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO);

	FlightPricesVO searchDomesticPrices(SupplierAccount supplierAccount, FlightPricesDTO flightPricesDTO);

	String supportType();

}
