package io.github.hejun.electron.flights.supplier.impl;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.hejun.electron.flights.constant.Constants;
import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.supplier.ISupplierSupport;
import io.github.hejun.electron.flights.supplier.impl.request.wnfx.*;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * 蜗牛分销 供方实现
 *
 * @author HeJun
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WnfxSupplierSupport implements ISupplierSupport {

	private final ObjectMapper objectMapper;
	private RestTemplate restTemplate = new RestTemplate();

	{
		if (log.isDebugEnabled()) {
			restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
			restTemplate.getInterceptors().add((request, body, execution) -> {
				log.debug("WNFX Request: {}", new String(body, StandardCharsets.UTF_8));
				ClientHttpResponse response = execution.execute(request, body);
				log.debug("WNFX Response: {}", StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
				return response;
			});
		}
	}

	@Override
	public FlightsSearchVO search(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		if (flightsSearchDTO.getSegments().size() == 1) {
			return this.searchSingle(supplierAccount, flightsSearchDTO);
		} else {
			if (flightsSearchDTO.getSegments().size() == 2) {
				String firstSegment = flightsSearchDTO.getSegments().getFirst().getDepartureCityCode() + flightsSearchDTO.getSegments().getFirst().getArrivalCityCode();
				String lastSegment = flightsSearchDTO.getSegments().getFirst().getArrivalCityCode() + flightsSearchDTO.getSegments().getFirst().getDepartureCityCode();
				if (Objects.equals(firstSegment, lastSegment)) {
					return this.searchRound(supplierAccount, flightsSearchDTO);
				}
			}
			return this.searchMulti(supplierAccount, flightsSearchDTO);
		}
	}

	private FlightsSearchVO searchSingle(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		ObjectNode params = objectMapper.createObjectNode();
		params.put("dpt", flightsSearchDTO.getSegments().getFirst().getDepartureCityCode());
		params.put("arr", flightsSearchDTO.getSegments().getFirst().getArrivalCityCode());
		params.put("date", DateFormatUtils.format(flightsSearchDTO.getSegments().getFirst().getDepartureDate(), "yyyy-MM-dd"));
		if (supplierAccount.getExtParam() != null) {
			params.put("ex_track", supplierAccount.getExtParam().getOrDefault("ex_track", "chailv"));
		} else {
			params.put("ex_track", "chailv");
		}

		WnfxResponse<WnfxSearch> flightResp = this.request(supplierAccount, "flight.national.supply.sl.searchflight", params, new ParameterizedTypeReference<>() {
		});

		List<CompletableFuture<WnfxResponse<WnfxPrice>>> priceFutures = new ArrayList<>(flightResp.getResult().getTotal());
		List<CompletableFuture<Triple<String, String, Pair<String, String>>>> refundReissueRuleFutures = new ArrayList<>(flightResp.getResult().getTotal() * 4);
		List<CompletableFuture<Triple<String, String, String>>> baggageRuleFutures = new ArrayList<>(flightResp.getResult().getTotal() * 4);

		Map<String, Set<String>> repeatBaggageRuleCheckMap = new ConcurrentHashMap<>(flightResp.getResult().getTotal() * 4);
		for (WnfxSearch.FlightInfo flightInfo : flightResp.getResult().getFlightInfos()) {
			CompletableFuture<WnfxResponse<WnfxPrice>> priceFuture = CompletableFuture.supplyAsync(() -> {
				params.put("flightNum", flightInfo.getFlightNum());
				return this.request(supplierAccount, "flight.national.supply.sl.searchprice", params, new ParameterizedTypeReference<>() {
				});
			});
			priceFutures.add(priceFuture);
			priceFuture.thenApplyAsync(resp -> {
				WnfxPrice wnfxPrice = resp.getResult();
				for (WnfxPrice.Vendor vendor : wnfxPrice.getVendors()) {
					CompletableFuture<Triple<String, String, Pair<String, String>>> repeatRefundReissueRuleFuture = CompletableFuture.supplyAsync(() -> {
						ObjectNode refundReissueRuleParams = objectMapper.createObjectNode();
						refundReissueRuleParams.put("flightNum", wnfxPrice.getCode());
						refundReissueRuleParams.put("cabin", vendor.getCabin());
						refundReissueRuleParams.put("dep", wnfxPrice.getDepCode());
						refundReissueRuleParams.put("arr", wnfxPrice.getArrCode());
						refundReissueRuleParams.put("dptDate", wnfxPrice.getDate());
						refundReissueRuleParams.put("dptTime", wnfxPrice.getBtime());
						refundReissueRuleParams.put("policyId", vendor.getPolicyId());
						refundReissueRuleParams.put("maxSellPrice", vendor.getBarePrice());
						refundReissueRuleParams.put("minSellPrice", vendor.getBarePrice());
						refundReissueRuleParams.put("printPrice", vendor.getVppr());
						refundReissueRuleParams.put("tagName", vendor.getPrtag());
						refundReissueRuleParams.put("translate", false);
						refundReissueRuleParams.put("sfid", vendor.getGroupId());
						refundReissueRuleParams.put("needPercentTgqText", false);
						refundReissueRuleParams.put("businessExt", vendor.getBusinessExt());
						refundReissueRuleParams.put("client", vendor.getDomain());
						if (vendor.getBusinessExtMap() != null) {
							refundReissueRuleParams.put("childCabin", vendor.getBusinessExtMap().getChildCabin());
							refundReissueRuleParams.put("childSellPrice", vendor.getBusinessExtMap().getChildPrice());
						}
						WnfxResponse<WnfxRefundReissueRule> refundReissueRuleResp = this.request(supplierAccount, "flight.national.supply.sl.tgqNew", refundReissueRuleParams, new ParameterizedTypeReference<>() {
						});
						String returnRule = refundReissueRuleResp.getResult().getReturnRule();
						String changeRule = refundReissueRuleResp.getResult().getChangeRule();
						return Triple.of(wnfxPrice.getCode(), vendor.getPolicyId(), Pair.of(returnRule, changeRule));
					});
					refundReissueRuleFutures.add(repeatRefundReissueRuleFuture);
				}
				return null;
			});
			priceFuture.thenAcceptAsync(resp -> {
				WnfxPrice wnfxPrice = resp.getResult();
				for (WnfxPrice.Vendor vendor : wnfxPrice.getVendors()) {
					CompletableFuture<Triple<String, String, String>> baggageRuleFuture = CompletableFuture.supplyAsync(() -> {
						String airlineCode;
						if (Boolean.TRUE.equals(vendor.getShareShowAct())) {
							airlineCode = wnfxPrice.getActCode().substring(0, 2);
						} else {
							airlineCode = wnfxPrice.getCarrier();
						}
						if (repeatBaggageRuleCheckMap.containsKey(airlineCode) && repeatBaggageRuleCheckMap.get(airlineCode).contains(vendor.getCabin())) {
							return null;
						}
						repeatBaggageRuleCheckMap.putIfAbsent(airlineCode, new ConcurrentHashSet<>());
						repeatBaggageRuleCheckMap.get(airlineCode).add(vendor.getCabin());

						ObjectNode baggageRuleParams = objectMapper.createObjectNode();
						baggageRuleParams.put("airlineCode", airlineCode);
						baggageRuleParams.put("cabin", vendor.getCabin());
						baggageRuleParams.put("depCode", wnfxPrice.getDepCode());
						baggageRuleParams.put("arrCode", wnfxPrice.getArrCode());
						baggageRuleParams.put("saleDate", wnfxPrice.getDate());
						baggageRuleParams.put("depDate", wnfxPrice.getDate());
						if (StringUtils.isNotBlank(vendor.getLuggage())) {
							baggageRuleParams.put("luggage", vendor.getLuggage());
						}
						WnfxResponse<WnfxBaggageRule> baggageRuleResp = this.request(supplierAccount, "flight.national.supply.sl.baggagerule", baggageRuleParams, new ParameterizedTypeReference<>() {
						});
						return Triple.of(airlineCode, vendor.getCabin(), baggageRuleResp.getResult().getRuleBaseInfo().getCheckWeight());
					});
					baggageRuleFutures.add(baggageRuleFuture);
				}
			});
		}

		CompletableFuture.allOf(priceFutures.toArray(CompletableFuture[]::new)).join();
		CompletableFuture.allOf(refundReissueRuleFutures.toArray(CompletableFuture[]::new)).join();
		CompletableFuture.allOf(baggageRuleFutures.toArray(CompletableFuture[]::new)).join();

		Map<String, WnfxPrice> priceMap = new HashMap<>(flightResp.getResult().getTotal());
		for (CompletableFuture<WnfxResponse<WnfxPrice>> future : priceFutures) {
			try {
				WnfxResponse<WnfxPrice> priceResp = future.get();
				priceMap.put(priceResp.getResult().getCode(), priceResp.getResult());
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		Map<String, Map<String, Pair<String, String>>> refundReissueRuleMap = new HashMap<>();
		for (CompletableFuture<Triple<String, String, Pair<String, String>>> future : refundReissueRuleFutures) {
			try {
				Triple<String, String, Pair<String, String>> triple = future.get();
				if (triple == null) {
					continue;
				}
				if (refundReissueRuleMap.containsKey(triple.getLeft())) {
					refundReissueRuleMap.get(triple.getLeft()).put(triple.getMiddle(), triple.getRight());
				} else {
					refundReissueRuleMap.put(triple.getLeft(), new HashMap<>(Map.of(triple.getMiddle(), triple.getRight())));
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		Map<String, Map<String, String>> baggageRuleMap = new HashMap<>();
		for (CompletableFuture<Triple<String, String, String>> future : baggageRuleFutures) {
			try {
				Triple<String, String, String> triple = future.get();
				if (triple == null) {
					continue;
				}
				if (baggageRuleMap.containsKey(triple.getLeft())) {
					baggageRuleMap.get(triple.getLeft()).put(triple.getMiddle(), triple.getRight());
				} else {
					baggageRuleMap.put(triple.getLeft(), new HashMap<>(Map.of(triple.getMiddle(), triple.getRight())));
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		return this.formatSingleSegmentResponse(flightsSearchDTO, flightResp, priceMap, refundReissueRuleMap, baggageRuleMap);
	}

	private FlightsSearchVO searchRound(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	private FlightsSearchVO searchMulti(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	private <T> WnfxResponse<T> request(SupplierAccount supplierAccount, String tag, ObjectNode params,
										ParameterizedTypeReference<WnfxResponse<T>> typeReference) {
		MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
		request.add("tag", tag);
		request.add("token", supplierAccount.getAccount());
		request.add("createTime", String.valueOf(System.currentTimeMillis()));
		try {
			request.add("params", objectMapper.writeValueAsString(params));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		String sortedParam = String.join("",
			"createTime=", Objects.requireNonNull(request.getFirst("createTime")),
			"key=", supplierAccount.getSecret(),
			"params=", Objects.requireNonNull(request.getFirst("params")),
			"tag=", tag,
			"token=", supplierAccount.getAccount()
		);
		request.add("sign", DigestUtils.md5Hex(sortedParam.getBytes(StandardCharsets.UTF_8)));

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setAcceptLanguage(Locale.LanguageRange.parse("zh-CN,zh;q=0.9,en;q=0.8"));
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		return restTemplate.exchange(supplierAccount.getApiUrl(), HttpMethod.POST, new HttpEntity<>(request, headers), typeReference).getBody();
	}

	private FlightsSearchVO formatSingleSegmentResponse(FlightsSearchDTO flightsSearchDTO, WnfxResponse<WnfxSearch> resp, Map<String, WnfxPrice> priceMap,
														Map<String, Map<String, Pair<String, String>>> refundReissueRuleMap,
														Map<String, Map<String, String>> baggageRuleMap) {
		List<FlightsSearchVO.FlightInfo> flights = new ArrayList<>();
		for (WnfxSearch.FlightInfo wnfxFlight : resp.getResult().getFlightInfos()) {
			if (!priceMap.containsKey(wnfxFlight.getFlightNum())) {
				continue;
			}

			FlightsSearchVO.FlightInfo.FlightSegment segment = new FlightsSearchVO.FlightInfo.FlightSegment();
			segment.setAirline(wnfxFlight.getCarrier());
			segment.setFlightNo(wnfxFlight.getFlightNum().substring(wnfxFlight.getCarrier().length()));

			segment.setDepartureAirlineCode(wnfxFlight.getDpt());
			segment.setDepartureDate(DateFormatUtils.format(flightsSearchDTO.getSegments().getFirst().getDepartureDate(), "yyyy-MM-dd"));
			segment.setDepartureTime(wnfxFlight.getDptTime());
			segment.setDepartureTerminalCode(StringUtils.defaultIfBlank(wnfxFlight.getDptTerminal(), null));

			segment.setArrivalAirlineCode(wnfxFlight.getArr());
			if (StringUtils.isNotBlank(wnfxFlight.getFlightTimes())) {
				String[] split = wnfxFlight.getFlightTimes().split("小时");
				if (NumberUtils.isParsable(split[0])) {
					Date departureDate;
					try {
						departureDate = DateUtils.parseDate(segment.getDepartureDate() + " " + segment.getDepartureTime(), "yyyy-MM-dd HH:mm");
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
					int hours = Integer.parseInt(split[0]);
					Date arrivalDate = DateUtils.addHours(departureDate, hours);
					if (split.length == 2) {
						int minutes = Integer.parseInt(split[1].replace("分钟", "").replace("分", ""));
						arrivalDate = DateUtils.addMinutes(arrivalDate, minutes);
					}
					segment.setArrivalDate(DateFormatUtils.format(arrivalDate, "yyyy-MM-dd"));
					segment.setDuration(Duration.ofMillis(arrivalDate.getTime() - departureDate.getTime()).toMinutes());
				}
			}
			segment.setArrivalTime(wnfxFlight.getArrTime());
			segment.setArrivalTerminalCode(StringUtils.defaultIfBlank(wnfxFlight.getArrTerminal(), null));

			if (Boolean.TRUE.equals(wnfxFlight.getCodeShare())) {
				String actFlightNum = wnfxFlight.getActFlightNum();
				FlightsSearchVO.FlightInfo.FlightSegment.ShareFlightInfo shareFlight = new FlightsSearchVO.FlightInfo.FlightSegment.ShareFlightInfo();
				shareFlight.setAirline(actFlightNum.substring(0, 2));
				shareFlight.setFlightNo(actFlightNum.substring(2));
				segment.setShareFlight(shareFlight);
			}

			segment.setAircraftType(wnfxFlight.getPlanetype());
			segment.setMileage(wnfxFlight.getDistance());

			if (Boolean.TRUE.equals(wnfxFlight.getStop())) {
				List<FlightsSearchVO.FlightInfo.FlightSegment.FlightStop> stops = new ArrayList<>();
				for (WnfxSearch.FlightInfo.NewStopInfo stopInfo : wnfxFlight.getNewStopInfoList()) {
					FlightsSearchVO.FlightInfo.FlightSegment.FlightStop stop = new FlightsSearchVO.FlightInfo.FlightSegment.FlightStop();
					stop.setStopAirlineCode(stopInfo.getStopAirportCode());
					if (stopInfo.getArrTime() != null && stopInfo.getDptTime() != null) {
						stop.setStopTime(stopInfo.getArrTime());
					}
					stops.add(stop);
				}
				segment.setStops(stops);
			}

			segment.setMeal(Boolean.TRUE.equals(wnfxFlight.getMeal()) ? "S" : null);

			WnfxPrice wnfxPrice = priceMap.get(wnfxFlight.getFlightNum());
			List<FlightsSearchVO.FlightInfo.FlightCabin> cabins = new ArrayList<>(wnfxPrice.getVendors().size());
			for (WnfxPrice.Vendor vendor : wnfxPrice.getVendors()) {
				FlightsSearchVO.FlightInfo.FlightCabin cabin = new FlightsSearchVO.FlightInfo.FlightCabin();
				cabin.setCode(List.of(vendor.getCabin()));
				cabin.setCount(List.of(vendor.getCabinCount()));
				cabin.setAmount(vendor.getVppr());
				cabin.setCnTax(wnfxPrice.getArf());
				cabin.setYqTax(wnfxPrice.getTof());
				cabin.setYFareAmount(StringUtils.isBlank(vendor.getBasePrice()) || "0".equals(vendor.getBasePrice()) ? null : vendor.getBasePrice());
				cabin.setDiscount(vendor.getDiscount());

				Pair<String, String> refundReissueRulePair = refundReissueRuleMap.getOrDefault(wnfxPrice.getCode(), Map.of()).get(vendor.getPolicyId());
				if (refundReissueRulePair != null) {
					cabin.setRefundRule(List.of(refundReissueRulePair.getLeft()));
					cabin.setReissueRule(List.of(refundReissueRulePair.getRight()));
				}

				String actAirline;
				if (Boolean.TRUE.equals(vendor.getShareShowAct())) {
					actAirline = wnfxPrice.getActCode().substring(0, 2);
				} else {
					actAirline = wnfxPrice.getCarrier();
				}
				String baggage = baggageRuleMap.getOrDefault(actAirline, Map.of()).get(vendor.getCabin());
				if (baggage != null) {
					cabin.setBaggage(List.of(baggage));
				}
				cabin.setExt(Map.of("policyId", vendor.getPolicyId()));
				cabins.add(cabin);
			}

			FlightsSearchVO.FlightInfo flight = new FlightsSearchVO.FlightInfo();
			flight.setSupplier(Constants.Supplier.WNFX);
			flight.setFlightSegments(List.of(segment));
			flight.setCabins(cabins);

			flights.add(flight);
		}

		FlightsSearchVO vo = new FlightsSearchVO();
		vo.setFlights(flights);
		return vo;
	}

	private FlightsSearchVO formatRoundSegmentResponse() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	private FlightsSearchVO formatMultiSegmentResponse() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public String supportType() {
		return Constants.Supplier.WNFX;
	}

}
