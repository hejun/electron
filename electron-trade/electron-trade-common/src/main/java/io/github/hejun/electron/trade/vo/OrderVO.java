package io.github.hejun.electron.trade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date createDate;

}
