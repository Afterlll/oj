package com.jxy.ojcodesandbox.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangkeyao
 */
@RestController
@RequestMapping("/")
public class Main {

    @RequestMapping("health")
    public String health() {
        return "ok";
    }

}
