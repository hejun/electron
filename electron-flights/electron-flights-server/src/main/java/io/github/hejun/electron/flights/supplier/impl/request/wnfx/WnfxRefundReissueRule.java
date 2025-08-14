package io.github.hejun.electron.flights.supplier.impl.request.wnfx;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 蜗牛分销 响应 - 退改签规则
 *
 * @author HeJun
 */
@Getter
@Setter
public class WnfxRefundReissueRule {

	private String cTgqCabin;

	private String cTgqPercentText;

	private String signText;

	private Integer tgqFrom;

	private Boolean cCanCharge;

	private String cTgqProduct;

	private String tgqProduct;

	private Boolean canRefund;

	private List<TgqPointCharge> tgqPointCharges;

	private Boolean hasTime;

	private String returnText;

	private String changeText;

	private Map<String, Object> extMap;

	private String childTgqText;

	private String cSignText;

	private BigDecimal cBasePrice;

	private Boolean canCharge;

	private List<TgqPointCharge> cTgqPointCharges;

	private Integer cViewType;

	private String tgqText;

	private BigDecimal basePrice;

	private String changeRule;

	private String cChangeRule;

	private String tgqPercentText;

	private Map<String, Object> cExtMap;

	private String tgqCabin;

	private String cChangeText;

	private String cHasTime;

	private Boolean cAllowChange;

	private String cReturnText;

	private Boolean airlineTgq;

	private Integer viewType;

	private Boolean cCanRefund;

	private Integer cTgqFrom;

	private String cReturnRule;

	private Boolean cAirlineTgq;

	private String returnRule;

	private Boolean allowChange;

	@Getter
	@Setter
	public static class TgqPointCharge {

		private Integer returnFee;

		private String timeText;

		private Long time;

		private BigDecimal changeFee;

	}

}
