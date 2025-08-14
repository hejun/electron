package io.github.hejun.electron.flights.supplier.impl.request.wnfx;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 蜗牛分销 响应 - 价格
 *
 * @author HeJun
 */
@Getter
@Setter
public class WnfxPrice {

	private String date;

	private String com;

	private String msg;

	private String code;

	private String correct;

	private String distance;

	private String arrCode;

	private String tof;

	private String depCode;

	private Boolean zhiji;

	private String stopAirportCode;

	private String arf;

	private String stopCityCode;

	private String stopsNum;

	private Boolean codeShare;

	private String officeTicket;

	private String depTerminal;

	private String arrAirport;

	private List<Vendor> vendors;

	private String unionPriceResult;

	private List<String> stopInfoList;

	private String stopAirportFullName;

	private List<NewStopInfo> newStopInfoList;

	private String flightType;

	private String stopCityName;

	private String depAirport;

	private String eAirdrome;

	private String actCode;

	private Boolean meal;

	private String carrier;

	private String btime;

	private Boolean stop;

	private String etime;

	private String arrTerminal;

	private String bAirdrome;

	private String stopAirportName;

	private Boolean status;


	@Getter
	@Setter
	public static class Vendor {

		private String wrapperId;

		private String tgqShowData;

		private String businessExt;

		private String companyName;

		private String prtag;

		private String groupId;

		private String discount;

		private String cabin;

		private String vppr;

		private List<ReimbursementItem> reimbursementItemList;

		private String cabinType;

		private BusinessExtMap businessExtMap;

		private String extMap;

		private String luggage;

		private String price;

		private String expressInfo;

		private String pType;

		private String tagRule;

		private Boolean fuzzy;

		private String basePrice;

		private String afee;

		private String cardType;

		private String cabinCount;

		private String tagProperty;

		private String it;

		private String bprtag;

		private String tagName;

		private String lastTicketTime;

		private String limitType;

		private String limitRule;

		private String iata;

		private String policyId;

		private String barePrice;

		private String policyType;

		private String domain;

		private Boolean shareShowAct;

		@Getter
		@Setter
		public static class ReimbursementItem {

			private String code;

			private Boolean supportElectronic;

			private String text;

			private String reimburseType;

		}

		@Getter
		@Setter
		public static class BusinessExtMap {

			private String bookingParamKey;

			private String childPriceType;

			private String ptripDescControl;

			private String groupId;

			private String productMark;

			private String cardType;

			private String childSource;

			private String childPrice;

			private Boolean shareShowAct;

			private String planeType;

			private String childBuyAdult;

			private String childCabin;

		}

	}

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
