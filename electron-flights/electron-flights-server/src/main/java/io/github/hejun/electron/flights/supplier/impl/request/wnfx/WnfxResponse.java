package io.github.hejun.electron.flights.supplier.impl.request.wnfx;

import lombok.Getter;
import lombok.Setter;

/**
 * 蜗牛分销 响应
 *
 * @author HeJun
 */
@Getter
@Setter
public class WnfxResponse<T> {

	private Integer code;

	private String message;

	private Long createTime;

	private String realMsg;

	private T result;

}
