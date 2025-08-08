package io.github.hejun.electron.datacenter.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hejun.electron.datacenter.api.falback.ZoneApiFallbackFactory;
import io.github.hejun.electron.datacenter.vo.ZoneVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 消息服务Api
 *
 * @author HeJun
 */
@FeignClient(name = "electron-datacenter", contextId = "zone", fallbackFactory = ZoneApiFallbackFactory.class)
public interface ZoneApi {

	@GetMapping("/zone")
	IPage<ZoneVO> findPage(@RequestParam Integer pageNum, @RequestParam Integer pageSize);

}
