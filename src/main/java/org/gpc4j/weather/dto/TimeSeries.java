package org.gpc4j.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeSeries {

  private Map<String,String> units;
  private Map<String,Object> qc_summary;
  private List<Station> station;
  private Map<String,Object> summary;

}
