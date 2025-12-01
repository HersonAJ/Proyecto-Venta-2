package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.MenuDB;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuDB menuDB;

    public MenuController(MenuDB menuDB) {
        this.menuDB = menuDB;
    }

    @GetMapping("/obtener")
    public ResponseEntity<?> obtenerMenu() {
        try {
            List<Map<String, Object>> menu = menuDB.obtenerMenuCompleto();

            return ResponseEntity.ok(Map.of("success", true, "menu", menu));
        } catch (Exception e) {
            System.out.println("Error obteniendo menu:" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al obtener menu"));
        }
    }
}
