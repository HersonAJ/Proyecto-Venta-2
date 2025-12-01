package com.ventas.ventas.controllers;

import com.ventas.ventas.DB.ProductosDB;
import com.ventas.ventas.DTOs.productos.CrearProductoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductosController {

    private final ProductosDB productosDB;

    public ProductosController(ProductosDB productosDB) {
        this.productosDB = productosDB;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearProducto(@RequestBody CrearProductoRequest request) {
        try {
            // Validaciones básicas
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "El nombre es requerido"));
            }

            if (request.getTipo() == null || (!request.getTipo().equals("Cheveres") && !request.getTipo().equals("Tortas") && !request.getTipo().equals("Tacos"))) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tipo de producto inválido. Use: Cheveres, Tortas o Tacos"));
            }

            if (request.getPrecioBase() == null || request.getPrecioBase() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Precio base inválido"));
            }
            Integer productoId = productosDB.crearProducto(
                    request.getNombre(),
                    request.getTipo(),
                    request.getPrecioBase(),
                    request.getDescripcion(),
                    request.getImagenUrl(),
                    request.getTiempoPreparacion(),
                    request.getIngredientes()
            );

            if (productoId != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Producto creado exitosamente",
                        "productoId", productoId
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error al crear el producto"));
            }

        } catch (Exception e) {
            System.out.println("ERROR creando producto: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error interno del servidor"));
        }
    }
}