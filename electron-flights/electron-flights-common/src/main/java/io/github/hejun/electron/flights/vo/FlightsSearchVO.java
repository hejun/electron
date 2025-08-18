package io.github.hejun.electron.flights.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 航班查询
 *
 * @author HeJun
 */
@Getter
@Setter
public class FlightsSearchVO {

	private Integer total;

	private List<FlightInfo> flights;

	@Getter
	@Setter
	public static class FlightInfo {

		@JsonIgnore
		private String supplier;

		private List<FlightSegment> flightSegments;

		private Double minPrice;

		private Map<String, Object> ext;

		@Getter
		@Setter
		public static class FlightSegment {

			private String airline;

			private String flightNo;

			private String departureAirlineCode;

			private String departureDate;

			private String departureTime;

			private String departureTerminalCode;

			private String arrivalAirlineCode;

			private String arrivalDate;

			private String arrivalTime;

			private String arrivalTerminalCode;

			private Long duration;

			private ShareFlightInfo shareFlight;

			private String aircraftType;

			private String mileage;

			private List<FlightStop> stops;

			private String meal;

			@Getter
			@Setter
			public static class ShareFlightInfo {

				private String airline;

				private String flightNo;

			}

			@Getter
			@Setter
			public static class FlightStop {

				private String stopAirlineCode;

				private String stopTime;

				private String arrivalTerminalCode;

				private String departureTerminalCode;

			}

		}

	}

}
