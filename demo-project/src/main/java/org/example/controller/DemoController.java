package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description
 * @author wulin
 * @create 2021/8/3 10:26
 */
@RestController
public class DemoController {

    @GetMapping("demo")
    public String demo() {
        return "demo project test";
    }

}
