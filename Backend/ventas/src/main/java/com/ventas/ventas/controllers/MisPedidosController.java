package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.MisPedidosDB;
import com.ventas.ventas.DTOs.Login.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class MisPedidosController {

    private final MisPedidosDB misPedidosDB;
    private final JwtUtil jwtUtil;

    public MisPedidosController(MisPedidosDB misPedidosDB, JwtUtil jwtUtil) {
        this.misPedidosDB = misPedidosDB;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> obtenerMisPedidos(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            Integer usuarioId = extraerUserIdDeToken(authorizationHeader);

            if (usuarioId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Usuario no autenticado"));
            }

            List<Map<String, Object>> pedidos = misPedidosDB.obtenerPedidosActivos(usuarioId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "pedidos", pedidos
            ));

        } catch (Exception e) {
            System.out.println("ERROR obteniendo pedidos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error al obtener los pedidos"
            ));
        }
    }

    private Integer extraerUserIdDeToken(String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                if (!jwtUtil.validateToken(token)) {
                    return null;
                }
                return jwtUtil.getUserIdFromToken(token);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error extrayendo userId del token: " + e.getMessage());
            return null;
        }
    }
}
