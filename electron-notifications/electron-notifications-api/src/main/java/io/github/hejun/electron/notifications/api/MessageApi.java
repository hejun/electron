package io.github.hejun.electron.notifications.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hejun.electron.notifications.api.falback.MessageApiFallbackFactory;
import io.github.hejun.electron.notifications.dto.MessageDTO;
import io.github.hejun.electron.notifications.vo.MessageVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
	IPage<MessageVO> findPage(@Valid @Min(value = 1, message = "页码不可小于1") @RequestParam(defaultValue = "1") Integer pageNum,
							  @Valid @Max(value = 200, message = "每页数据不可大于200") @RequestParam(defaultValue = "15") Integer pageSize);

	@PostMapping("/message")
	MessageVO sendMessage(@Valid @RequestBody MessageDTO messageDTO);

}
