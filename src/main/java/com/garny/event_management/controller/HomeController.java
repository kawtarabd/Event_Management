package com.garny.event_management.controller;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
public String homePage() {
        return "home";  // Retourne la vue home.html
    }
}
