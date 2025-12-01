package com.ventas.ventas.DB;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MenuDB {

    private final DataSource dataSource;

    public MenuDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Map<String, Object>> obtenerMenuCompleto() {
        String sql = """
            SELECT 
                p.id,
                p.nombre,
                p.tipo,
                p.precio_base,
                p.imagen_url,
                p.descripcion,
                i.id as ingrediente_id,
                i.nombre as ingrediente_nombre,
                pi.es_obligatorio
            FROM productos p
            LEFT JOIN producto_ingredientes pi ON p.id = pi.producto_id
            LEFT JOIN ingredientes i ON pi.ingrediente_id = i.id
            WHERE p.activo = TRUE
            ORDER BY p.tipo, p.nombre, i.nombre
            """;

        List<Map<String, Object>> menu = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Map<Integer, Map<String, Object>> productosMap = new HashMap<>();

            while (rs.next()) {
                Integer productoId = rs.getInt("id");

                // Crear producto si no existe
                if (!productosMap.containsKey(productoId)) {
                    Map<String, Object> producto = new HashMap<>();
                    producto.put("id", productoId);
                    producto.put("nombre", rs.getString("nombre"));
                    producto.put("tipo", rs.getString("tipo"));
                    producto.put("precioBase", rs.getDouble("precio_base"));
                    producto.put("imagenUrl", rs.getString("imagen_url"));
                    producto.put("descripcion", rs.getString("descripcion"));
                    producto.put("ingredientes", new ArrayList<Map<String, Object>>());

                    productosMap.put(productoId, producto);
                    menu.add(producto);
                }

                // Agregar ingrediente si existe
                Map<String, Object> producto = productosMap.get(productoId);
                List<Map<String, Object>> ingredientes =
                        (List<Map<String, Object>>) producto.get("ingredientes");

                if (rs.getObject("ingrediente_id") != null) {
                    Map<String, Object> ingrediente = new HashMap<>();
                    ingrediente.put("id", rs.getInt("ingrediente_id"));
                    ingrediente.put("nombre", rs.getString("ingrediente_nombre"));
                    ingrediente.put("esObligatorio", rs.getBoolean("es_obligatorio"));

                    ingredientes.add(ingrediente);
                }
            }

            return menu;

        } catch (SQLException e) {
            System.out.println("Error obteniendo menú: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
