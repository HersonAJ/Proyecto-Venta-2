package com.ventas.ventas.controllers;


import com.ventas.ventas.DB.PerfilDB;
import com.ventas.ventas.DTOs.Perfil.ActualizarAvatarRequest;
import com.ventas.ventas.DTOs.Perfil.PerfilCompletoResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuario")
public class PerfilController {

    private final PerfilDB perfilDB;

    public PerfilController(PerfilDB perfilDB) {
        this.perfilDB = perfilDB;
    }

    @GetMapping("/perfil-completo")
    public ResponseEntity<?> getPerfilCompleto(Authentication authentication) {
        try {
            String email = authentication.getName();
            PerfilCompletoResponse perfil = perfilDB.obtenerPerfilCompleto(email);

            if (perfil != null) {
                return ResponseEntity.ok(perfil);
            } else {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener perfil");
        }
    }

    @PutMapping("/avatar")
    public ResponseEntity<?> actualizarAvatar(@RequestBody ActualizarAvatarRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();

            // Obtener perfil para tener el ID del usuario
            PerfilCompletoResponse perfil = perfilDB.obtenerPerfilCompleto(email);
            if (perfil == null) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Usuario no encontrado"));
            }

            // Validar que el avatarId esté en rango válido (1-6)
            if (request.getAvatarId() < 1 || request.getAvatarId() > 6) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Avatar ID inválido"));
            }

            // Actualizar avatar
            boolean actualizado = perfilDB.actualizarAvatar(perfil.getId(), request.getAvatarId());

            if (actualizado) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Avatar actualizado correctamente"));
            } else {
                return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al actualizar avatar"));
            }

        } catch (Exception e) {
            System.out.println("ERROR actualizando avatar: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error interno del servidor"));
        }
    }
}