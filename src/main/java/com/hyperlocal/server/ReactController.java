package com.hyperlocal.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReactController {
    @RequestMapping(value = "/")
    public String index() {
        return "/index.html";
    }
}