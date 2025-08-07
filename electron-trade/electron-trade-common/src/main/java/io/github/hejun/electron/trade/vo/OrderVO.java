package io.github.hejun.electron.trade.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 订单 VO
 *
 * @author HeJun
 */
@Getter
@Setter
@ToString
public class OrderVO {

	private String creator;

	private Date createDate;

}
