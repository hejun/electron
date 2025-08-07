package io.github.hejun.electron.notifications.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.hejun.electron.notifications.dto.MessageDTO;
import io.github.hejun.electron.notifications.vo.MessageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * 消息 Controller
 *
 * @author HeJun
 */
@Slf4j
@RestController
@RequestMapping("/message")
public class MessageController {

	@GetMapping
	public IPage<MessageVO> findPage(@Valid @Min(value = 1, message = "页码不可小于1") @RequestParam(defaultValue = "1") Long pageNum,
									 @Valid @Max(value = 200, message = "每页数据不可大于200") @RequestParam(defaultValue = "15") Long pageSize,
									 Principal principal) {
		MessageVO vo = new MessageVO();
		vo.setCreator(principal.getName());
		vo.setCreateDate(new Date());

		Page<MessageVO> page = Page.of(pageNum, pageSize);
		page.setTotal(1L);
		page.setRecords(List.of(vo));
		return page;
	}

	@PostMapping
	public MessageVO sendMessage(@Valid @RequestBody MessageDTO messageDTO, Principal principal) {
		log.info("MessageDTO: {}, principal: {}", messageDTO, principal.getName());
		MessageVO vo = new MessageVO();
		vo.setCreator(principal.getName());
		vo.setCreateDate(new Date());
		return vo;
	}

}
