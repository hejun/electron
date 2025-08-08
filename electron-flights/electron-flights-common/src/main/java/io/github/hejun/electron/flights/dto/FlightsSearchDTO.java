package io.github.hejun.electron.flights.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class FlightsSearchDTO {

	@NotBlank(message = "出发城市不可为空")
	private String departureCityCode;

	@NotBlank(message = "到达城市不可为空")
	private String arrivalCityCode;

	@NotNull(message = "出发日期不可为空")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date departureDate;

	private Integer adultCount;

	private Integer childCount;

	private Integer infantCount;

}
