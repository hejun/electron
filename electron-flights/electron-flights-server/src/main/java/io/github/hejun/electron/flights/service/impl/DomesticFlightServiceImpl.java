package io.github.hejun.electron.flights.service.impl;

import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.service.IDomesticFlightService;
import io.github.hejun.electron.flights.service.ISupplierAccountService;
import io.github.hejun.electron.flights.strategy.IFlightControlStrategy;
import io.github.hejun.electron.flights.supplier.ISupplierSupport;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 航班服务
 *
 * @author HeJun
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DomesticFlightServiceImpl implements IDomesticFlightService {

	private final ISupplierAccountService supplierAccountService;
	private Map<String, ISupplierSupport> supportSupplierMap;
	private final List<IFlightControlStrategy> controlStrategies;

	@Override
	public FlightsSearchVO search(FlightsSearchDTO search) {
		FlightsSearchVO vo = new FlightsSearchVO();

		List<SupplierAccount> approvedAccounts = supplierAccountService.findApprovedAccounts();
		if (CollectionUtils.isEmpty(approvedAccounts) || CollectionUtils.isEmpty(supportSupplierMap)) {
			vo.setFlights(List.of());
			return vo;
		}

		List<CompletableFuture<FlightsSearchVO>> futureList = new ArrayList<>();

		for (SupplierAccount approvedAccount : approvedAccounts) {
			if (supportSupplierMap.containsKey(approvedAccount.getCode())) {
				CompletableFuture<FlightsSearchVO> future = CompletableFuture.supplyAsync(() ->
					supportSupplierMap.get(approvedAccount.getCode()).search(approvedAccount, search)
				);
				futureList.add(future);
			}
		}

		CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new));

		List<FlightsSearchVO.FlightInfo> flightInfos = new ArrayList<>();
		for (CompletableFuture<FlightsSearchVO> future : futureList) {
			try {
				FlightsSearchVO result = future.get();

				if (result != null && !CollectionUtils.isEmpty(result.getFlights())) {
					flightInfos.addAll(result.getFlights());
				}
			} catch (InterruptedException | ExecutionException e) {
				log.error("查询航班异常: {}", e.getMessage(), e);
			}
		}
		vo.setFlights(flightInfos);

		if (!CollectionUtils.isEmpty(controlStrategies)) {
			for (IFlightControlStrategy strategy : controlStrategies) {
				strategy.control(vo);
			}
		}

		return vo;
	}

	@Autowired
	public void setSupportSuppliers(List<ISupplierSupport> supportSuppliers) {
		if (CollectionUtils.isEmpty(supportSuppliers)) {
			return;
		}
		this.supportSupplierMap = new HashMap<>(supportSuppliers.size());
		for (ISupplierSupport supplier : supportSuppliers) {
			supportSupplierMap.put(supplier.supportType(), supplier);
		}
	}

}
