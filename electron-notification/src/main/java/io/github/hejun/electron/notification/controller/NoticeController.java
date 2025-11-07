package io.github.hejun.electron.notification.controller;

import io.github.hejun.electron.common.core.model.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * 通知 Controller
 *
 * @author HeJun
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

	@PostMapping
	public Result<String> notice(Principal principal) {
		return Result.SUCCESS("From: " + principal.getName());
	}

}
