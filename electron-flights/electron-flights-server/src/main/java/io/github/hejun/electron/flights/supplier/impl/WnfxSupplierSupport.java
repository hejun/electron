package io.github.hejun.electron.flights.supplier.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.hejun.electron.datacenter.api.AirportApi;
import io.github.hejun.electron.datacenter.vo.AirportVO;
import io.github.hejun.electron.flights.constant.Constants;
import io.github.hejun.electron.flights.dto.FlightPricesDTO;
import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.supplier.ISupplierSupport;
import io.github.hejun.electron.flights.supplier.impl.request.wnfx.*;
import io.github.hejun.electron.flights.vo.FlightPricesVO;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 蜗牛分销 供方实现
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WnfxSupplierSupport implements ISupplierSupport {

	private final AirportApi airportApi;
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
	public FlightsSearchVO searchDomesticFlights(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		if (flightsSearchDTO.getSegments().size() == 1) {
			return this.searchSingle(supplierAccount, flightsSearchDTO);
		} else {
			if (flightsSearchDTO.getSegments().size() == 2) {
				FlightsSearchDTO.FlightsSearchSegment firstSegment = flightsSearchDTO.getSegments().getFirst();
				FlightsSearchDTO.FlightsSearchSegment lastSegment = flightsSearchDTO.getSegments().getLast();
				String firstConcatSegment = firstSegment.getDepartureCityCode() + firstSegment.getArrivalCityCode();
				String lastConcatSegment = lastSegment.getDepartureCityCode() + lastSegment.getArrivalCityCode();
				if (Objects.equals(firstConcatSegment, lastConcatSegment)) {
					return this.searchRound();
				}
			}
			return this.searchMulti();
		}
	}

	private FlightsSearchVO searchSingle(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		ObjectNode params = objectMapper.createObjectNode();
		List<AirportVO> dptAirports = airportApi.findAirportByCityCode(flightsSearchDTO.getSegments().getFirst().getDepartureCityCode());
		List<AirportVO> arrAirports = airportApi.findAirportByCityCode(flightsSearchDTO.getSegments().getFirst().getArrivalCityCode());
		params.put("dpt", dptAirports.getFirst().getCode());
		params.put("arr", arrAirports.getFirst().getCode());
		params.put("date", DateFormatUtils.format(flightsSearchDTO.getSegments().getFirst().getDepartureDate(), "yyyy-MM-dd"));
		if (supplierAccount.getExtParam() != null) {
			params.put("ex_track", supplierAccount.getExtParam().getOrDefault("ex_track", "chailv"));
		} else {
			params.put("ex_track", "chailv");
		}

		WnfxResponse<WnfxSearch> flightResp = this.request(supplierAccount, "flight.national.supply.sl.searchflight", params, new ParameterizedTypeReference<>() {
		});

		return this.formatSingleSegmentResponse(flightsSearchDTO, flightResp);
	}

	private FlightsSearchVO formatSingleSegmentResponse(FlightsSearchDTO flightsSearchDTO, WnfxResponse<WnfxSearch> resp) {
		List<FlightsSearchVO.FlightInfo> flights = new ArrayList<>();

		for (WnfxSearch.FlightInfo wnfxFlight : resp.getResult().getFlightInfos()) {
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


			FlightsSearchVO.FlightInfo flight = new FlightsSearchVO.FlightInfo();
			flight.setSupplier(Constants.Supplier.WNFX);
			flight.setFlightSegments(List.of(segment));
			flight.setMinPrice(Math.max(wnfxFlight.getMinVppr(), wnfxFlight.getBarePrice()));

			flights.add(flight);
		}

		FlightsSearchVO vo = new FlightsSearchVO();
		vo.setTotal(flights.size());
		vo.setFlights(flights);
		return vo;
	}

	private FlightsSearchVO searchRound() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	private FlightsSearchVO searchMulti() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public FlightPricesVO searchDomesticPrices(SupplierAccount supplierAccount, FlightPricesDTO flightPricesDTO) {
		if (flightPricesDTO.getSegments().size() == 1) {
			return this.searchSinglePrices(supplierAccount, flightPricesDTO);
		} else {
			if (flightPricesDTO.getSegments().size() == 2) {
				FlightPricesDTO.FlightPricesSegment firstSegment = flightPricesDTO.getSegments().getFirst();
				FlightPricesDTO.FlightPricesSegment lastSegment = flightPricesDTO.getSegments().getLast();
				String firstConcatSegment = firstSegment.getDepartureAirlineCode() + firstSegment.getArrivalAirlineCode();
				String lastConcatSegment = lastSegment.getArrivalAirlineCode() + lastSegment.getDepartureAirlineCode();
				if (Objects.equals(firstConcatSegment, lastConcatSegment)) {
					return this.searchRoundPrices();
				}
			}
			return this.searchMultiPrices();
		}
	}

	private FlightPricesVO searchSinglePrices(SupplierAccount supplierAccount, FlightPricesDTO flightPricesDTO) {
		ObjectNode params = objectMapper.createObjectNode();
		FlightPricesDTO.FlightPricesSegment segment = flightPricesDTO.getSegments().getFirst();
		params.put("dpt", segment.getDepartureAirlineCode());
		params.put("arr", segment.getArrivalAirlineCode());
		params.put("date", DateFormatUtils.format(segment.getDepartureDate(), "yyyy-MM-dd"));
		params.put("flightNum", segment.getAirline() + segment.getFlightNo());
		if (supplierAccount.getExtParam() != null) {
			params.put("ex_track", supplierAccount.getExtParam().getOrDefault("ex_track", "chailv"));
		} else {
			params.put("ex_track", "chailv");
		}

		WnfxResponse<WnfxPrice> priceResp = this.request(supplierAccount, "flight.national.supply.sl.searchprice", params, new ParameterizedTypeReference<>() {
		});

		List<CompletableFuture<Pair<String, Pair<String, String>>>> refundReissueRuleFutures = new ArrayList<>(priceResp.getResult().getVendors().size());
		List<CompletableFuture<Pair<String, String>>> baggageRuleFutures = new ArrayList<>(priceResp.getResult().getVendors().size());

		Set<String> checkRepeatBaggageRule = new HashSet<>(priceResp.getResult().getVendors().size());
		for (WnfxPrice.Vendor vendor : priceResp.getResult().getVendors()) {
			// 退改签规则
			CompletableFuture<Pair<String, Pair<String, String>>> repeatRefundReissueRuleFuture = CompletableFuture.supplyAsync(() -> {
				ObjectNode refundReissueRuleParams = objectMapper.createObjectNode();
				refundReissueRuleParams.put("flightNum", priceResp.getResult().getCode());
				refundReissueRuleParams.put("cabin", vendor.getCabin());
				refundReissueRuleParams.put("dep", priceResp.getResult().getDepCode());
				refundReissueRuleParams.put("arr", priceResp.getResult().getArrCode());
				refundReissueRuleParams.put("dptDate", priceResp.getResult().getDate());
				refundReissueRuleParams.put("dptTime", priceResp.getResult().getBtime());
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
				return Pair.of(vendor.getPolicyId(), Pair.of(returnRule, changeRule));
			});
			refundReissueRuleFutures.add(repeatRefundReissueRuleFuture);
			// 行李额
			if (!checkRepeatBaggageRule.contains(vendor.getCabin())) {
				checkRepeatBaggageRule.add(vendor.getCabin());
				CompletableFuture<Pair<String, String>> baggageRuleFuture = CompletableFuture.supplyAsync(() -> {
					String actAirlineCode;
					if (Boolean.TRUE.equals(vendor.getShareShowAct())) {
						actAirlineCode = priceResp.getResult().getActCode().substring(0, 2);
					} else {
						actAirlineCode = priceResp.getResult().getCarrier();
					}
					ObjectNode baggageRuleParams = objectMapper.createObjectNode();
					baggageRuleParams.put("airlineCode", actAirlineCode);
					baggageRuleParams.put("cabin", vendor.getCabin());
					baggageRuleParams.put("depCode", priceResp.getResult().getDepCode());
					baggageRuleParams.put("arrCode", priceResp.getResult().getArrCode());
					baggageRuleParams.put("saleDate", priceResp.getResult().getDate());
					baggageRuleParams.put("depDate", priceResp.getResult().getDate());
					if (StringUtils.isNotBlank(vendor.getLuggage())) {
						baggageRuleParams.put("luggage", vendor.getLuggage());
					}
					WnfxResponse<WnfxBaggageRule> baggageRuleResp = this.request(supplierAccount, "flight.national.supply.sl.baggagerule", baggageRuleParams, new ParameterizedTypeReference<>() {
					});
					return Pair.of(vendor.getCabin(), baggageRuleResp.getResult().getRuleBaseInfo().getCheckWeight());
				});
				baggageRuleFutures.add(baggageRuleFuture);
			}
		}

		CompletableFuture.allOf(refundReissueRuleFutures.toArray(CompletableFuture[]::new)).join();
		CompletableFuture.allOf(baggageRuleFutures.toArray(CompletableFuture[]::new)).join();

		Map<String, Pair<String, String>> refundReissueRuleMap = new HashMap<>();
		for (CompletableFuture<Pair<String, Pair<String, String>>> future : refundReissueRuleFutures) {
			try {
				Pair<String, Pair<String, String>> pair = future.get();
				if (pair == null) {
					continue;
				}
				refundReissueRuleMap.put(pair.getLeft(), pair.getRight());
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		Map<String, String> baggageRuleMap = new HashMap<>();
		for (CompletableFuture<Pair<String, String>> future : baggageRuleFutures) {
			try {
				Pair<String, String> pair = future.get();
				if (pair == null) {
					continue;
				}
				baggageRuleMap.put(pair.getLeft(), pair.getRight());
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		return this.formatSinglePricesResponse(priceResp.getResult(), refundReissueRuleMap, baggageRuleMap);
	}

	private FlightPricesVO formatSinglePricesResponse(WnfxPrice wnfxPrice, Map<String, Pair<String, String>> refundReissueRuleMap,
													  Map<String, String> baggageRuleMap) {
		List<FlightPricesVO.FlightCabin> cabins = new ArrayList<>();
		for (WnfxPrice.Vendor vendor : wnfxPrice.getVendors()) {
			FlightPricesVO.FlightCabin cabin = new FlightPricesVO.FlightCabin();
			cabin.setSupplier(Constants.Supplier.WNFX);
			cabin.setCode(List.of(vendor.getCabin()));
			cabin.setCount(List.of(vendor.getCabinCount()));
			cabin.setSellPrice(vendor.getVppr());
			cabin.setCostPrice(vendor.getBarePrice());
			cabin.setAirportTax(wnfxPrice.getArf());
			cabin.setOilTax(wnfxPrice.getTof());
			cabin.setYFareAmount(vendor.getBasePrice() == null || vendor.getBasePrice() == 0 ? null : vendor.getBasePrice());
			cabin.setDiscount(vendor.getDiscount());

			Pair<String, String> refundReissueRulePair = refundReissueRuleMap.get(vendor.getPolicyId());
			if (refundReissueRulePair != null) {
				cabin.setRefundRule(List.of(refundReissueRulePair.getLeft()));
				cabin.setReissueRule(List.of(refundReissueRulePair.getRight()));
			}

			String baggage = baggageRuleMap.get(vendor.getCabin());
			if (baggage != null) {
				cabin.setBaggage(List.of(baggage));
			}
			cabin.setExt(Map.of(
					"policyId", vendor.getPolicyId(),
					"tag", Optional.ofNullable(vendor.getTagProperty()).map(s -> s.split("/")).stream().flatMap(Arrays::stream).toList()
				)
			);
			cabins.add(cabin);
		}

		FlightPricesVO vo = new FlightPricesVO();
		vo.setTotal(cabins.size());
		vo.setCabins(cabins);
		return vo;
	}

	private FlightPricesVO searchRoundPrices() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	private FlightPricesVO searchMultiPrices() {
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

	@Override
	public String supportType() {
		return Constants.Supplier.WNFX;
	}

}
