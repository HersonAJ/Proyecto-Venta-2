package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.reportes.ReporteUsuariosDB;
import com.ventas.ventas.DTOs.Login.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {

    private final ReporteUsuariosDB reporteUsuariosDB;
    private final JwtUtil jwtUtil;

    public UsuariosController(ReporteUsuariosDB reporteUsuariosDB, JwtUtil jwtUtil) {
        this.reporteUsuariosDB = reporteUsuariosDB;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Endpoint para obtener todos los usuarios (excluye admin id=1)
     * GET /api/usuarios
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodosUsuarios(
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
                        "message", "No tiene permisos para ver usuarios. Solo trabajadores o administradores."
                ));
            }

            // 3. Validar paginación
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

            // 4. Obtener usuarios
            Map<String, Object> resultado = reporteUsuariosDB.obtenerTodosUsuarios(pagina, limite);

            // 5. Devolver respuesta
            return procesarResultado(resultado);

        } catch (Exception e) {
            System.out.println("ERROR obteniendo usuarios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Endpoint para obtener usuarios por rol específico
     * GET /api/usuarios/rol/{rol}
     */
    @GetMapping("/rol/{rol}")
    public ResponseEntity<?> obtenerUsuariosPorRol(
            @PathVariable String rol,
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
            String rolUsuario = (String) tokenInfo.get("rol");
            if (!"trabajador".equals(rolUsuario) && !"admin".equals(rolUsuario)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "No tiene permisos para ver usuarios. Solo trabajadores o administradores."
                ));
            }

            // 3. Validar que el rol solicitado sea válido
            if (!"cliente".equals(rol) && !"trabajador".equals(rol) && !"admin".equals(rol)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Rol no válido. Los roles permitidos son: cliente, trabajador, admin"
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

            // 5. Obtener usuarios por rol
            Map<String, Object> resultado = reporteUsuariosDB.obtenerUsuariosPorRol(rol, pagina, limite);

            // 6. Devolver respuesta
            return procesarResultado(resultado);

        } catch (Exception e) {
            System.out.println("ERROR obteniendo usuarios por rol: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Endpoint para obtener estadísticas de usuarios
     * GET /api/usuarios/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasUsuarios(
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            // 1. Verificar y extraer información del token
            Map<String, Object> tokenInfo = validarTokenYExtraerInfo(authorizationHeader);
            if (tokenInfo == null) {
                return crearRespuestaNoAutorizado();
            }

            // 2. Verificar que sea administrador (solo admin ve estadísticas)
            String rol = (String) tokenInfo.get("rol");
            if (!"admin".equals(rol)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "No tiene permisos para ver estadísticas. Solo administradores."
                ));
            }

            // 3. Obtener solo usuarios con paginación mínima para estadísticas
            Map<String, Object> resultado = reporteUsuariosDB.obtenerTodosUsuarios(1, 1);

            if (Boolean.TRUE.equals(resultado.get("success"))) {
                // Extraer solo las estadísticas
                Map<String, Object> estadisticas = Map.of(
                        "success", true,
                        "totalUsuarios", resultado.get("totalRegistros"),
                        "estadisticasRoles", resultado.get("estadisticasRoles")
                );
                return ResponseEntity.ok(estadisticas);
            } else {
                return ResponseEntity.status(500).body(resultado);
            }

        } catch (Exception e) {
            System.out.println("ERROR obteniendo estadísticas de usuarios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Endpoint para buscar usuario por ID
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(
            @PathVariable Integer id,

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
                        "message", "No tiene permisos para ver usuarios. Solo trabajadores o administradores."
                ));
            }

            // 3. Validar ID
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "ID de usuario inválido"
                ));
            }

            // 4. Obtener todos los usuarios y buscar por ID
            // (Podríamos crear un método específico en DB, pero por simplicidad usamos este enfoque)
            Map<String, Object> resultado = reporteUsuariosDB.obtenerTodosUsuarios(null, null);

            if (Boolean.TRUE.equals(resultado.get("success"))) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> usuarios =
                        (java.util.List<Map<String, Object>>) resultado.get("usuarios");

                // Buscar usuario por ID
                Map<String, Object> usuarioEncontrado = null;
                for (Map<String, Object> usuario : usuarios) {
                    if (id.equals(usuario.get("id"))) {
                        usuarioEncontrado = usuario;
                        break;
                    }
                }

                if (usuarioEncontrado != null) {
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "usuario", usuarioEncontrado
                    ));
                } else {
                    return ResponseEntity.status(404).body(Map.of(
                            "success", false,
                            "message", "Usuario no encontrado"
                    ));
                }
            } else {
                return ResponseEntity.status(500).body(resultado);
            }

        } catch (Exception e) {
            System.out.println("ERROR obteniendo usuario por ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    // ========== MÉTODOS AUXILIARES REUTILIZADOS ==========

    /**
     * Valida el token JWT y extrae información
     */
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

    /**
     * Procesa el resultado de la capa DB
     */
    private ResponseEntity<?> procesarResultado(Map<String, Object> resultado) {
        if (Boolean.TRUE.equals(resultado.get("success"))) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.status(500).body(resultado);
        }
    }

    /**
     * Crea respuesta de no autorizado
     */
    private ResponseEntity<?> crearRespuestaNoAutorizado() {
        return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Token inválido o expirado"
        ));
    }
}