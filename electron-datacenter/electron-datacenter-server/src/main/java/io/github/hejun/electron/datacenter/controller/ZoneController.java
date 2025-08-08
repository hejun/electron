package io.github.hejun.electron.datacenter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.hejun.electron.datacenter.vo.ZoneVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * 消息 Controller
 *
 * @author HeJun
 */
@Slf4j
@RestController
@RequestMapping("/zone")
public class ZoneController {

	@GetMapping
	public IPage<ZoneVO> findPage(@Valid @Min(value = 1, message = "页码不可小于1") @RequestParam(defaultValue = "1") Long pageNum,
								  @Valid @Max(value = 200, message = "每页数据不可大于200") @RequestParam(defaultValue = "15") Long pageSize,
								  Principal principal) {
		ZoneVO vo = new ZoneVO();
		vo.setCode("000000");
		vo.setValue("区划" + principal.getName());

		Page<ZoneVO> page = Page.of(pageNum, pageSize);
		page.setTotal(1L);
		page.setRecords(List.of(vo));
		return page;
	}

}
