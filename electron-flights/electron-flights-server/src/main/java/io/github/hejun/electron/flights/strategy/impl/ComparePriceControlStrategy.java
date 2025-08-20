package io.github.hejun.electron.flights.strategy.impl;

import io.github.hejun.electron.flights.strategy.IPricesControlStrategy;
import io.github.hejun.electron.flights.vo.FlightPricesVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 控制价格
 *
 * @author HeJun
 */
@Component
public class ComparePriceControlStrategy implements IPricesControlStrategy {

	@Override
	public void control(FlightPricesVO flightPricesVO) {
		List<FlightPricesVO.FlightCabin> cabins = flightPricesVO.getCabins();
		if (CollectionUtils.isNotEmpty(cabins)) {
			for (FlightPricesVO.FlightCabin cabin : cabins) {
				if (cabin.getCostPrice() > cabin.getSellPrice()) {
					cabin.setSellPrice(cabin.getCostPrice());
				}
			}
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

}
