package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.VentasManualesDB;
import com.ventas.ventas.DTOs.Login.JwtUtil;
import com.ventas.ventas.DTOs.productos.VentaManualRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trabajador")
public class VentasManualesController {

    private final VentasManualesDB ventasManualesDB;
    private final JwtUtil jwtUtil;

    public VentasManualesController(VentasManualesDB ventasManualesDB, JwtUtil jwtUtil) {
        this.ventasManualesDB = ventasManualesDB;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/ventas-manuales/productos")
    public ResponseEntity<?> obtenerProductosParaVenta(
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Verificar que el usuario sea trabajador o admin
            Integer trabajadorId = obtenerTrabajadorId(authorizationHeader);
            if (trabajadorId == null) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "No tiene permisos para realizar esta acción"));
            }

            List<Map<String, Object>> productos = ventasManualesDB.obtenerProductosParaVenta();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "productos", productos
            ));

        } catch (Exception e) {
            System.out.println("ERROR obteniendo productos para venta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error interno del servidor"));
        }
    }

    @PostMapping("/ventas-manuales/registrar")
    public ResponseEntity<?> registrarVentaManual(
            @RequestBody VentaManualRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Verificar que el usuario sea trabajador o admin
            Integer trabajadorId = obtenerTrabajadorId(authorizationHeader);
            if (trabajadorId == null) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "No tiene permisos para realizar esta acción"));
            }

            // Validar request
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Debe agregar al menos un producto"));
            }

            if (request.getTotal() == null || request.getTotal() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Total inválido"));
            }

            // Registrar venta manual
            Map<String, Object> resultado = ventasManualesDB.registrarVentaManual(
                    trabajadorId,
                    request.getItems(),
                    request.getTotal()
            );

            if ((Boolean) resultado.get("success")) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", resultado.get("message"),
                        "total", resultado.get("total")
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", resultado.get("message")
                ));
            }

        } catch (Exception e) {
            System.out.println("ERROR registrando venta manual: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error interno del servidor"));
        }
    }

    private Integer obtenerTrabajadorId(String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                if (!jwtUtil.validateToken(token)) {
                    return null;
                }

                String rol = jwtUtil.getRoleFromToken(token);
                if ("trabajador".equals(rol) || "admin".equals(rol)) {
                    return jwtUtil.getUserIdFromToken(token);
                }
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error obteniendo ID del trabajador: " + e.getMessage());
            return null;
        }
    }
}