package org.graphwalker.studio;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StudioController {

  @RequestMapping("/")
  public String index() {
    return "index";
  }
}
