package org.gpc4j.weather.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Map default context path to JSF index page.
 */
@Controller
public class WelcomeController {

  @GetMapping("/")
  public String homePage(Model model) {
    return "index.xhtml";
  }

}
