package io.github.hejun.electron.flights.supplier.impl.request.wnfx;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 蜗牛分销 响应 - 行李额
 *
 * @author HeJun
 */
@Getter
@Setter
public class WnfxBaggageRule {

	private String request;

	private String briefWeightInfo;

	private MatchInfo matchInfo;

	private String infantBaggageRule;

	private String baggageOtherDesc;

	private String checkedBaggageRule;

	private String excessBaggageRule;

	private RuleBaseInfo ruleBaseInfo;

	private String cardBaggageRule;

	private String baggageOtherOutDesc;

	private String stretcherBaggageRule;

	private String cabinBaggageRule;

	private List<String> specialRules;

	private String smsContent;

	private String weightInfo;

	private String rowKey;

	@Getter
	@Setter
	public static class MatchInfo {

		private Boolean airlineRouteLimit;

	}

	@Getter
	@Setter
	public static class RuleBaseInfo {

		private String checkNum;

		private String handSize;

		private String checkSize;

		private Boolean sharedNum;

		private Boolean check;

		private String checkWeight;

		private String handWeight;

		private String handNum;

		private Boolean hand;

	}

}
