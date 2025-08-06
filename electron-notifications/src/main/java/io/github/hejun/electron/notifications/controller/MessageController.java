package io.github.hejun.electron.notifications.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

/**
 * 消息 Controller
 *
 * @author HeJun
 */
@RestController
@RequestMapping("/message")
public class MessageController {

	@GetMapping
	public Map<String, Object> findPage(@RequestParam(defaultValue = "1") Long pageNum,
										@RequestParam(defaultValue = "15") Long pageSize,
										Principal principal) {
		return Map.of(
			"pageSize", pageSize,
			"pageNum", pageNum,
			"authentication", principal.getName()
		);
	}

}
