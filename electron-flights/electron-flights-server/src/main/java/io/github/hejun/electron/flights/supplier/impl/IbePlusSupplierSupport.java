package io.github.hejun.electron.flights.supplier.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.hejun.electron.flights.constant.Constants;
import io.github.hejun.electron.flights.dto.FlightsSearchDTO;
import io.github.hejun.electron.flights.entity.SupplierAccount;
import io.github.hejun.electron.flights.supplier.ISupplierSupport;
import io.github.hejun.electron.flights.supplier.impl.request.ibePlus.FareInterfaceRequest;
import io.github.hejun.electron.flights.supplier.impl.request.ibePlus.FareInterfaceResponse;
import io.github.hejun.electron.flights.vo.FlightsSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * IBE+ 供方实现
 *
 * @author HeJun
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class IbePlusSupplierSupport implements ISupplierSupport {

	private final XmlMapper xmlMapper = XmlMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
	private final RestTemplate restTemplate = new RestTemplate();

	@Override
	public FlightsSearchVO search(SupplierAccount supplierAccount, FlightsSearchDTO flightsSearchDTO) {
		final String url = supplierAccount.getApiUrl() + "/ota/xml/AirFlightShop/D";

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

		Map<String, List<FareInterfaceResponse.Output.Result.FlightShopResult.PsAvBind>> bindsGroup = flightShopResult.getPsAvBinds().stream()
			.collect(Collectors.groupingBy(binds -> String.join("_", binds.getAvRPH()), LinkedHashMap::new, Collectors.toList()));
		for (List<FareInterfaceResponse.Output.Result.FlightShopResult.PsAvBind> binds : bindsGroup.values()) {
			List<FlightsSearchVO.FlightInfo.FlightSegment> flightSegments = new ArrayList<>();
			List<FlightsSearchVO.FlightInfo.FlightCabin> cabins = new ArrayList<>();
			for (FareInterfaceResponse.Output.Result.FlightShopResult.PsAvBind bind : binds) {
				if (flightSegments.isEmpty()) {
					for (String rph : bind.getAvRPH()) {
						flightSegments.add(this.formatFlightSegment(flightMap.get(rph)));
					}
				}
				String seq = bind.getSeq();
				FareInterfaceResponse.Output.Result.FlightShopResult.PS ps = psMap.get(seq);
				if (ps == null) {
					continue;
				}
				Map<String, String> taxMap = ps.getTaxes().stream().collect(Collectors.toMap(FareInterfaceResponse.Output.Result.FlightShopResult.PS.Tax::getCode, FareInterfaceResponse.Output.Result.FlightShopResult.PS.Tax::getAmt));

				List<String> cabinCountList = new ArrayList<>();
				for (int i = 0; i < bind.getBkClass().size(); i++) {
					String rph = bind.getAvRPH().get(i);
					String bkClass = bind.getBkClass().get(i);
					String cabinCount = cabinCountMap.getOrDefault(rph, Collections.emptyMap()).get(bkClass);
					cabinCountList.add(cabinCount);
				}

				FlightsSearchVO.FlightInfo.FlightCabin cabin = new FlightsSearchVO.FlightInfo.FlightCabin();
				cabin.setCode(bind.getBkClass());
				cabin.setCount(cabinCountList);
				cabin.setAmount(ps.getDisAmt());
				cabin.setCnTax(taxMap.getOrDefault("CN", "0"));
				cabin.setYqTax(taxMap.getOrDefault("YQ", "0"));
				String yFareAmount = ps.getFCs().stream()
					.map(FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC::getYFares)
					.filter(Objects::nonNull)
					.map(FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC.YFares::getYFareAmount)
					.filter(Objects::nonNull)
					.max(Comparator.comparing(Double::valueOf))
					.orElse(null);
				cabin.setYFareAmount(yFareAmount);
				if (yFareAmount != null) {
					BigDecimal discount = NumberUtils.createBigDecimal(cabin.getAmount())
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
					.toList();
				cabin.setBaggage(baggage);

				cabins.add(cabin);
			}
			FlightsSearchVO.FlightInfo flightInfo = new FlightsSearchVO.FlightInfo();
			flightInfo.setSupplier(Constants.Supplier.IBE_PLUS);
			flightInfo.setFlightSegments(flightSegments);
			flightInfo.setCabins(cabins);
			flights.add(flightInfo);
		}

		FlightsSearchVO vo = new FlightsSearchVO();
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

	private List<String> formatRefundRule(List<FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC> fcs,
										  Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.FSRefundDetailDisplay> refundRuleMap) {
		List<String> rules = new ArrayList<>();
		for (FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC fc : fcs) {
			List<String> refundRPHs = fc.getRefundDetailRph();
			if (CollectionUtils.isEmpty(refundRPHs)) {
				continue;
			}
			List<String> itemRules = new ArrayList<>();
			itemRules.add(refundRuleMap.get(refundRPHs.getFirst()).getLastDepartureTime());
			for (int i = 0; i < refundRPHs.size() - 1; i++) {
				itemRules.add(refundRuleMap.get(refundRPHs.get(i)).getRefundPercent());
				itemRules.add(refundRuleMap.get(refundRPHs.get(i)).getFirstDepartureTime());
			}
			itemRules.add(refundRuleMap.get(refundRPHs.getLast()).getRefundPercent());
			itemRules.add(refundRuleMap.get(refundRPHs.getLast()).getLastDepartureTime());

			rules.add(String.join("-", itemRules));
		}
		return rules;
	}

	private List<String> formatReissueRule(List<FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC> fcs,
										   Map<String, FareInterfaceResponse.Output.Result.FlightShopResult.FSReissueDetailDisplay> reissueRuleMap) {
		List<String> rules = new ArrayList<>();
		for (FareInterfaceResponse.Output.Result.FlightShopResult.PS.FC fc : fcs) {
			List<String> reissueRPHs = fc.getReissueDetailRph();
			if (CollectionUtils.isEmpty(reissueRPHs)) {
				continue;
			}
			List<String> itemRules = new ArrayList<>();
			itemRules.add(reissueRuleMap.get(reissueRPHs.getFirst()).getLastDepartureTime());
			for (int i = 0; i < reissueRPHs.size() - 1; i++) {
				itemRules.add(reissueRuleMap.get(reissueRPHs.get(i)).getReissuePercent());
				itemRules.add(reissueRuleMap.get(reissueRPHs.get(i)).getFirstDepartureTime());
			}
			itemRules.add(reissueRuleMap.get(reissueRPHs.getLast()).getReissuePercent());
			itemRules.add(reissueRuleMap.get(reissueRPHs.getLast()).getLastDepartureTime());

			rules.add(String.join("-", itemRules));
		}
		return rules;
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
	public String supportType() {
		return Constants.Supplier.IBE_PLUS;
	}


}
