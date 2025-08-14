package io.github.hejun.electron.flights.vo;

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

	private List<FlightInfo> flights;

	@Getter
	@Setter
	public static class FlightInfo {

		private String supplier;

		private List<FlightSegment> flightSegments;

		private List<FlightCabin> cabins;

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

		@Getter
		@Setter
		public static class FlightCabin {

			private List<String> code;

			private List<String> count;

			private String amount;

			private String cnTax;

			private String yqTax;

			private String yFareAmount;

			private String discount;

			private List<String> refundRule;

			private List<String> reissueRule;

			private List<String> baggage;

			private Map<String, Object> ext;

		}

	}


}
