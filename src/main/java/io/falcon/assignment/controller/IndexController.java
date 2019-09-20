package io.falcon.assignment.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "", produces = MediaType.TEXT_HTML_VALUE)
public class IndexController {

    @GetMapping
    public String index() {
        return "index";
    }
}