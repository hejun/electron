package io.github.hejun.electron.notifications.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 消息 VO
 *
 * @author HeJun
 */
@Getter
@Setter
@ToString
public class MessageVO {

	private String creator;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private Date createDate;

}
