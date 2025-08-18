package io.github.hejun.electron.flights.supplier.impl.request.ibePlus;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * IBE+ 响应
 *
 * @author HeJun
 */
@Getter
@Setter
@JacksonXmlRootElement(localName = "FareInterface")
public class FareInterfaceResponse {

	@JacksonXmlProperty(localName = "Output")
	private Output output;

	@Getter
	@Setter
	public static class Output {

		@JacksonXmlProperty(localName = "HeaderOut")
		private HeaderOut headerOut;

		@JacksonXmlProperty(localName = "Result")
		private Result result;

		@Getter
		@Setter
		public static class HeaderOut {

			@JacksonXmlProperty(localName = "sessionId")
			private String sessionId;

		}

		@Getter
		@Setter
		public static class Result {

			@JacksonXmlProperty(localName = "FlightShopResult")
			private FlightShopResult flightShopResult;

			@JacksonXmlProperty(localName = "Info")
			private Info info;

			@Getter
			@Setter
			public static class FlightShopResult {

				@JacksonXmlProperty(localName = "AvJourneys")
				private AvJourneys avJourneys;

				@JacksonXmlProperty(localName = "PSn")
				@JacksonXmlElementWrapper(localName = "PSn")
				private List<PS> pSn;

				@JacksonXmlProperty(localName = "PsAvBinds")
				@JacksonXmlElementWrapper(localName = "PsAvBinds")
				private List<PsAvBind> psAvBinds;

				@JacksonXmlProperty(localName = "FsRefundRuleDisplay")
				@JacksonXmlElementWrapper(localName = "FsRefundRuleDisplay")
				private List<FSRefundDetailDisplay> fsRefundRuleDisplays;

				@JacksonXmlProperty(localName = "FsReissueRuleDisplay")
				@JacksonXmlElementWrapper(localName = "FsReissueRuleDisplay")
				private List<FSReissueDetailDisplay> fsReissueRuleDisplay;

				@JacksonXmlProperty(localName = "NFares")
				@JacksonXmlElementWrapper(localName = "NFares")
				private List<NFare> nFares;

				@JacksonXmlProperty(localName = "PFares")
				@JacksonXmlElementWrapper(localName = "PFares")
				private List<PFare> pFares;

				@JacksonXmlProperty(localName = "FbrDtls")
				@JacksonXmlElementWrapper(localName = "FbrDtls")
				private List<FbrDtl> fbrDtls;

				@JacksonXmlProperty(localName = "BrandInfos")
				@JacksonXmlElementWrapper(localName = "BrandInfos")
				private List<BrandInfo> brandInfos;

				@JacksonXmlProperty(localName = "ShopWarning")
				private ShopWarning shopWarning;

				@JacksonXmlProperty(localName = "DshopVersion")
				private String dShopVersion;

				@Getter
				@Setter
				public static class AvJourneys {

					@JacksonXmlProperty(localName = "RPH")
					private String RPH;

					@JacksonXmlProperty(localName = "office")
					private String office;

					@JacksonXmlProperty(localName = "AvJourney")
					@JacksonXmlElementWrapper(localName = "AvJourney", useWrapping = false)
					private List<AvJourney> avJourney;

					@Getter
					@Setter
					public static class AvJourney {

						@JacksonXmlProperty(localName = "RPH")
						private String RPH;

						@JacksonXmlProperty(localName = "dt")
						private String dt;

						@JacksonXmlProperty(localName = "week")
						private String week;

						@JacksonXmlProperty(localName = "dep")
						private String dep;

						@JacksonXmlProperty(localName = "arr")
						private String arr;

						@JacksonXmlProperty(localName = "AvOpt")
						@JacksonXmlElementWrapper(localName = "AvOpt", useWrapping = false)
						private List<AvOpt> avOpts;

						@Getter
						@Setter
						public static class AvOpt {

							@JacksonXmlProperty(localName = "RPH")
							private String RPH;

							@JacksonXmlProperty(localName = "Flt")
							private Flt flt;

							@Getter
							@Setter
							public static class Flt {

								@JacksonXmlProperty(localName = "RPH")
								private String RPH;

								@JacksonXmlProperty(localName = "routno")
								private String routno;

								@JacksonXmlProperty(localName = "subid")
								private String subid;

								@JacksonXmlProperty(localName = "pgind")
								private String pgind;

								@JacksonXmlProperty(localName = "airline")
								private String airline;

								@JacksonXmlProperty(localName = "fltNo")
								private String fltNo;

								@JacksonXmlProperty(localName = "dep")
								private String dep;

								@JacksonXmlProperty(localName = "arr")
								private String arr;

								@JacksonXmlProperty(localName = "tpm")
								private String tpm;

								@JacksonXmlProperty(localName = "dt")
								private String dt;

								@JacksonXmlProperty(localName = "week")
								private String week;

								@JacksonXmlProperty(localName = "deptm")
								private String deptm;

								@JacksonXmlProperty(localName = "arrtm")
								private String arrtm;

								@JacksonXmlProperty(localName = "arrad")
								private String arrad;

								@JacksonXmlProperty(localName = "stop")
								private String stop;

								@JacksonXmlProperty(localName = "stopOvers")
								private StopOvers stopOvers;

								@JacksonXmlProperty(localName = "dev")
								private String dev;

								@JacksonXmlProperty(localName = "ASR")
								private String ASR;

								@JacksonXmlProperty(localName = "meal")
								private String meal;

								@JacksonXmlProperty(localName = "et")
								private String et;

								@JacksonXmlProperty(localName = "lnk")
								private String lnk;

								@JacksonXmlProperty(localName = "codeshare")
								private CodeShare codeshare;

								@JacksonXmlProperty(localName = "term")
								private Term term;

								@JacksonXmlProperty(localName = "class")
								@JacksonXmlElementWrapper(localName = "class", useWrapping = false)
								private List<Clazz> clazz;

								@Getter
								@Setter
								public static class StopOvers {

									@JacksonXmlProperty(localName = "stopOver")
									@JacksonXmlElementWrapper(localName = "stopOver", useWrapping = false)
									private List<String> stopOver;

									@JacksonXmlProperty(localName = "stopInfo")
									@JacksonXmlElementWrapper(localName = "stopInfo", useWrapping = false)
									private List<StopInfo> stopInfo;

									@Getter
									@Setter
									public static class StopInfo {

										@JacksonXmlProperty(localName = "stopCity")
										private String stopCity;

										@JacksonXmlProperty(localName = "stopTime")
										private String stopTime;

										@JacksonXmlProperty(localName = "arrTerm")
										private String arrTerm;

										@JacksonXmlProperty(localName = "depTerm")
										private String depTerm;

									}

								}

								@Getter
								@Setter
								public static class CodeShare {

									@JacksonXmlProperty(localName = "airline")
									private String airline;

									@JacksonXmlProperty(localName = "fltno")
									private String fltNo;

								}

								@Getter
								@Setter
								public static class Term {

									@JacksonXmlProperty(localName = "dep")
									private String dep;

									@JacksonXmlProperty(localName = "arr")
									private String arr;

								}

								@Getter
								@Setter
								public static class Clazz {

									@JacksonXmlProperty(localName = "name")
									private String name;

									@JacksonXmlProperty(localName = "av")
									private String av;

								}

							}

						}

					}

				}

				@Getter
				@Setter
				public static class PS {

					@JacksonXmlProperty(localName = "seq")
					private String seq;

					@JacksonXmlProperty(localName = "disAmt")
					private Double disAmt;

					@JacksonXmlProperty(localName = "disCurrCode")
					private String disCurrCode;

					@JacksonXmlProperty(localName = "Fc")
					private String fc;

					@JacksonXmlProperty(localName = "Tc")
					private String tc;

					@JacksonXmlProperty(localName = "EI")
					private String EI;

					@JacksonXmlProperty(localName = "RMK")
					private RMK rmk;

					@JacksonXmlProperty(localName = "zValue")
					private String zValue;

					@JacksonXmlProperty(localName = "itiType")
					private String itiType;

					@JacksonXmlProperty(localName = "fbc")
					private String fbc;

					@JacksonXmlProperty(localName = "Taxes")
					@JacksonXmlElementWrapper(localName = "Taxes")
					private List<Tax> taxes;

					@JacksonXmlProperty(localName = "Routs")
					@JacksonXmlElementWrapper(localName = "Routs")
					private List<Rout> routs;

					@JacksonXmlProperty(localName = "FCs")
					@JacksonXmlElementWrapper(localName = "FCs")
					private List<FC> fCs;

					@JacksonXmlProperty(localName = "offices")
					private Offices offices;

					@JacksonXmlProperty(localName = "refundRuleIndicator")
					private String refundRuleIndicator;

					@JacksonXmlProperty(localName = "reissueRuleIndicator")
					private String reissueRuleIndicator;

					@JacksonXmlProperty(localName = "passengerType")
					private String passengerType;

					@JacksonXmlProperty(localName = "CabinFares")
					@JacksonXmlElementWrapper(localName = "CabinFares")
					private List<CabinFare> cabinFares;

					@JacksonXmlProperty(localName = "odType")
					private String odType;

					@JacksonXmlProperty(localName = "rmkcms")
					private String rmkCms;

					@Getter
					@Setter
					public static class RMK {

						@JacksonXmlProperty(localName = "OT")
						private String ot;

					}

					@Getter
					@Setter
					public static class Tax {

						@JacksonXmlProperty(localName = "code")
						private String code;

						@JacksonXmlProperty(localName = "amt")
						private String amt;

						@JacksonXmlProperty(localName = "currCode")
						private String currCode;

						@JacksonXmlProperty(localName = "taxComponent")
						private String taxComponent;

					}

					@Getter
					@Setter
					public static class Rout {

						@JacksonXmlProperty(localName = "carr")
						private String carr;

						@JacksonXmlProperty(localName = "fltNo")
						private String fltNo;

						@JacksonXmlProperty(localName = "bkClass")
						private String bkClass;

						@JacksonXmlProperty(localName = "cabin")
						private String cabin;

						@JacksonXmlProperty(localName = "cabinSequence")
						private String cabinSequence;

						@JacksonXmlProperty(localName = "OI")
						private String OI;

						@JacksonXmlProperty(localName = "departureDate")
						private String departureDate;

						@JacksonXmlProperty(localName = "departureTime")
						private String departureTime;

						@JacksonXmlProperty(localName = "arrivalDate")
						private String arrivalDate;

						@JacksonXmlProperty(localName = "arrivalTime")
						private String arrivalTime;

						@JacksonXmlProperty(localName = "departureAirport")
						private String departureAirport;

						@JacksonXmlProperty(localName = "arrivalAirport")
						private String arrivalAirport;

					}

					@Getter
					@Setter
					public static class FC {

						@JacksonXmlProperty(localName = "disAmt")
						private String disAmt;

						@JacksonXmlProperty(localName = "oriAmt")
						private String oriAmt;

						@JacksonXmlProperty(localName = "fareBasis")
						private String fareBasis;

						@JacksonXmlProperty(localName = "disCurrCode")
						private String disCurrCode;

						@JacksonXmlProperty(localName = "SecInfo")
						private SecInfo secInfo;

						@JacksonXmlProperty(localName = "FareBind")
						private FareBind fareBind;

						@JacksonXmlProperty(localName = "RefundDetailRph")
						@JacksonXmlElementWrapper(localName = "RefundDetailRph", useWrapping = false)
						private List<String> refundDetailRph;

						@JacksonXmlProperty(localName = "ReissueDetailRph")
						@JacksonXmlElementWrapper(localName = "ReissueDetailRph", useWrapping = false)
						private List<String> reissueDetailRph;

						@JacksonXmlProperty(localName = "YFares")
						private YFares yFares;

						@JacksonXmlProperty(localName = "endorsement")
						private String endorsement;

						@JacksonXmlProperty(localName = "ori")
						private String ori;

						@JacksonXmlProperty(localName = "des")
						private String des;

						@Getter
						@Setter
						public static class SecInfo {

							@JacksonXmlProperty(localName = "secNo")
							private String secNo;

							@JacksonXmlProperty(localName = "Baggage")
							private String baggage;

							@JacksonXmlProperty(localName = "A")
							private String a;

							@JacksonXmlProperty(localName = "B")
							private String b;

						}

						@Getter
						@Setter
						public static class FareBind {

							@JacksonXmlProperty(localName = "sysType")
							private String sysType;

							@JacksonXmlProperty(localName = "fareRPH")
							private String fareRPH;

							@JacksonXmlProperty(localName = "fbrRuleRPH")
							private String fbrRuleRPH;

							@JacksonXmlProperty(localName = "fbrDtlRPH")
							private String fbrDtlRPH;

						}

						@Getter
						@Setter
						public static class YFares {

							@JacksonXmlProperty(localName = "yFareAmount")
							private Double yFareAmount;

						}

					}

					@Getter
					@Setter
					public static class Offices {

						@JacksonXmlProperty(localName = "office")
						private String office;

					}

					@Getter
					@Setter
					public static class CabinFare {

						@JacksonXmlProperty(localName = "cabin")
						private String cabin;

						@JacksonXmlProperty(localName = "amount")
						private String amount;

						@JacksonXmlProperty(localName = "subCabinName")
						private String subCabinName;

					}

				}

				@Getter
				@Setter
				public static class PsAvBind {

					@JacksonXmlProperty(localName = "seq")
					private String seq;

					@JacksonXmlProperty(localName = "avRPH")
					@JacksonXmlElementWrapper(localName = "avRPH", useWrapping = false)
					private List<String> avRPH;

					@JacksonXmlProperty(localName = "bkClass")
					@JacksonXmlElementWrapper(localName = "bkClass", useWrapping = false)
					private List<String> bkClass;

				}

				@Getter
				@Setter
				public static class FSRefundDetailDisplay {

					@JacksonXmlProperty(localName = "RefundDetailRph")
					private String refundDetailRph;

					@JacksonXmlProperty(localName = "DepartureTimeType")
					private String departureTimeType;

					@JacksonXmlProperty(localName = "FirstDepartureTime")
					private Integer firstDepartureTime;

					@JacksonXmlProperty(localName = "LastDepartureTime")
					private Integer lastDepartureTime;

					@JacksonXmlProperty(localName = "FirstDepartureTimeUnit")
					private String firstDepartureTimeUnit;

					@JacksonXmlProperty(localName = "DepartureTimeUnit")
					private String departureTimeUnit;

					@JacksonXmlProperty(localName = "RefundPercent")
					private Integer refundPercent;

					@JacksonXmlProperty(localName = "RefundDiscription")
					private String refundDiscription;

					@JacksonXmlProperty(localName = "PassengerType")
					private String passengerType;

					@JacksonXmlProperty(localName = "JourneyType")
					private String journeyType;

					@JacksonXmlProperty(localName = "TicketUseType")
					private String ticketUseType;

					@JacksonXmlProperty(localName = "RefundAllowedTag")
					private String refundAllowedTag;

					@JacksonXmlProperty(localName = "FixedAmount")
					private String fixedAmount;

					@JacksonXmlProperty(localName = "PassengerLevel")
					private String passengerLevel;

					@JacksonXmlProperty(localName = "LowerValue")
					private String lowerValue;

					@JacksonXmlProperty(localName = "LowerRelate")
					private String lowerRelate;

					@JacksonXmlProperty(localName = "UpperValue")
					private String upperValue;

					@JacksonXmlProperty(localName = "UpperRelate")
					private String upperRelate;


				}

				@Getter
				@Setter
				public static class FSReissueDetailDisplay {

					@JacksonXmlProperty(localName = "ReissueDetailRph")
					private String reissueDetailRph;

					@JacksonXmlProperty(localName = "DepartureTimeType")
					private String departureTimeType;

					@JacksonXmlProperty(localName = "FirstDepartureTime")
					private Integer firstDepartureTime;

					@JacksonXmlProperty(localName = "LastDepartureTime")
					private Integer lastDepartureTime;

					@JacksonXmlProperty(localName = "FirstDepartureTimeUnit")
					private String firstDepartureTimeUnit;

					@JacksonXmlProperty(localName = "DepartureTimeUnit")
					private String departureTimeUnit;

					@JacksonXmlProperty(localName = "ReissuePercent")
					private Integer reissuePercent;

					@JacksonXmlProperty(localName = "ReissueDiscription")
					private String reissueDiscription;

					@JacksonXmlProperty(localName = "freeChangeTimes")
					private String freeChangeTimes;

					@JacksonXmlProperty(localName = "PassengerType")
					private String passengerType;

					@JacksonXmlProperty(localName = "JourneyType")
					private String journeyType;

					@JacksonXmlProperty(localName = "TicketUsedTag")
					private String ticketUsedTag;

					@JacksonXmlProperty(localName = "PassengerLevel")
					private String passengerLevel;

					@JacksonXmlProperty(localName = "LowerValue")
					private String lowerValue;

					@JacksonXmlProperty(localName = "LowerRelate")
					private String lowerRelate;

					@JacksonXmlProperty(localName = "UpperValue")
					private String upperValue;

					@JacksonXmlProperty(localName = "UpperRelate")
					private String upperRelate;


				}

				@Getter
				@Setter
				public static class NFare {

					@JacksonXmlProperty(localName = "RPH")
					private String RPH;

					@JacksonXmlProperty(localName = "ruleRPH")
					private String ruleRPH;

					@JacksonXmlProperty(localName = "carr")
					private String carr;

					@JacksonXmlProperty(localName = "ori")
					private String ori;

					@JacksonXmlProperty(localName = "des")
					private String des;

					@JacksonXmlProperty(localName = "amt")
					private String amt;

					@JacksonXmlProperty(localName = "fbc")
					private String fbc;

					@JacksonXmlProperty(localName = "bkClass")
					private String bkClass;

					@JacksonXmlProperty(localName = "cbClass")
					private String cbClass;

					@JacksonXmlProperty(localName = "currCode")
					private String currCode;

					@JacksonXmlProperty(localName = "tourCode")
					private String tourCode;

					@JacksonXmlProperty(localName = "EI")
					private String EI;

					@JacksonXmlProperty(localName = "jourType")
					private String jourType;

					@JacksonXmlProperty(localName = "agNo")
					private String agNo;

					@JacksonXmlProperty(localName = "ob")
					private String ob;

					@JacksonXmlProperty(localName = "ib")
					private String ib;

					@JacksonXmlProperty(localName = "minStay")
					private String minStay;

					@JacksonXmlProperty(localName = "minStayUnit")
					private String minStayUnit;

					@JacksonXmlProperty(localName = "maxStay")
					private String maxStay;

					@JacksonXmlProperty(localName = "maxStayUnit")
					private String maxStayUnit;

				}

				@Getter
				@Setter
				public static class PFare {

					@JacksonXmlProperty(localName = "RPH")
					private String RPH;

					@JacksonXmlProperty(localName = "ruleRPH")
					private String ruleRPH;

					@JacksonXmlProperty(localName = "carr")
					private String carr;

					@JacksonXmlProperty(localName = "ori")
					private String ori;

					@JacksonXmlProperty(localName = "des")
					private String des;

					@JacksonXmlProperty(localName = "amt")
					private String amt;

					@JacksonXmlProperty(localName = "fbc")
					private String fbc;

					@JacksonXmlProperty(localName = "bkClass")
					private String bkClass;

					@JacksonXmlProperty(localName = "cbClass")
					private String cbClass;

					@JacksonXmlProperty(localName = "currCode")
					private String currCode;

					@JacksonXmlProperty(localName = "ruleNo")
					private String ruleNo;

				}

				@Getter
				@Setter
				public static class FbrDtl {

					@JacksonXmlProperty(localName = "RPH")
					private String RPH;

					@JacksonXmlProperty(localName = "fbrId")
					private String fbrId;

					@JacksonXmlProperty(localName = "calculateType")
					private String calculateType;

				}

				@Getter
				@Setter
				public static class BrandInfo {
				}

				@Getter
				@Setter
				public static class ShopWarning {

					@JacksonXmlProperty(localName = "code")
					private String code;

					@JacksonXmlProperty(localName = "message")
					private String message;

					@JacksonXmlProperty(localName = "uuid")
					private String uuid;

					@JacksonXmlProperty(localName = "tid")
					private String tid;

				}

			}

			@Getter
			@Setter
			public static class Info {

				@JacksonXmlProperty(localName = "simInfo")
				private String simInfo;

				@JacksonXmlProperty(localName = "debugInfo")
				private String debugInfo;

				@JacksonXmlProperty(localName = "dffVintage")
				private String dffVintage;


			}

		}

	}

}
