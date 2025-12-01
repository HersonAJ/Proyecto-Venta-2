package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.RegistroDB;
import com.ventas.ventas.modelos.Usuario;
import com.ventas.ventas.DTOs.Login.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RegistroController {

    private final RegistroDB registroDB;
    private final JwtUtil jwtUtil;

    public RegistroController(RegistroDB registroDB, JwtUtil jwtUtil) {
        this.registroDB = registroDB;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {

        // Validaciones básicas
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(crearError("El nombre es requerido"));
        }

        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(crearError("El email es requerido"));
        }

        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(crearError("La contraseña debe tener al menos 6 caracteres"));
        }

        if (!usuario.getAceptaTerminos()) {
            return ResponseEntity.badRequest().body(crearError("Debe aceptar los términos y condiciones"));
        }

        // Verificar si el usuario ya existe
        if (registroDB.existeUsuario(usuario.getEmail())) {
            return ResponseEntity.badRequest().body(crearError("El correo electrónico ya está registrado"));
        }

        // Registrar usuario
        String jwtToken = registroDB.registrarUsuarioYGenerarToken(usuario, jwtUtil);

        if (jwtToken != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario registrado y autenticado exitosamente");
            response.put("token", jwtToken);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(crearError("Error al registrar el usuario"));
        }
    }

    private Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", mensaje);
        return error;
    }
}