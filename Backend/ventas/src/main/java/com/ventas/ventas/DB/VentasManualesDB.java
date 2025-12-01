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
public class VentasManualesDB {

    private final DataSource dataSource;

    public VentasManualesDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Obtener productos disponibles para venta manual
    public List<Map<String, Object>> obtenerProductosParaVenta() {
        String sql = """
            SELECT 
                id,
                nombre,
                tipo,
                precio_base,
                descripcion
            FROM productos 
            WHERE activo = TRUE
            ORDER BY tipo, nombre
            """;

        List<Map<String, Object>> productos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("id", rs.getInt("id"));
                producto.put("nombre", rs.getString("nombre"));
                producto.put("tipo", rs.getString("tipo"));
                producto.put("precioBase", rs.getDouble("precio_base"));
                producto.put("descripcion", rs.getString("descripcion"));

                productos.add(producto);
            }

            return productos;

        } catch (SQLException e) {
            System.out.println("Error obteniendo productos para venta: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Registrar venta manual
    public Map<String, Object> registrarVentaManual(Integer trabajadorId, List<Map<String, Object>> items, Double total) {
        Connection conn = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 1. Registrar la venta en tabla ventas
            boolean ventaRegistrada = registrarVenta(conn, trabajadorId, items, total);
            if (!ventaRegistrada) {
                resultado.put("success", false);
                resultado.put("message", "Error al registrar la venta");
                conn.rollback();
                return resultado;
            }

            // 2. Commit de la operación
            conn.commit();

            resultado.put("success", true);
            resultado.put("message", "Venta manual registrada exitosamente");
            resultado.put("total", total);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Error registrando venta manual: " + e.getMessage());
            e.printStackTrace();
            resultado.put("success", false);
            resultado.put("message", "Error interno del servidor");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultado;
    }

    private boolean registrarVenta(Connection conn, Integer trabajadorId, List<Map<String, Object>> items, Double total) throws SQLException {
        String sql = """
            INSERT INTO ventas (pedido_id, usuario_id, trabajador_id, total, metodo_pago, tipo_venta, descripcion)
            VALUES (NULL, NULL, ?, ?, 'efectivo', 'normal', ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trabajadorId);
            stmt.setDouble(2, total);

            // Crear descripción detallada de la venta
            String descripcion = crearDescripcionVenta(items);
            stmt.setString(3, descripcion);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private String crearDescripcionVenta(List<Map<String, Object>> items) {
        StringBuilder descripcion = new StringBuilder("Venta manual - ");

        for (Map<String, Object> item : items) {
            String nombre = (String) item.get("nombre");
            Integer cantidad = (Integer) item.get("cantidad");

            // Manejar tanto Integer como Double para precioUnitario
            Double precio;
            Object precioObj = item.get("precioUnitario");
            if (precioObj instanceof Integer) {
                precio = ((Integer) precioObj).doubleValue();
            } else if (precioObj instanceof Double) {
                precio = (Double) precioObj;
            } else {
                precio = 0.0; // Valor por defecto en caso de error
            }

            descripcion.append(cantidad).append("x ").append(nombre)
                    .append(" (Q").append(String.format("%.2f", precio)).append(" c/u), ");
        }

        // Remover la última coma y espacio
        if (descripcion.length() > 2) {
            descripcion.setLength(descripcion.length() - 2);
        }

        return descripcion.toString();
    }
}