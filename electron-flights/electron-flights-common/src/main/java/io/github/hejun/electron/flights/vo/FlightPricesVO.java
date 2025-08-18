package io.github.hejun.electron.flights.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 价格查询
 *
 * @author HeJun
 */
@Getter
@Setter
public class FlightPricesVO {

	private Integer total;

	private List<FlightCabin> cabins;

	@Getter
	@Setter
	public static class FlightCabin {

		private String supplier;

		private List<String> code;

		private List<String> count;

		private Double sellPrice;

		private Double costPrice;

		private String airportTax;

		private String oilTax;

		private Double yFareAmount;

		private String discount;

		private List<String> refundRule;

		private List<String> reissueRule;

		private List<String> baggage;

		private Map<String, Object> ext;

	}

}
