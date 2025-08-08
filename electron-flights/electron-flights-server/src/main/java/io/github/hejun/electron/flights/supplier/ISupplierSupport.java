package io.github.hejun.electron.flights.supplier;

import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;

/**
 * 供方实现
 *
 * @author HeJun
 */
public interface ISupplierSupport {

	FlightsSearchVO search(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO);

	String supportType();

}
