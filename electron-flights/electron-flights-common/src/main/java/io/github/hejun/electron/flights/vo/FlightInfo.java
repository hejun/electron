package io.github.hejun.electron.flights.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 航班
 *
 * @author HeJun
 */
@Getter
@Setter
public class FlightInfo {

	private String supplier;

	private List<FlightSegment> flightSegments;

}
