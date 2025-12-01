package com.ventas.ventas.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hola")
public class prueba {

    @GetMapping
    public String hola(){
        return "hola";
    }
}
