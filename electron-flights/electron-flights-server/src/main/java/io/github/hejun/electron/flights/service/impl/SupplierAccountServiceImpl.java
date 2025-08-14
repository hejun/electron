package io.github.hejun.electron.flights.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.mapper.SupplierAccountMapper;
import io.github.hejun.electron.flights.service.ISupplierAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供方账户 Service
 *
 * @author HeJun
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SupplierAccountServiceImpl implements ISupplierAccountService {

	private final SupplierAccountMapper supplierAccountMapper;

	@Override
	public List<SupplierAccount> findApprovedAccounts() {
		JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		Long tenantId = authentication.getToken().getClaim("iss_id");
		if (log.isDebugEnabled()) {
			log.debug("当前查询租户: {}, 用户: {}", tenantId, authentication.getName());
		}
		return supplierAccountMapper
			.selectList(Wrappers.<SupplierAccount>lambdaQuery()
				.eq(SupplierAccount::getTenantId, tenantId)
				.eq(SupplierAccount::getEnabled, true)
			);
	}

}
