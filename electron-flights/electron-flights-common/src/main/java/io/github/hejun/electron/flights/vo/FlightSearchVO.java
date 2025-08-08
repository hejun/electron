package io.github.hejun.electron.flights.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 航班查询
 *
 * @author HeJun
 */
@Getter
@Setter
public class FlightSearchVO {

	private String departureCityCode;

	private String arrivalCityCode;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date departureDate;

	private Integer adultCount;

	private Integer childCount;

	private Integer infantCount;

}
