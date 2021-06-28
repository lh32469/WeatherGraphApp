package org.gpc4j.weather;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gpc4j.weather.dto.TimeSeries;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author Lyle T Harris
 */
@Named("station")
@RequestScope
public class StationBean {

  /**
   * The Chart/Graph object used by JSF/Primefaces.
   */
  LineChartModel tempGraph;


  public static final DateTimeFormatter DTF
      = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

  /**
   * Readings every five minutes.
   */
  static final String fiveMinute =
      "https://api.mesowest.net/v2/stations/timeseries?stid=KPDX&recent=4320&obtimezone=local&complete=1&hfmetars=1&token=d8c6aee36a994f90857925cea26934be";


  final static private Logger LOG = LoggerFactory.getLogger(StationBean.class);


  @PostConstruct
  public void postConstruct() throws IOException {
    LOG.info("Start...");

    HttpServletRequest request = (HttpServletRequest)
        FacesContext.getCurrentInstance().getExternalContext().getRequest();
    LOG.info("request.getServerName() = " + request.getServerName());

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response =
        restTemplate.exchange(fiveMinute, HttpMethod.GET, HttpEntity.EMPTY, String.class);

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    TimeSeries timeSeries = mapper.readValue(response.getBody(), TimeSeries.class);
    Map<String, Object> observations = timeSeries.getStation().get(0).getObservations();

    Map<ZonedDateTime, Double> timesToTemps = getTimesToTemps(observations);
    List<String> times = (List<String>) observations.get("date_time");

    tempGraph = new LineChartModel();
    tempGraph.setTitle("Temperature");
    tempGraph.setLegendPosition("n");

    Axis xAxis = tempGraph.getAxis(AxisType.X);
    xAxis.setMin(-10);

    Axis yAxis = tempGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Degrees F");

    LineChartSeries now = new LineChartSeries();
    now.setShowMarker(false);
    now.setLabel("Now");

    LineChartSeries yesterday = new LineChartSeries();
    yesterday.setShowMarker(false);
    yesterday.setLabel("Prev");

    for (int i = 0; i < times.size(); i++) {
      ZonedDateTime time = ZonedDateTime.parse(times.get(i), DTF);
      Double temp = timesToTemps.get(time);

      if (null != temp) {
        now.set(i, temp * 9 / 5 + 32);
      }

      Optional<Double> prev = getTime24HoursAgo(time, timesToTemps);
      if (prev.isPresent()) {
        yesterday.set(i, prev.get());
      }
    }

    xAxis.setMax((now.getData().size()) + 20);
    //xAxis.setMax(650);

    tempGraph.addSeries(now);
    tempGraph.addSeries(yesterday);
  }


  /**
   * Gets the temperature 24 hours prior to the ZonedDateTime provided.
   * Returns Optional.empty() if no temperature is found.
   */
  Optional<Double> getTime24HoursAgo(ZonedDateTime now,
                                     Map<ZonedDateTime, Double> timesToTemps) {

    LOG.debug("now = " + now);
    Optional<ZonedDateTime> prevTemp = Optional.empty();

    LOG.debug("timesToTemps.keySet().size() = " + timesToTemps.keySet().size());

    // Narrow search area for subsequent search iterations
    List<ZonedDateTime> searchArea = timesToTemps.keySet().stream()
        .filter(t -> t.isAfter(now.minusHours(28)))
        .filter(t -> t.isBefore(now.minusHours(20)))
        .collect(Collectors.toList());

    LOG.debug("searchArea.size() = " + searchArea.size());

    // Expanding window to catch the closest one first.
    for (int i = 1; i < 15; i++) {

      ZonedDateTime prevWindowOpen = now.minusHours(24).minusMinutes(i);
      ZonedDateTime prevWindowClose = now.minusHours(24).plusMinutes(i);
      if (LOG.isTraceEnabled()) {
        LOG.trace("Between " + prevWindowOpen + " and " + prevWindowClose);
      }

      prevTemp = searchArea.stream()
          .filter(t -> t.isAfter(prevWindowOpen))
          .filter(t -> t.isBefore(prevWindowClose))
          .findFirst();

      if (prevTemp.isPresent()) {
        break;
      }
    }

    if (prevTemp.isPresent()) {
      double degF = timesToTemps.get(prevTemp.get()) * 9 / 5 + 32;
      if (LOG.isTraceEnabled()) {
        LOG.trace("prevTemp = " + prevTemp.get() + ", " + degF + " Deg F");
      }
      return Optional.of(degF);
    } else {
      return Optional.empty();
    }

  }


  /**
   * Process the Observations Map provided and return a Map of temperature
   * reading time to temperature reading in degrees fahrenheit.
   */
  Map<ZonedDateTime, Double> getTimesToTemps(Map<String, Object> observations) {

    Map<ZonedDateTime, Double> timesToTemps = new HashMap<>();

    List<Double> airTemp = (List<Double>) observations.get("air_temp_set_1");
    List<String> times = (List<String>) observations.get("date_time");

    for (int i = 0; i < times.size(); i++) {
      String time = times.get(i);
      ZonedDateTime key = ZonedDateTime.parse(time, DTF);
      if (null != airTemp.get(i)) {
        timesToTemps.put(key, airTemp.get(i));
      }
    }

    return timesToTemps;
  }


  /**
   * JSF Method to get resulting Chart/Graph.
   */
  public LineChartModel getTempGraph() {
    return tempGraph;
  }


}
