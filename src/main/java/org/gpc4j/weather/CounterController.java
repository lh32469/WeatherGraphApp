package org.gpc4j.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@Scope(value = "prototype")
@Slf4j
public class CounterController {

  @GetMapping("/count")
  public String count() {
    return this + " 0";
  }

  @GetMapping("/echo")
  public String echo(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();

    sb.append("Remote Address: ");
    sb.append(request.getRemoteAddr());
    sb.append("\n");

    sb.append("Remote Host: ");
    sb.append(request.getRemoteHost());
    sb.append("\n");

    sb.append("Remote Port: ");
    sb.append(request.getRemotePort());
    sb.append("\n");

    sb.append("X-FORWARDED-FOR: ");
    sb.append(request.getHeader("X-FORWARDED-FOR"));
    sb.append("\n");

    log.info(sb.toString());
    return sb.toString();
  }

}
