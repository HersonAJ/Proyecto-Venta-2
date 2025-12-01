package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.CrearPedidoDB;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class CrearPedidoController {

    private final CrearPedidoDB crearPedidoDB;

    public CrearPedidoController(CrearPedidoDB crearPedidoDB) {
        this.crearPedidoDB = crearPedidoDB;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearPedido(@RequestBody CrearPedidoRequest request) {
        try {
            if (request.getUsuarioId() == null || request.getUsuarioId() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ID de usuario inválido"));
            }

            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "El pedido debe contener al menos un producto"));
            }

            if (request.getTotal() == null || request.getTotal() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Total inválido"));
            }
            Integer pedidoId = crearPedidoDB.crearPedido(
                    request.getUsuarioId(),
                    request.getItems(),
                    request.getTotal()
            );

            if (pedidoId != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Pedido creado exitosamente",
                        "pedidoId", pedidoId
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al crear el pedido"));
            }

        } catch (Exception e) {
            System.out.println("ERROR creando pedido: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error interno del servidor"));
        }
    }

    // Clase interna para el request
    public static class CrearPedidoRequest {
        private Integer usuarioId;
        private List<Map<String, Object>> items;
        private Double total;

        // Getters y Setters
        public Integer getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

        public List<Map<String, Object>> getItems() { return items; }
        public void setItems(List<Map<String, Object>> items) { this.items = items; }

        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
    }
}
