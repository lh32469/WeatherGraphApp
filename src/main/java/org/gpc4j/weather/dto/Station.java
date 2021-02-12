package org.gpc4j.weather.dto;

import lombok.Data;
import java.util.Map;


@Data
public class Station {

  String nwsfirezone;
  String elev_dem;
  String timezone;
  String sgid;
  String shortname;
  String elevation;
  String stid;
  Map<String,Object> observations;
  boolean restricted;
  String wims_id;
  String gacc;
  String status;
  Map<String,String> period_of_record;
  String longitude;
  String county;
  String state;
  String cwa;
  String nwszone;
  String id;
  String mnet_id;
  String name;
  String country;
  Map<String,Object> sensor_variables;
  boolean qc_flagged;
  String latitude;

}
