package io.github.hejun.electron.flights.service;

import io.github.hejun.electron.flights.entity.SupplierAccount;

import java.util.List;

/**
 * 供方账户 Service
 *
 * @author HeJun
 */
public interface ISupplierAccountService {

	List<SupplierAccount> findApprovedAccounts();

}
