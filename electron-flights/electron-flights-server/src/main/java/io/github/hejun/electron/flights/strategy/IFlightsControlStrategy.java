package io.github.hejun.electron.flights.strategy;

import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import org.springframework.core.Ordered;

/**
 * 查询策略
 *
 * @author HeJun
 */
public interface IFlightsControlStrategy extends Ordered {

	void control(FlightsSearchVO flightsSearchVO);

}
