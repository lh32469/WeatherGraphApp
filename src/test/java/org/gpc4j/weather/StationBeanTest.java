
package org.gpc4j.weather;


import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.gpc4j.weather.dto.TimeSeries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static org.gpc4j.weather.StationBean.DTF;

@Slf4j
public class StationBeanTest {


  Map<String, Object> observations;
  StationBean stationBean;

  final static private Logger LOG
      = LoggerFactory.getLogger(StationBeanTest.class);


  @BeforeEach
  public void setup() throws IOException {
    LOG.info("StationBeanTest.setup");

    stationBean = new StationBean();

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    InputStream iStream = getClass().getResourceAsStream("/test1.json");
    TimeSeries timeSeries = mapper.readValue(iStream, TimeSeries.class);

    observations = timeSeries.getStation().get(0).getObservations();
  }

  @Test
  public void get24HoursAgo1() {
    ZonedDateTime now =
        ZonedDateTime.parse("2021-02-11T13:50:00-0800", DTF);
    Map<ZonedDateTime, Double> timesToTemps
        = stationBean.getTimesToTemps(observations);

    Optional<Double> temp = stationBean.getTemp24HoursAgo(now, timesToTemps);
    log.info("temp = {}", temp);
    Assertions.assertEquals(46.0, temp.get());
  }

  @Test
  public void sdf() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    URL url = new URL("https://api.mesowest.net/v2/stations/timeseries?stid=e5093&recent=4320&obtimezone=local&complete=1&hfmetars=1&token=d8c6aee36a994f90857925cea26934be");

    TimeSeries timeSeries = mapper.readValue(url, TimeSeries.class);
  }


}
