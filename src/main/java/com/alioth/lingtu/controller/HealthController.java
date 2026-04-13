package com.alioth.lingtu.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检测
 *
 * @author alioth
 */
@RestController
@RequestMapping("health")
public class HealthController {
    @PostMapping("/check")
    public String check() {
        return "OK";
    }
}
