package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.reportes.ReporteVentasAdminDB;
import com.ventas.ventas.DTOs.Login.JwtUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class ReporteVentasController {

    private final ReporteVentasAdminDB reporteVentasAdminDB;
    private final JwtUtil jwtUtil;

    public ReporteVentasController(ReporteVentasAdminDB reporteVentasAdminDB, JwtUtil jwtUtil) {
        this.reporteVentasAdminDB = reporteVentasAdminDB;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Endpoint para obtener reporte de ventas (para administradores)
     */
    @GetMapping("/reporte")
    public ResponseEntity<?> obtenerReporteVentas(
            @RequestParam(value = "fechaInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioLocal,

            @RequestParam(value = "fechaFin", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinLocal,

            @RequestParam(value = "pagina", required = false) Integer pagina,
            @RequestParam(value = "limite", required = false) Integer limite,

            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            // 1. Verificar y extraer información del token
            Map<String, Object> tokenInfo = validarTokenYExtraerInfo(authorizationHeader);
            if (tokenInfo == null) {
                return crearRespuestaNoAutorizado();
            }

            // 2. Verificar que sea administrador
            String rol = (String) tokenInfo.get("rol");
            if (!"admin".equals(rol)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "No tiene permisos para acceder a este reporte. Solo administradores."
                ));
            }

            // 3. Convertir fechas y validar
            Date fechaInicio = convertirFecha(fechaInicioLocal);
            Date fechaFin = convertirFecha(fechaFinLocal);

            if (fechaInicio != null && fechaFin != null && fechaInicio.after(fechaFin)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "La fecha de inicio no puede ser posterior a la fecha de fin"
                ));
            }

            // 4. Validar paginación
            if (pagina != null && pagina <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "El número de página debe ser mayor a 0"
                ));
            }

            if (limite != null && limite <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "El límite debe ser mayor a 0"
                ));
            }

            // 5. Obtener reporte de ventas
            Map<String, Object> resultado = reporteVentasAdminDB.obtenerReporteVentas(
                    fechaInicio, fechaFin, pagina, limite
            );

            // 6. Devolver respuesta
            return procesarResultado(resultado);

        } catch (Exception e) {
            System.out.println("ERROR obteniendo reporte de ventas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Endpoint para obtener estadísticas de ventas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasVentas(
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            // 1. Verificar y extraer información del token
            Map<String, Object> tokenInfo = validarTokenYExtraerInfo(authorizationHeader);
            if (tokenInfo == null) {
                return crearRespuestaNoAutorizado();
            }

            // 2. Verificar que sea administrador
            String rol = (String) tokenInfo.get("rol");
            if (!"admin".equals(rol)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "No tiene permisos para acceder a las estadísticas. Solo administradores."
                ));
            }

            // 3. Obtener estadísticas
            Map<String, Object> estadisticas = reporteVentasAdminDB.obtenerEstadisticasVentas();

            // 4. Devolver respuesta
            return procesarResultado(estadisticas);

        } catch (Exception e) {
            System.out.println("ERROR obteniendo estadísticas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Endpoint para obtener ventas del día (para trabajadores y admin)
     */
    @GetMapping("/hoy")
    public ResponseEntity<?> obtenerVentasDelDia(
            @RequestParam(value = "pagina", required = false) Integer pagina,
            @RequestParam(value = "limite", required = false) Integer limite,

            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            // 1. Verificar y extraer información del token
            Map<String, Object> tokenInfo = validarTokenYExtraerInfo(authorizationHeader);
            if (tokenInfo == null) {
                return crearRespuestaNoAutorizado();
            }

            // 2. Verificar que sea trabajador o administrador
            String rol = (String) tokenInfo.get("rol");
            if (!"trabajador".equals(rol) && !"admin".equals(rol)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "No tiene permisos para acceder a este reporte. Solo trabajadores o administradores."
                ));
            }

            // 3. Usar fecha actual
            Date fechaHoy = Date.valueOf(LocalDate.now());

            // 4. Obtener ventas del día
            Map<String, Object> resultado = reporteVentasAdminDB.obtenerReporteVentas(
                    fechaHoy, fechaHoy, pagina, limite
            );

            // 5. Agregar mensaje específico
            if (Boolean.TRUE.equals(resultado.get("success"))) {
                resultado.put("mensaje", "Ventas del día " + LocalDate.now());
            }

            // 6. Devolver respuesta
            return procesarResultado(resultado);

        } catch (Exception e) {
            System.out.println("ERROR obteniendo ventas del día: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }


    private Map<String, Object> validarTokenYExtraerInfo(String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authorizationHeader.substring(7);

            // Validar token
            if (!jwtUtil.validateToken(token)) {
                return null;
            }

            // Extraer información del token
            return Map.of(
                    "rol", jwtUtil.getRoleFromToken(token),
                    "userId", jwtUtil.getUserIdFromToken(token),
                    "nombre", jwtUtil.getNombreFromToken(token)
            );

        } catch (Exception e) {
            System.out.println("Error validando token: " + e.getMessage());
            return null;
        }
    }


    private Date convertirFecha(LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }


    private ResponseEntity<?> procesarResultado(Map<String, Object> resultado) {
        if (Boolean.TRUE.equals(resultado.get("success"))) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.status(500).body(resultado);
        }
    }

    private ResponseEntity<?> crearRespuestaNoAutorizado() {
        return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Token inválido o expirado"
        ));
    }
}