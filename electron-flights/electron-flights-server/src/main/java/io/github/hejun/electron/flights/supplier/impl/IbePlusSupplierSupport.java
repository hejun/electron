package io.github.hejun.electron.flights.supplier.impl;

import io.github.hejun.electron.flights.constant.Constants;
import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.supplier.ISupplierSupport;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * IBE+ 供方实现
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class IbePlusSupplierSupport implements ISupplierSupport {

	@Override
	public FlightsSearchVO search(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public String supportType() {
		return Constants.Supplier.IBE_PLUS;
	}

}
