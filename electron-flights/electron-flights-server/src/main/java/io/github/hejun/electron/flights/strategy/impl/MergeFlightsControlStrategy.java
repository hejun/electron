package io.github.hejun.electron.flights.strategy.impl;

import io.github.hejun.electron.flights.strategy.IFlightsControlStrategy;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 合并航班
 *
 * @author HeJun
 */
@Component
public class MergeFlightsControlStrategy implements IFlightsControlStrategy {

	@Override
	@SuppressWarnings("unchecked")
	public void control(FlightsSearchVO flightsSearchVO) {
		List<FlightsSearchVO.FlightInfo> flights = flightsSearchVO.getFlights();
		if (CollectionUtils.isNotEmpty(flights)) {
			Map<String, FlightsSearchVO.FlightInfo> check = new HashMap<>();
			for (FlightsSearchVO.FlightInfo flightInfo : flights) {
				String flightGroup = flightInfo.getFlightSegments().stream()
					.map(s -> String.join("", s.getAirline(), s.getFlightNo()))
					.collect(Collectors.joining("_"));
				if (!check.containsKey(flightGroup)) {
					Map<String, Object> ext = new HashMap<>(Optional.ofNullable(flightInfo.getExt()).orElse(Map.of()));
					ext.put("supplier", new ArrayList<>(List.of(flightInfo.getSupplier())));
					flightInfo.setExt(ext);
					check.put(flightGroup, flightInfo);
				} else {
					FlightsSearchVO.FlightInfo prev = check.get(flightGroup);
					((List<String>) prev.getExt().get("supplier")).add(flightInfo.getSupplier());
					if (flightInfo.getMinPrice() < prev.getMinPrice()) {
						prev.setMinPrice(flightInfo.getMinPrice());
					}
				}
			}
			flightsSearchVO.setFlights(new ArrayList<>(check.values()));
			flightsSearchVO.setTotal(flightsSearchVO.getFlights().size());
		}
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
