package io.github.hejun.electron.notifications.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hejun.electron.notifications.api.falback.MessageApiFallbackFactory;
import io.github.hejun.electron.notifications.dto.MessageDTO;
import io.github.hejun.electron.notifications.vo.MessageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 消息服务Api
 *
 * @author HeJun
 */
@FeignClient(name = "electron-notifications", contextId = "message", fallbackFactory = MessageApiFallbackFactory.class)
public interface MessageApi {

	@GetMapping("/message")
	IPage<MessageVO> findPage(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize);

	@PostMapping("/message")
	MessageVO sendMessage(@RequestBody MessageDTO messageDTO);

}
