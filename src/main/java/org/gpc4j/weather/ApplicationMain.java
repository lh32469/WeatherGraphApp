package org.gpc4j.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@SpringBootApplication
//@EnableDiscoveryClient
//@RefreshScope
public class ApplicationMain {

  static Logger LOG = LoggerFactory.getLogger(ApplicationMain.class);

  public static void main(String[] args) throws UnknownHostException {
    LOG.info("Starting..");

    // Add hostname to Mapped Diagnostic Context for Logback XML file variables.
    MDC.put("hostname", InetAddress.getLocalHost().getHostName());

    SpringApplication.run(ApplicationMain.class, args);
  }

  @GetMapping(path = "ping")
  public String ping() {
    return "pong\n";
  }


}
