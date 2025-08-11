package io.github.hejun.electron.flights.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 * 供方账户
 *
 * @author HeJun
 */
@Getter
@Setter
@TableName(value = "t_supplier_account", autoResultMap = true)
public class SupplierAccount {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	private String code;

	private String name;

	private String account;

	private String secret;

	private String apiUrl;

	@TableField(typeHandler = JacksonTypeHandler.class)
	private Map<String, String> extParam;

	private Boolean enabled;

	private Long tenantId;

	private Date createDate;

	private Date lastModifiedDate;

}
