package com.acheron.inst_bot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({"/", "/app"})
    public String index() {
        return "forward:/index.html";
    }
}
