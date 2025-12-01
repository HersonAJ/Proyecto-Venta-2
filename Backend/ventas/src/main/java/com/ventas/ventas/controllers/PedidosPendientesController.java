package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.PedidosPendientesDB;
import com.ventas.ventas.DTOs.Login.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trabajador")
public class PedidosPendientesController {

    private final PedidosPendientesDB pedidosPendientesDB;
    private final JwtUtil jwtUtil;

    public PedidosPendientesController(PedidosPendientesDB pedidosPendientesDB, JwtUtil jwtUtil) {
        this.pedidosPendientesDB = pedidosPendientesDB;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/pedidos-pendientes")
    public ResponseEntity<?> obtenerPedidosPendientes(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Verificar que el usuario sea trabajador o admin
            if (!esTrabajadorOAdmin(authorizationHeader)) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "No tiene permisos para acceder a este recurso"));
            }

            List<Map<String, Object>> pedidos = pedidosPendientesDB.obtenerPedidosPendientes();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "pedidos", pedidos
            ));

        } catch (Exception e) {
            System.out.println("ERROR obteniendo pedidos pendientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error al obtener los pedidos pendientes"
            ));
        }
    }

    private boolean esTrabajadorOAdmin(String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                if (!jwtUtil.validateToken(token)) {
                    return false;
                }

                String rol = jwtUtil.getRoleFromToken(token);
                return "trabajador".equals(rol) || "admin".equals(rol);
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error verificando rol del usuario: " + e.getMessage());
            return false;
        }
    }
}