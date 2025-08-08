package io.github.hejun.electron.flights.service.impl;

import io.github.hejun.electron.flights.constant.Constants;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.service.ISupplierAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供方账户 Service
 *
 * @author HeJun
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SupplierAccountServiceImpl implements ISupplierAccountService {

	@Override
	public List<SupplierAccount> findApprovedAccounts() {
		SupplierAccount supplierAccount = new SupplierAccount();
		supplierAccount.setSupplier(Constants.Supplier.IBE_PLUS);
		return List.of(supplierAccount);
	}

}
