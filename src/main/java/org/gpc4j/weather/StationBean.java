package org.gpc4j.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import generated.Ob;
import generated.Station;
import generated.Variable;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * @author Lyle T Harris
 */
@Named("station")
@RequestScope
public class StationBean {

  static final String fiveMinute =
      "https://api.mesowest.net/v2/stations/timeseries?stid=KPDX&recent=4320&obtimezone=local&complete=1&hfmetars=1&token=d8c6aee36a994f90857925cea26934be";


  private Station station;

  LineChartModel tempGraph;

  /**
   * Number of hours to look back for comparison.
   */
  private static final int NUM_HOURS_AGO = 24;

  private static final ZoneOffset zone = ZoneOffset.ofHours(-7);

  final static private Logger LOG
      = LoggerFactory.getLogger(StationBean.class);


  @PostConstruct
  public void postConstruct() throws IOException {
    LOG.info("Start...");


    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response =
        restTemplate.exchange(fiveMinute, HttpMethod.GET, HttpEntity.EMPTY, String.class);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode json = mapper.reader().readTree(response.getBody());
    ArrayNode stations = (ArrayNode) json.get("STATION");
    JsonNode station = stations.get(0);
    JsonNode observations = station.get("OBSERVATIONS");
    ArrayNode airTemp = (ArrayNode) observations.get("air_temp_set_1");
    List<Double> temps = new LinkedList<>();
    airTemp.forEach(node -> temps.add(node.asDouble() * 9 / 5 + 32));

    tempGraph = new LineChartModel();
    tempGraph.setTitle("Temperature");
    tempGraph.setLegendPosition("n");

    Axis xAxis = tempGraph.getAxis(AxisType.X);
    xAxis.setMin(-10);

    Axis yAxis = tempGraph.getAxis(AxisType.Y);
    yAxis.setLabel("Degrees F");

    LineChartSeries now = new LineChartSeries();
    now.setShowMarker(false);

    LineChartSeries yesterday = new LineChartSeries();
    yesterday.setShowMarker(false);

//    Ob latest = station.getOb().get(0);
//    now.setLabel("Today  (" + getTemp(latest) + ")");
//    yesterday.setLabel("Yesterday  ("
//        + getTempHoursAgo(NUM_HOURS_AGO, latest) + ")");

//    Collections.reverse(temps);

    int i = 1;


    for (Double temp : temps) {

      int tempNow = (int) Math.round(temp);

//      long time = Long.parseLong(ob.getUtime());
//      LocalDateTime ldt = LocalDateTime.ofEpochSecond(time, 0, zone);

//      int tempYesterday
//          = Integer.parseInt(getTempHoursAgo(NUM_HOURS_AGO, ldt));


      now.set(i++, tempNow);

//      if (tempYesterday > 0 && tempNow > 0) {
//        now.set(i, tempNow);
//        yesterday.set(i, tempYesterday);
//        i++;
//      }

    }

    xAxis.setMax((now.getData().size()) + 20);
    //xAxis.setMax(650);

    tempGraph.addSeries(now);
//    tempGraph.addSeries(yesterday);
  }


  public List<String> getTemps() {

    List<String> temps
        = Collections.synchronizedList(new ArrayList<String>());

    station.getOb().forEach(ob -> {
      ob.getVariable().stream()
          .filter(v -> v.getVar().equals("T"))
          .forEach(v -> {
            temps.add(v.getValue());
          });
    });

    Collections.reverse(temps);
    return temps;
  }


  /**
   * Get the temperature N number of hours ago.
   *
   * @param numberOfHoursAgo
   * @param now
   * @return
   */
  String getTempHoursAgo(int numberOfHoursAgo, LocalDateTime now) {

    LocalDateTime past = now.plusHours(-numberOfHoursAgo);
    // System.out.println("Past: " + past);

    Optional<Ob> ago = station.getOb().stream()
        .filter(ob -> {
          long time = Long.parseLong(ob.getUtime());
          LocalDateTime ldt
              = LocalDateTime.ofEpochSecond(time, 0, zone);
          return ldt.plusSeconds(15).isAfter(past)
              && ldt.plusSeconds(-15).isBefore(past);
        })
        .findFirst();

    if (ago.isPresent()) {
      Ob ob = ago.get();
      LOG.debug("Found: " + ob.getTime());
      return getTemp(ob);
    }

    return "0";

  }


  String getTempHoursAgo(int numberOfHoursAgo, Ob now) {

    long time = Long.parseLong(now.getUtime());
    LocalDateTime ldt = LocalDateTime.ofEpochSecond(time, 0, zone);

    return getTempHoursAgo(numberOfHoursAgo, ldt);
  }


  String getTemp(Ob ob) {
    Optional<Variable> temp = ob.getVariable().stream()
        .filter(v -> v.getVar().equals("T"))
        .findAny();

    if (temp.isPresent()) {
      return temp.get().getValue();
    } else {
      return "0";
    }
  }


  public LineChartModel getTempGraph() {
    return tempGraph;
  }


  public static void main(String[] args) throws IOException {

//        System.setProperty("http.proxyHost",
//                "http://www-proxy.us.oracle.com");
//        System.setProperty("http.proxyPort",
//                "80");
    StationBean sb = new StationBean();
    sb.postConstruct();

    // System.out.println("Zones: " + ZoneId.getAvailableZoneIds());
    sb.station.getOb().stream().forEach(ob -> {

      long time = Long.parseLong(ob.getUtime());

      LocalDateTime ldt
          = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.ofHours(-7));

      System.out.println("utime: " + ob.getUtime() + ";  "
          + ob.getTime() + "; "
          + sb.getTempHoursAgo(1, ldt) + "; "
          + ldt);
    });

    // sb.getTemps().forEach(System.out::println);
  }


}
