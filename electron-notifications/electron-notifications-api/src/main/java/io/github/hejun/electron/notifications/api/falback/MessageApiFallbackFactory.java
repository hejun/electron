package io.github.hejun.electron.notifications.api.falback;

import com.baomidou.mybatisplus.core.metadata.IPage;
import feign.FeignException;
import io.github.hejun.electron.notifications.api.MessageApi;
import io.github.hejun.electron.notifications.dto.MessageDTO;
import io.github.hejun.electron.notifications.vo.MessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 消息Feign回滚
 *
 * @author HeJun
 */
@Slf4j
@Component
public class MessageApiFallbackFactory implements FallbackFactory<MessageApi> {

	@Override
	public MessageApi create(Throwable cause) {
		return new MessageApi() {

			@Override
			public IPage<MessageVO> findPage(Integer pageNum, Integer pageSize) {
				log.error("MessageApi findPage Error: {}", cause.getMessage());
				if (cause instanceof FeignException e) {
					throw e;
				}
				throw new RuntimeException(cause);
			}

			@Override
			public MessageVO sendMessage(MessageDTO messageDTO) {
				log.error("MessageApi sendMessage Error: {}", cause.getMessage());
				if (cause instanceof FeignException e) {
					throw e;
				}
				throw new RuntimeException(cause);
			}

		};
	}

}
