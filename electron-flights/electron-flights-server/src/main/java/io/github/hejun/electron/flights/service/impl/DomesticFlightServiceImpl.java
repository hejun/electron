package io.github.hejun.electron.flights.service.impl;

import io.github.hejun.electron.flights.dto.FlightPricesDTO;
import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.service.IDomesticFlightService;
import io.github.hejun.electron.flights.service.ISupplierAccountService;
import io.github.hejun.electron.flights.strategy.IFlightsControlStrategy;
import io.github.hejun.electron.flights.strategy.IPricesControlStrategy;
import io.github.hejun.electron.flights.supplier.ISupplierSupport;
import io.github.hejun.electron.flights.vo.FlightPricesVO;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

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
	private final List<IFlightsControlStrategy> flightsControlStrategies;
	private final List<IPricesControlStrategy> pricesControlStrategies;

	@Override
	public FlightsSearchVO searchFlights(FlightsSearchDTO search) {
		FlightsSearchVO vo = new FlightsSearchVO();

		List<SupplierAccount> approvedAccounts = supplierAccountService.findApprovedAccounts();
		if (CollectionUtils.isEmpty(approvedAccounts) || CollectionUtils.isEmpty(supportSupplierMap)) {
			vo.setFlights(List.of());
			return vo;
		}

		List<CompletableFuture<FlightsSearchVO>> futureList = new ArrayList<>();

		for (SupplierAccount approvedAccount : approvedAccounts) {
			if (supportSupplierMap.containsKey(approvedAccount.getCode())) {
				RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
				CompletableFuture<FlightsSearchVO> future = CompletableFuture.supplyAsync(() -> {
					RequestContextHolder.setRequestAttributes(requestAttributes);
					return supportSupplierMap.get(approvedAccount.getCode()).searchDomesticFlights(approvedAccount, search);
				});
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

		if (!CollectionUtils.isEmpty(flightsControlStrategies)) {
			for (IFlightsControlStrategy strategy : flightsControlStrategies) {
				strategy.control(vo);
			}
		}

		return vo;
	}

	@Override
	public FlightPricesVO searchPrices(FlightPricesDTO flightPricesDTO) {
		FlightPricesVO vo = new FlightPricesVO();

		List<SupplierAccount> approvedAccounts = supplierAccountService.findApprovedAccounts();
		if (CollectionUtils.isEmpty(approvedAccounts) || CollectionUtils.isEmpty(supportSupplierMap)) {
			vo.setCabins(List.of());
			return vo;
		}

		List<CompletableFuture<FlightPricesVO>> futureList = new ArrayList<>();

		for (SupplierAccount approvedAccount : approvedAccounts) {
			if (supportSupplierMap.containsKey(approvedAccount.getCode())) {
				RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
				CompletableFuture<FlightPricesVO> future = CompletableFuture.supplyAsync(() -> {
					RequestContextHolder.setRequestAttributes(requestAttributes);
					return supportSupplierMap.get(approvedAccount.getCode()).searchDomesticPrices(approvedAccount, flightPricesDTO);
				});
				futureList.add(future);
			}
		}

		CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new));

		List<FlightPricesVO.FlightCabin> flightInfos = new ArrayList<>();
		for (CompletableFuture<FlightPricesVO> future : futureList) {
			try {
				FlightPricesVO result = future.get();

				if (result != null && !CollectionUtils.isEmpty(result.getCabins())) {
					flightInfos.addAll(result.getCabins());
				}
			} catch (InterruptedException | ExecutionException e) {
				log.error("查询航班异常: {}", e.getMessage(), e);
			}
		}
		vo.setCabins(flightInfos);

		if (!CollectionUtils.isEmpty(pricesControlStrategies)) {
			for (IPricesControlStrategy strategy : pricesControlStrategies) {
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
