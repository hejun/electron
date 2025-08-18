package io.github.hejun.electron.flights.supplier.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.hejun.electron.datacenter.api.AirportApi;
import io.github.hejun.electron.flights.constant.Constants;
import io.github.hejun.electron.flights.dto.FlightPricesDTO;
import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.supplier.ISupplierSupport;
import io.github.hejun.electron.flights.supplier.impl.request.ibePlus.FareInterfaceRequest;
import io.github.hejun.electron.flights.supplier.impl.request.ibePlus.FareInterfaceResponse;
import io.github.hejun.electron.flights.vo.FlightPricesVO;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * IBE+ 供方实现
 *
 * @author HeJun
 */
@Slf4j
@Component
public class IbePlusSupplierSupport implements ISupplierSupport {

	private final XmlMapper xmlMapper = XmlMapper.builder().build();
	private final RestTemplate restTemplate = new RestTemplate();
	private final RedisTemplate<String, FareInterfaceResponse> redisTemplate;

	private final Function<FlightsSearchDTO, String> FLIGHT_CACHE_KEY_GENERATOR;
	private final Function<FlightPricesDTO, String> PRICE_CACHE_KEY_GENERATOR;

	@Autowired
	public IbePlusSupplierSupport(RedisTemplate<String, FareInterfaceResponse> redisTemplate, AirportApi airportApi) {
		this.redisTemplate = redisTemplate;
		if (log.isDebugEnabled()) {
			restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
			restTemplate.getInterceptors().add((request, body, execution) -> {
				log.debug("Ibe+ Request: {}", new String(body, StandardCharsets.UTF_8));
				ClientHttpResponse response = execution.execute(request, body);
				log.debug("Ibe+ Response: {}", StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
				return response;
			});
		}
		restTemplate.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().set("Accept-Encoding", "gzip, deflate");
			ClientHttpResponse response = execution.execute(request, body);
			List<String> contentEncodings = Objects
				.requireNonNullElse(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING), Collections.emptyList());
			if (contentEncodings.contains("gzip")) {
				return new DecompressedClientHttpResponse(response);
			}
			return response;
		});

		// FLIGHT_CACHE_KEY_GENERATOR 和 PRICE_CACHE_KEY_GENERATOR 值需要一样
		this.FLIGHT_CACHE_KEY_GENERATOR = flightsSearchDTO -> flightsSearchDTO.getSegments().stream()
			.map(s -> String.join("_", s.getDepartureCityCode(), s.getArrivalCityCode(), DateFormatUtils.format(s.getDepartureDate(), "yyyyMMdd")))
			.collect(Collectors.joining("-"));
		this.PRICE_CACHE_KEY_GENERATOR = flightPricesDTO -> flightPricesDTO.getSegments().stream()
			.map(s -> String.join("_", airportApi.findZoneByAirportCode(s.getDepartureAirlineCode()).getThreeCode(), airportApi.findZoneByAirportCode(s.getArrivalAirlineCode()).getThreeCode(), DateFormatUtils.format(s.getDepartureDate(), "yyyyMMdd")))
			.collect(Collectors.joining("-"));
	}

	@Override
	public FlightsSearchVO searchDomesticFlights(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		final String url = supplierAccount.getApiUrl() + "/AirFlightShop/D";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);
		headers.setBasicAuth(supplierAccount.getAccount(), supplierAccount.getSecret());
		if (CollectionUtils.isEmpty(supplierAccount.getExtParam()) || !supplierAccount.getExtParam().containsKey("AID")) {
			throw new RuntimeException("AID is required");
		}
		headers.add("AID", supplierAccount.getExtParam().get("AID"));
		headers.set("EchoToken", UUID.randomUUID().toString().replaceAll("-", ""));

		String request = this.buildRequest(supplierAccount, flightsSearchDTO);

		FareInterfaceResponse resp = restTemplate.postForObject(url, new HttpEntity<>(request, headers), FareInterfaceResponse.class);

		if (resp == null) {
			return null;
		} else {
			// TODO 需要优化缓存逻辑,减少IBE+访问次数
			redisTemplate.opsForValue().set(FLIGHT_CACHE_KEY_GENERATOR.apply(flightsSearchDTO), resp, Duration.ofMinutes(10));
		}

		return this.formatResponse(resp);
	}

	private FlightsSearchVO formatResponse(FareInterfaceResponse resp) {
		List<FlightsSearchVO.FlightInfo> flights = new ArrayList<>();

		FareInterfaceResponse.Output.Result.FlightShopResult flightShopResult = resp.getOutput().getResult().getFlightShopResult();

		Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney.AvOpt.Flt> flightMap = new HashMap<>();
		for (FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney avJourney : flightShopResult.getAvJourneys().getAvJourney()) {
			for (FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney.AvOpt avOpt : avJourney.getAvOpts()) {
				flightMap.put(avOpt.getFlt().getRPH(), avOpt.getFlt());
			}
		}

		Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.PS> psMap = flightShopResult.getPSn().stream()
			.collect(Collectors.toMap(FareInterfaceResponse.Output.Result.FlightShopResult.PS::getSeq, ps -> ps));

		Map<String, FlightsSearchVO.FlightInfo> checkRepeatAndCompareMinPriceMap = new HashMap<>();
		for (FareInterfaceResponse.Output.Result.FlightShopResult.PsAvBind bind : flightShopResult.getPsAvBinds()) {
			List<FlightsSearchVO.FlightInfo.FlightSegment> flightSegments = new ArrayList<>();
			for (String rph : bind.getAvRPH()) {
				flightSegments.add(this.formatFlightSegment(flightMap.get(rph)));
			}
			String flightGroup = flightSegments.stream().map(s -> String.join("", s.getAirline(), s.getFlightNo())).collect(Collectors.joining("_"));
			Double flightPrice = psMap.get(bind.getSeq()).getDisAmt();
			if (checkRepeatAndCompareMinPriceMap.containsKey(flightGroup)) {
				FlightsSearchVO.FlightInfo flightInfo = checkRepeatAndCompareMinPriceMap.get(flightGroup);
				if (flightPrice < flightInfo.getMinPrice()) {
					flightInfo.setMinPrice(flightPrice);
				}
			} else {
				FlightsSearchVO.FlightInfo flightInfo = new FlightsSearchVO.FlightInfo();
				flightInfo.setSupplier(Constants.Supplier.IBE_PLUS);
				flightInfo.setFlightSegments(flightSegments);
				flightInfo.setMinPrice(flightPrice);
				flights.add(flightInfo);

				checkRepeatAndCompareMinPriceMap.put(flightGroup, flightInfo);
			}
		}

		FlightsSearchVO vo = new FlightsSearchVO();
		vo.setTotal(flights.size());
		vo.setFlights(flights);
		return vo;
	}

	private FlightsSearchVO.FlightInfo.FlightSegment formatFlightSegment(FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney.AvOpt.Flt flight) {
		FlightsSearchVO.FlightInfo.FlightSegment flightSegment = new FlightsSearchVO.FlightInfo.FlightSegment();
		flightSegment.setAirline(flight.getAirline());
		flightSegment.setFlightNo(flight.getFltNo());

		flightSegment.setDepartureAirlineCode(flight.getDep());
		Date departureDate;
		try {
			departureDate = DateUtils.parseDate(flight.getDt(), Locale.ENGLISH, "ddMMMyy");
			flightSegment.setDepartureDate(DateFormatUtils.format(departureDate, "yyyy-MM-dd"));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		flightSegment.setDepartureTime(flight.getDeptm().replaceAll("(.{2})", ":$1").substring(1));
		flightSegment.setDepartureTerminalCode(flight.getTerm().getDep());

		flightSegment.setArrivalAirlineCode(flight.getArr());
		if (StringUtils.hasText(flight.getArrad())) {
			Date arrivalDate = DateUtils.addDays(departureDate, Integer.parseInt(flight.getArrad()));
			flightSegment.setArrivalDate(DateFormatUtils.format(arrivalDate, "yyyy-MM-dd"));
		} else {
			flightSegment.setArrivalDate(flightSegment.getDepartureDate());
		}
		flightSegment.setArrivalTime(flight.getArrtm().replaceAll("(.{2})", ":$1").substring(1));
		flightSegment.setArrivalTerminalCode(flight.getTerm().getArr());

		Date departureDatetime;
		Date arrivalDatetime;
		try {
			departureDatetime = DateUtils.parseDate(flightSegment.getDepartureDate() + " " + flightSegment.getDepartureTime(), "yyyy-MM-dd HH:mm");
			arrivalDatetime = DateUtils.parseDate(flightSegment.getArrivalDate() + " " + flightSegment.getArrivalTime(), "yyyy-MM-dd HH:mm");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		flightSegment.setDuration(TimeUnit.MILLISECONDS.toMinutes(arrivalDatetime.getTime() - departureDatetime.getTime()));
		flightSegment.setAircraftType(flight.getDev());
		if (flight.getStopOvers() != null) {
			List<FlightsSearchVO.FlightInfo.FlightSegment.FlightStop> stops = new ArrayList<>();
			for (FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney.AvOpt.Flt.StopOvers.StopInfo stopInfo : flight.getStopOvers().getStopInfo()) {
				FlightsSearchVO.FlightInfo.FlightSegment.FlightStop stop = new FlightsSearchVO.FlightInfo.FlightSegment.FlightStop();
				stop.setStopAirlineCode(stopInfo.getStopCity());
				stop.setStopTime(stopInfo.getStopTime());
				stop.setArrivalTerminalCode(stopInfo.getArrTerm());
				stop.setDepartureTerminalCode(stopInfo.getDepTerm());
				stops.add(stop);
			}
			flightSegment.setStops(stops);
		}
		flightSegment.setMileage(flight.getTpm());
		flightSegment.setMeal(flight.getMeal());
		if (flight.getCodeshare() != null) {
			FlightsSearchVO.FlightInfo.FlightSegment.ShareFlightInfo shareFlightInfo = new FlightsSearchVO.FlightInfo.FlightSegment.ShareFlightInfo();
			shareFlightInfo.setAirline(flight.getCodeshare().getAirline());
			shareFlightInfo.setFlightNo(flight.getCodeshare().getFltNo());
			flightSegment.setShareFlight(shareFlightInfo);
		}
		return flightSegment;
	}

	private String buildRequest(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		Map<String, String> extParam = supplierAccount.getExtParam();
		if (CollectionUtils.isEmpty(extParam)) {
			throw new RuntimeException("Invalid ibe+ extParam");
		}

		FareInterfaceRequest.Input.HeaderIn.Agency agency = new FareInterfaceRequest.Input.HeaderIn.Agency();
		agency.setOfficeId(extParam.get("officeId"));
		agency.setPid(extParam.get("pid"));
		agency.setCity(extParam.get("city"));

		FareInterfaceRequest.Input.HeaderIn headerIn = new FareInterfaceRequest.Input.HeaderIn();
		headerIn.setSysCode("CRS");
		headerIn.setChannelID("1E");
		headerIn.setChannelType("COMMON");
		headerIn.setAgency(agency);
		headerIn.setLanguage("CN");
		headerIn.setCommandType("FS");

		List<FareInterfaceRequest.Input.Request.FlightShopRequest.OriginDestinationInfo> originDestinationInfos = new ArrayList<>();
		for (FlightsSearchDTO.FlightsSearchSegment segment : flightsSearchDTO.getSegments()) {
			FareInterfaceRequest.Input.Request.FlightShopRequest.OriginDestinationInfo originDestinationInfo = new FareInterfaceRequest.Input.Request.FlightShopRequest.OriginDestinationInfo();
			originDestinationInfo.setOri(segment.getDepartureCityCode());
			originDestinationInfo.setDes(segment.getArrivalCityCode());
			originDestinationInfo.setDepartureDate(DateFormatUtils.format(segment.getDepartureDate(), "ddMMMyy", Locale.ENGLISH).toUpperCase());
			originDestinationInfos.add(originDestinationInfo);
		}

		FareInterfaceRequest.Input.Request.FlightShopRequest.TravelPreferences.Passenger passenger = new FareInterfaceRequest.Input.Request.FlightShopRequest.TravelPreferences.Passenger();
		if (flightsSearchDTO.getChildCount() != null && flightsSearchDTO.getChildCount() > 0) {
			passenger.setNumber(1);
			passenger.setType("CH");
		} else if (flightsSearchDTO.getInfantCount() != null && flightsSearchDTO.getInfantCount() > 0) {
			passenger.setNumber(1);
			passenger.setType("IN");
		} else {
			passenger.setNumber(1);
			passenger.setType("AD");
		}

		FareInterfaceRequest.Input.Request.FlightShopRequest.TravelPreferences travelPreferences = new FareInterfaceRequest.Input.Request.FlightShopRequest.TravelPreferences();
		travelPreferences.setCurrCode("CNY");
		travelPreferences.setDisplayCurrCode("CNY");
		travelPreferences.setIsDirectFlightOnly(false);
		travelPreferences.setJourneyType("OW");
		travelPreferences.setPassenger(passenger);

		FareInterfaceRequest.Input.Request.FlightShopRequest.Option option = new FareInterfaceRequest.Input.Request.FlightShopRequest.Option();
		option.setIsCityOrAirport("2");
		option.setRuleTypeNeeded("NON");
		option.setLowestOrAll("A");
		option.setIsRefundReissueRuleNeeded("Y");
		option.setIncludeBaggage(true);

		FareInterfaceRequest.Input.Request.FlightShopRequest flightShopRequest = new FareInterfaceRequest.Input.Request.FlightShopRequest();
		flightShopRequest.setOriginDestinationInfo(originDestinationInfos);
		flightShopRequest.setTravelPreferences(travelPreferences);
		flightShopRequest.setOption(option);

		FareInterfaceRequest.Input.Request request = new FareInterfaceRequest.Input.Request();
		request.setFlightShopRequest(flightShopRequest);

		FareInterfaceRequest.Input input = new FareInterfaceRequest.Input();
		input.setHeaderIn(headerIn);
		input.setRequest(request);

		FareInterfaceRequest searchRequest = new FareInterfaceRequest();
		searchRequest.setInput(input);

		try {
			return xmlMapper.writeValueAsString(searchRequest);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public FlightPricesVO searchDomesticPrices(SupplierAccount supplierAccount, FlightPricesDTO flightPricesDTO) {
		String cacheKey = PRICE_CACHE_KEY_GENERATOR.apply(flightPricesDTO);

		FareInterfaceResponse resp;
		if (Boolean.FALSE.equals(redisTemplate.hasKey(cacheKey))
			|| (resp = redisTemplate.opsForValue().get(cacheKey)) == null) {
			throw new RuntimeException("您航班查询停留时间过长, 请重新查询");
		}

		FareInterfaceResponse.Output.Result.FlightShopResult flightShopResult = resp.getOutput().getResult().getFlightShopResult();

		Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney.AvOpt.Flt> flightMap = new HashMap<>();
		for (FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney avJourney : flightShopResult.getAvJourneys().getAvJourney()) {
			for (FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney.AvOpt avOpt : avJourney.getAvOpts()) {
				flightMap.put(avOpt.getFlt().getRPH(), avOpt.getFlt());
			}
		}

		Map<String, Map<String, String>> cabinCountMap = new HashMap<>();
		for (FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney.AvOpt.Flt flt : flightMap.values()) {
			cabinCountMap.putIfAbsent(flt.getRPH(), new HashMap<>());
			for (FareInterfaceResponse.Output.Result.FlightShopResult.AvJourneys.AvJourney.AvOpt.Flt.Clazz clazz : flt.getClazz()) {
				cabinCountMap.get(flt.getRPH()).put(clazz.getName(), clazz.getAv());
			}
		}

		Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.PS> psMap = flightShopResult.getPSn().stream()
			.collect(Collectors.toMap(FareInterfaceResponse.Output.Result.FlightShopResult.PS::getSeq, ps -> ps));

		Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.FSRefundDetailDisplay> refundRuleMap = flightShopResult.getFsRefundRuleDisplays().stream()
			.collect(Collectors.toMap(FareInterfaceResponse.Output.Result.FlightShopResult.FSRefundDetailDisplay::getRefundDetailRph, rule -> rule));

		Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.FSReissueDetailDisplay> reissueRuleMap = flightShopResult.getFsReissueRuleDisplay().stream()
			.collect(Collectors.toMap(FareInterfaceResponse.Output.Result.FlightShopResult.FSReissueDetailDisplay::getReissueDetailRph, rule -> rule));

		List<FlightPricesVO.FlightCabin> cabins = new ArrayList<>();

		String targetFlightGroup = flightPricesDTO.getSegments().stream()
			.map(s -> String.join("", s.getAirline(), s.getFlightNo()))
			.collect(Collectors.joining("-"));
		for (FareInterfaceResponse.Output.Result.FlightShopResult.PsAvBind bind : flightShopResult.getPsAvBinds()) {
			String flightGroup = bind.getAvRPH().stream()
				.map(flightMap::get).filter(Objects::nonNull)
				.map(s -> String.join("", s.getAirline(), s.getFltNo()))
				.collect(Collectors.joining("-"));
			if (flightGroup.equals(targetFlightGroup)) {
				String seq = bind.getSeq();
				FareInterfaceResponse.Output.Result.FlightShopResult.PS ps = psMap.get(seq);
				if (ps == null) {
					continue;
				}

				Map<String, String> taxMap = ps.getTaxes().stream().collect(Collectors.toMap(FareInterfaceResponse.Output.Result.FlightShopResult.PS.Tax::getCode, FareInterfaceResponse.Output.Result.FlightShopResult.PS.Tax::getTaxComponent));

				List<String> cabinCountList = new ArrayList<>();
				for (int i = 0; i < bind.getBkClass().size(); i++) {
					String rph = bind.getAvRPH().get(i);
					String bkClass = bind.getBkClass().get(i);
					String cabinCount = cabinCountMap.getOrDefault(rph, Collections.emptyMap()).get(bkClass);
					cabinCountList.add(cabinCount);
				}

				FlightPricesVO.FlightCabin cabin = new FlightPricesVO.FlightCabin();
				cabin.setSupplier(Constants.Supplier.IBE_PLUS);
				cabin.setCode(bind.getBkClass());
				cabin.setCount(cabinCountList);
				cabin.setSellPrice(ps.getDisAmt());
				cabin.setCostPrice(ps.getDisAmt());
				cabin.setAirportTax(taxMap.getOrDefault("CN", "0"));
				cabin.setOilTax(taxMap.getOrDefault("YQ", "0"));
				Double yFareAmount = ps.getFCs().stream()
					.map(FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC::getYFares)
					.filter(Objects::nonNull)
					.map(FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC.YFares::getYFareAmount)
					.filter(Objects::nonNull)
					.filter(fare -> fare > 0)
					.max(Comparator.comparing(Double::valueOf))
					.orElse(null);
				cabin.setYFareAmount(yFareAmount);
				if (yFareAmount != null) {
					BigDecimal discount = BigDecimal.valueOf(cabin.getSellPrice())
						.divide(new BigDecimal(yFareAmount), 2, RoundingMode.UP)
						.multiply(BigDecimal.TEN);
					cabin.setDiscount(discount.setScale(1, RoundingMode.UP).toString());
				}
				cabin.setRefundRule(this.formatRefundRule(ps.getFCs(), refundRuleMap));
				cabin.setReissueRule(this.formatReissueRule(ps.getFCs(), reissueRuleMap));
				List<String> baggage = ps.getFCs().stream()
					.map(FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC::getSecInfo)
					.filter(Objects::nonNull)
					.map(FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC.SecInfo::getBaggage)
					.filter(Objects::nonNull)
					.toList();
				cabin.setBaggage(baggage);

				cabins.add(cabin);
			}
		}

		FlightPricesVO vo = new FlightPricesVO();
		vo.setTotal(cabins.size());
		vo.setCabins(cabins);
		return vo;
	}

	private List<String> formatRefundRule(List<FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC> fcs,
										  Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.FSRefundDetailDisplay> refundRuleMap) {
		List<Integer> sortedTimeOffset = List.of(720, 168, 48, 4, 0);
		List<String> rules = new ArrayList<>();
		for (FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC fc : fcs) {
			List<String> refundRPHs = fc.getRefundDetailRph();
			if (CollectionUtils.isEmpty(refundRPHs)) {
				continue;
			}

			Map<Integer, Integer> extractRuleMap = new HashMap<>();
			for (String reissueRPH : refundRPHs) {
				extractRuleMap.put(refundRuleMap.get(reissueRPH).getFirstDepartureTime(), refundRuleMap.get(reissueRPH).getRefundPercent());
			}

			List<String> itemRules = new ArrayList<>();
			Integer prevPercent = null;
			for (Integer offset : sortedTimeOffset) {
				Integer percent = extractRuleMap.get(offset);
				if (offset == 0 && percent == null){
					percent = extractRuleMap.get(-1);
				}
				if (percent != null) {
					prevPercent = percent;
				} else {
					percent = prevPercent;
				}
				if (percent != null) {
					itemRules.add(percent != -1 ? percent.toString() : "0");
					if (offset != 0) {
						itemRules.add(offset.toString());
					}
				}
			}
			rules.add(String.join("-", itemRules));
		}
		return rules;
	}

	private List<String> formatReissueRule(List<FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC> fcs,
										   Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.FSReissueDetailDisplay> reissueRuleMap) {
		List<Integer> sortedTimeOffset = List.of(720, 168, 48, 4, 0);
		List<String> rules = new ArrayList<>();
		for (FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC fc : fcs) {
			List<String> reissueRPHs = fc.getReissueDetailRph();
			if (CollectionUtils.isEmpty(reissueRPHs)) {
				continue;
			}

			Map<Integer, Integer> extractRuleMap = new HashMap<>();
			for (String reissueRPH : reissueRPHs) {
				extractRuleMap.put(reissueRuleMap.get(reissueRPH).getFirstDepartureTime(), reissueRuleMap.get(reissueRPH).getReissuePercent());
			}

			List<String> itemRules = new ArrayList<>();
			Integer prevPercent = null;
			for (Integer offset : sortedTimeOffset) {
				Integer percent = extractRuleMap.get(offset);
				if (offset == 0 && percent == null){
					percent = extractRuleMap.get(-1);
				}
				if (percent != null) {
					prevPercent = percent;
				} else {
					percent = prevPercent;
				}
				if (percent != null) {
					itemRules.add(percent != -1 ? percent.toString() : "0");
					if (offset != 0) {
						itemRules.add(offset.toString());
					}
				}
			}
			rules.add(String.join("-", itemRules));
		}
		return rules;
	}

	@Override
	public String supportType() {
		return Constants.Supplier.IBE_PLUS;
	}

	@RequiredArgsConstructor
	private static class DecompressedClientHttpResponse implements ClientHttpResponse {

		private final ClientHttpResponse originalResponse;
		private GZIPInputStream decompressedInputStream;

		@Nonnull
		@Override
		public HttpStatusCode getStatusCode() throws IOException {
			return originalResponse.getStatusCode();
		}

		@Nonnull
		@Override
		public String getStatusText() throws IOException {
			return originalResponse.getStatusText();
		}

		@Override
		public void close() {
			try {
				if (decompressedInputStream != null) {
					decompressedInputStream.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				originalResponse.close();
			}
		}

		@Nonnull
		@Override
		public InputStream getBody() throws IOException {
			if (decompressedInputStream != null) {
				decompressedInputStream.close();
			}
			decompressedInputStream = new GZIPInputStream(originalResponse.getBody());
			return decompressedInputStream;
		}

		@Nonnull
		@Override
		public HttpHeaders getHeaders() {
			HttpHeaders headers = originalResponse.getHeaders();
			headers.remove(HttpHeaders.CONTENT_ENCODING);
			headers.remove(HttpHeaders.CONTENT_LENGTH);
			return headers;
		}
	}

}
