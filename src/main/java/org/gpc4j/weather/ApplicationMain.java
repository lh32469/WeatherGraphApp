package org.gpc4j.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class ApplicationMain {

  static Logger LOG = LoggerFactory.getLogger(ApplicationMain.class);

  public static void main(String[] args) {
    LOG.info("Starting..");
    SpringApplication.run(ApplicationMain.class, args);
  }
}
