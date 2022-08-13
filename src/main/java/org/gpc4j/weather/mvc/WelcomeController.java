package org.gpc4j.weather.mvc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Map default context path to JSF index page.
 */
@Slf4j
@Controller
public class WelcomeController {

  @GetMapping()
  public String homePage(Model model) {
    return "/index.xhtml";
  }

  @GetMapping("/*")
  public String wildCard(HttpServletRequest request) {
    request.setAttribute("STATION", request.getRequestURI().replace("/", ""));
    return "/index.xhtml";
  }

}
