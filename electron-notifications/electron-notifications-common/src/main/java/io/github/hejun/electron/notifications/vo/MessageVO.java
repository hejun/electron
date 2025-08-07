package io.github.hejun.electron.notifications.vo;

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

	private Date createDate;

}
