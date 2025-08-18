package io.github.hejun.electron.flights.strategy;

import io.github.hejun.electron.flights.vo.FlightPricesVO;
import org.springframework.core.Ordered;

/**
 * 查询策略
 *
 * @author HeJun
 */
public interface IPricesControlStrategy extends Ordered {

	void control(FlightPricesVO flightPricesVO);

}
