package org.gpc4j.weather;

import generated.Ob;
import generated.Station;
import generated.Variable;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * @author Lyle T Harris
 */
@Named("station")
@RequestScope
public class StationBean {

  String site = "https://www.wrh.noaa.gov/mesowest/"
      + "getobextXml.php?sid=KPDX&num=48";

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
  public void postConstruct() {
    try {
      LOG.info("Start...");

      URL url = new URL(site);
      JAXBContext jc = JAXBContext.newInstance(Station.class);
      Unmarshaller um = jc.createUnmarshaller();

      station = (Station) um.unmarshal(url);

      // Marshaller m = jc.createMarshaller();
      // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      // m.marshal(station, System.out);
    } catch (IOException | JAXBException ex) {
      LOG.error(site, ex);
    }

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

    Ob latest = station.getOb().get(0);
    now.setLabel("Today  (" + getTemp(latest) + ")");
    yesterday.setLabel("Yesterday  ("
        + getTempHoursAgo(NUM_HOURS_AGO, latest) + ")");

    Collections.reverse(station.getOb());

    int i = 1;
    for (Ob ob : station.getOb()) {

      int tempNow = Integer.parseInt(getTemp(ob));

      long time = Long.parseLong(ob.getUtime());
      LocalDateTime ldt = LocalDateTime.ofEpochSecond(time, 0, zone);

      int tempYesterday
          = Integer.parseInt(getTempHoursAgo(NUM_HOURS_AGO, ldt));

      if (tempYesterday > 0 && tempNow > 0) {
        now.set(i, tempNow);
        yesterday.set(i, tempYesterday);
        i++;
      }

    }

    xAxis.setMax((now.getData().size()) + 20);
    //xAxis.setMax(650);

    tempGraph.addSeries(now);
    tempGraph.addSeries(yesterday);
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


  public static void main(String[] args) {

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
