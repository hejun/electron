package io.github.hejun.electron.datacenter.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hejun.electron.datacenter.api.falback.ZoneApiFallbackFactory;
import io.github.hejun.electron.datacenter.vo.ZoneVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
	IPage<ZoneVO> findPage(@Valid @Min(value = 1, message = "页码不可小于1") @RequestParam(defaultValue = "1") Integer pageNum,
						   @Valid @Max(value = 200, message = "每页数据不可大于200") @RequestParam(defaultValue = "15") Integer pageSize);

}
