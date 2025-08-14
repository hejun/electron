package io.github.hejun.electron.flights.supplier.impl.request.wnfx;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 蜗牛分销 响应 - 查询航班
 *
 * @author HeJun
 */
@Getter
@Setter
public class WnfxSearch {

	private List<String> moreWayFlightItemList;

	private Integer total;

	private String unifiedTip;

	private Integer code;

	private String message;

	private List<FlightInfo> flightInfos;

	@Getter
	@Setter
	public static class FlightInfo {

		private String yCabinCount;

		private String dptAirport;

		private String discount;

		private String minBookingParamKey;

		private String cabinType;

		private String bfBarePrice;

		private String pTripNote;

		private String flightNum;

		private String price;

		private String arrAirport;

		private String tag;

		private List<String> vendors;

		private String childCabin;

		private String childPriceType;

		private String bfTag;

		private String flightTimes;

		private List<String> stopInfoList;

		private String bfPrice;

		private String cabinCount;

		private String stopAirportFullName;

		private String tagProperty;

		private String flightTypeFullName;

		private String labels;

		private String limitRule;

		private String planetype;

		private Boolean stop;

		private String barePrice;

		private String domain;

		private String arrTerminal;

		private String stopAirportName;

		private String distance;

		private String minVppr;

		private String flightQuotePrices;

		private String cabin;

		private String tof;

		private String dptTime;

		private String stopAirportCode;

		private String arf;

		private String stopCityCode;

		private String stopsNum;

		private Boolean codeShare;

		private String actFlightNum;

		private String arr;

		private Boolean pTrip;

		private String dpt;

		private List<NewStopInfo> newStopInfoList;

		private String stopCityName;

		private String tagName;

		private String limitType;

		private String dptTerminal;

		private Boolean meal;

		private String carrier;

		private String childPrice;

		private String arrTime;


		@Getter
		@Setter
		public static class NewStopInfo {

			private String stopAirportCode;

			private String arrDate;

			private String stopCityCode;

			private String dptDate;

			private String stopCityName;

			private String dptTime;

			private String arrTime;

			private String stopAirportName;

		}

	}

}
