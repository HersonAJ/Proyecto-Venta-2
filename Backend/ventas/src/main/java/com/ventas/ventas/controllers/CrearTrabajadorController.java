package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.CrearTrabajadorDB;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class CrearTrabajadorController {

    private final CrearTrabajadorDB crearTrabajadorDB;

    public CrearTrabajadorController(CrearTrabajadorDB crearTrabajadorDB) {
        this.crearTrabajadorDB = crearTrabajadorDB;
    }

    @PostMapping("/crear-trabajador")
    public ResponseEntity<?> crearTrabajador(@RequestBody CrearTrabajadorRequest request) {
        try {
            // Validaciones
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "El nombre es requerido"));
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "El email es requerido"));
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "La contraseña debe tener al menos 6 caracteres"));
            }

            // Verificar si el email ya existe
            if (crearTrabajadorDB.existeUsuario(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "El correo electrónico ya está registrado"));
            }

            // Crear trabajador
            boolean creado = crearTrabajadorDB.crearTrabajador(
                    request.getNombre(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getTelefono()
            );

            if (creado) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Trabajador creado exitosamente"));
            } else {
                return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al crear el trabajador"));
            }

        } catch (Exception e) {
            System.out.println("ERROR creando trabajador: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error interno del servidor"));
        }
    }

    public static class CrearTrabajadorRequest {
        private String nombre;
        private String email;
        private String password;
        private String telefono;

        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
    }
}
