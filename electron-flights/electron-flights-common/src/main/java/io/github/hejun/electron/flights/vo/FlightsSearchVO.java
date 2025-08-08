package io.github.hejun.electron.flights.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 航班查询
 *
 * @author HeJun
 */
@Getter
@Setter
public class FlightsSearchVO {

	private String departureCityCode;

	private String arrivalCityCode;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date departureDate;

	private List<FlightInfo> flights;

}
