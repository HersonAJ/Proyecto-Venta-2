package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.CanjearPromocionDB;
import com.ventas.ventas.DTOs.Login.JwtUtil;
import com.ventas.ventas.DTOs.Perfil.CanjearPromocionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trabajador")
public class CanjearPromocionController {

    private final CanjearPromocionDB canjearPromocionDB;
    private final JwtUtil jwtUtil;

    public CanjearPromocionController(CanjearPromocionDB canjearPromocionDB, JwtUtil jwtUtil) {
        this.canjearPromocionDB = canjearPromocionDB;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/promociones/canjear")
    public ResponseEntity<?> canjearPromocion(
            @RequestBody CanjearPromocionRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Verificar que el usuario sea trabajador o admin
            if (!esTrabajadorOAdmin(authorizationHeader)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "No tiene permisos para realizar esta acción"
                ));
            }

            // Validar request
            if (request.getUsuarioId() == null || request.getUsuarioId() <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "ID de usuario inválido"
                ));
            }

            if (request.getCantidad() == null || request.getCantidad() <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Cantidad inválida"
                ));
            }

            // Canjear promoción
            Map<String, Object> resultado = canjearPromocionDB.canjearPromocion(
                    request.getUsuarioId(),
                    request.getCantidad()
            );

            if ((Boolean) resultado.get("success")) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", resultado.get("message"),
                        "promocionesRestantes", resultado.get("promocionesRestantes"),
                        "nombreUsuario", resultado.get("nombreUsuario"),
                        "cantidadCanjeada", resultado.get("cantidadCanjeada")
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", resultado.get("message")
                ));
            }

        } catch (Exception e) {
            System.out.println("ERROR canjeando promoción: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    @GetMapping("/promociones/consultar/{usuarioId}")
    public ResponseEntity<?> consultarPromocionesUsuario(
            @PathVariable Integer usuarioId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Verificar que el usuario sea trabajador o admin
            if (!esTrabajadorOAdmin(authorizationHeader)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "No tiene permisos para realizar esta acción"
                ));
            }

            // Validar usuarioId
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "ID de usuario inválido"
                ));
            }

            // Consultar promociones
            Map<String, Object> resultado = canjearPromocionDB.consultarPromocionesUsuario(usuarioId);

            if ((Boolean) resultado.get("success")) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "nombre", resultado.get("nombre"),
                        "email", resultado.get("email"),
                        "promocionesPendientes", resultado.get("promocionesPendientes"),
                        "promocionesCanjeadas", resultado.get("promocionesCanjeadas"),
                        "promocionActual", resultado.get("promocionActual")
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", resultado.get("message")
                ));
            }

        } catch (Exception e) {
            System.out.println("ERROR consultando promociones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
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

    @GetMapping("/promociones/buscar")
    public ResponseEntity<?> buscarUsuariosPorNombre(
            @RequestParam String nombre,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Verificar que el usuario sea trabajador o admin
            if (!esTrabajadorOAdmin(authorizationHeader)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "No tiene permisos para realizar esta acción"
                ));
            }

            // Validar nombre
            if (nombre == null || nombre.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Nombre de búsqueda inválido"
                ));
            }

            // Buscar usuarios por nombre
            Map<String, Object> resultado = canjearPromocionDB.consultarPromocionesPorNombre(nombre.trim());

            if ((Boolean) resultado.get("success")) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "usuarios", resultado.get("usuarios"),
                        "totalEncontrados", resultado.get("totalEncontrados")
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", resultado.get("message")
                ));
            }

        } catch (Exception e) {
            System.out.println("ERROR buscando usuarios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }
}
