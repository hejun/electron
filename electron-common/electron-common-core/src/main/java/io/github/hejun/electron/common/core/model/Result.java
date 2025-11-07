package io.github.hejun.electron.common.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;

/**
 * 返回结果封装类
 *
 * @author HeJun
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Result<T> implements Serializable {

	private Integer code;
	private String msg;
	private T data;

	/**
	 * 成功
	 *
	 * @param data 返回数据
	 * @return Result封装类
	 */
	public static <T> Result<T> SUCCESS(T data) {
		Result<T> result = new Result<>();
		result.code = 200;
		result.msg = "成功";
		result.data = data;
		return result;
	}

	/**
	 * 成功
	 *
	 * @return Result封装类
	 */
	public static <T> Result<T> SUCCESS() {
		return Result.SUCCESS(null);
	}

	/**
	 * 失败
	 *
	 * @param code 错误码
	 * @param msg  错误消息
	 * @param data 返回数据
	 * @return Result封装类
	 */
	public static <T> Result<T> ERROR(Integer code, String msg, T data) {
		Result<T> result = new Result<>();
		result.code = code;
		result.msg = msg;
		result.data = data;
		return result;
	}

	/**
	 * 失败
	 *
	 * @param code 错误码
	 * @param msg  错误消息
	 * @return Result封装类
	 */
	public static <T> Result<T> ERROR(Integer code, String msg) {
		return Result.ERROR(code, msg, null);
	}

}
