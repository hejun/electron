package io.github.hejun.electron.flights.supplier.impl.request.ibePlus;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * IBE+ 请求
 */
@Getter
@Setter
@JacksonXmlRootElement(localName = "FareInterface")
public class FareInterfaceRequest {

	@JacksonXmlProperty(localName = "Input")
	private Input input;

	@Getter
	@Setter
	public static class Input {

		@JacksonXmlProperty(localName = "HeaderIn")
		private HeaderIn headerIn;

		@JacksonXmlProperty(localName = "Request")
		private Request request;

		@Getter
		@Setter
		public static class HeaderIn {

			@JacksonXmlProperty(localName = "sysCode")
			private String sysCode;

			@JacksonXmlProperty(localName = "channelID")
			private String channelID;

			@JacksonXmlProperty(localName = "channelType")
			private String channelType;

			@JacksonXmlProperty(localName = "Agency")
			private Agency agency;

			@JacksonXmlProperty(localName = "language")
			private String language;

			@JacksonXmlProperty(localName = "commandType")
			private String commandType;

			@Getter
			@Setter
			public static class Agency {

				@JacksonXmlProperty(localName = "officeId")
				private String officeId;

				@JacksonXmlProperty(localName = "pid")
				private String pid;

				@JacksonXmlProperty(localName = "city")
				private String city;

			}

		}

		@Getter
		@Setter
		public static class Request {

			@JacksonXmlProperty(localName = "FlightShopRequest")
			public FlightShopRequest flightShopRequest;

			@Getter
			@Setter
			public static class FlightShopRequest {

				@JacksonXmlProperty(localName = "OriginDestinationInfo")
				@JacksonXmlElementWrapper(useWrapping = false)
				private List<OriginDestinationInfo> originDestinationInfo;

				@JacksonXmlProperty(localName = "TravelPreferences")
				private TravelPreferences travelPreferences;

				@JacksonXmlProperty(localName = "Option")
				private Option option;

				@Getter
				@Setter
				public static class OriginDestinationInfo {

					@JacksonXmlProperty(localName = "ori")
					private String ori;

					@JacksonXmlProperty(localName = "des")
					private String des;

					@JacksonXmlProperty(localName = "DepartureDate")
					private String departureDate;

				}

				@Getter
				@Setter
				public static class TravelPreferences {

					@JacksonXmlProperty(localName = "currCode")
					private Object currCode;

					@JacksonXmlProperty(localName = "displayCurrCode")
					private String displayCurrCode;

					@JacksonXmlProperty(localName = "isDirectFlightOnly")
					private Boolean isDirectFlightOnly;

					@JacksonXmlProperty(localName = "journeyType")
					private String journeyType;

					@JacksonXmlProperty(localName = "passenger")
					private Passenger passenger;


					@Getter
					@Setter
					public static class Passenger {

						private Integer number;

						private String type;

					}

				}

				@Getter
				@Setter
				public static class Option {

					@JacksonXmlProperty(localName = "isCityOrAirport")
					private String isCityOrAirport;

					@JacksonXmlProperty(localName = "ruleTypeNeeded")
					private String ruleTypeNeeded;

					@JacksonXmlProperty(localName = "lowestOrAll")
					private String lowestOrAll;

					@JacksonXmlProperty(localName = "isRefundReissueRuleNeeded")
					private String isRefundReissueRuleNeeded;

					@JacksonXmlProperty(localName = "includeBaggage")
					private Boolean includeBaggage;

					@Getter
					@Setter
					public static class JourneyTypeExt {

						private String type;

						private String calculation;

					}

				}

			}

		}

	}

}
