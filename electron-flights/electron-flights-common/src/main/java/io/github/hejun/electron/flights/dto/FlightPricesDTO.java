package io.github.hejun.electron.flights.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class FlightPricesDTO {

	@NotEmpty(message = "行程不可为空")
	private List<FlightPricesSegment> segments;

	private Integer adultCount;

	private Integer childCount;

	private Integer infantCount;

	@Getter
	@Setter
	public static class FlightPricesSegment {

		@NotBlank(message = "出发站点不可为空")
		private String departureAirlineCode;

		@NotBlank(message = "到达站点不可为空")
		private String arrivalAirlineCode;

		@NotNull(message = "出发日期不可为空")
		@JsonFormat(pattern = "yyyy-MM-dd")
		private Date departureDate;

		@NotBlank(message = "航司不可为空")
		private String airline;

		@NotBlank(message = "航班号不可为空")
		private String flightNo;

	}

}
