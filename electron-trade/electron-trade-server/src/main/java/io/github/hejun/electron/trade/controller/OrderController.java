package io.github.hejun.electron.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hejun.electron.datacenter.api.ZoneApi;
import io.github.hejun.electron.datacenter.vo.ZoneVO;
import io.github.hejun.electron.notifications.api.MessageApi;
import io.github.hejun.electron.notifications.dto.MessageDTO;
import io.github.hejun.electron.notifications.vo.MessageVO;
import io.github.hejun.electron.trade.dto.OrderDTO;
import io.github.hejun.electron.trade.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Date;

/**
 * 订单 Controller
 *
 * @author HeJun
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderController {

	private final MessageApi messageApi;
	private final ZoneApi zoneApi;

	@PostMapping
	public OrderVO create(@Valid @RequestBody OrderDTO orderDTO, Principal principal) {
		log.info("OrderDTO: {}, principal: {}", orderDTO, principal.getName());
		OrderVO vo = new OrderVO();
		vo.setCreator(principal.getName());
		vo.setCreateDate(new Date());

		MessageVO messageVO = messageApi.sendMessage(new MessageDTO());
		log.info("Notifications send message result: {}", messageVO);

		IPage<ZoneVO> page = zoneApi.findPage(null, 50);
		log.info("Datacenter zone findPage: {}", page);
		return vo;
	}

}
