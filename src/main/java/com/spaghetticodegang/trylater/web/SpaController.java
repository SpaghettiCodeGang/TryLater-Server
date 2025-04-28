package com.spaghetticodegang.trylater.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {"/{path:[^.]*}" })
    public String forward(@PathVariable String path) {
        return "forward:/index.html";
    }
}