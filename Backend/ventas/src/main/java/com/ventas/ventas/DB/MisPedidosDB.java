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
public class MisPedidosDB {

    private final DataSource dataSource;

    public MisPedidosDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Map<String, Object>> obtenerPedidosActivos(Integer usuarioId) {
        String sql = """
        SELECT 
            p.id,
            p.estado,
            p.total,
            p.fecha_pedido,
            p.metodo_pago
        FROM pedidos p
        WHERE p.usuario_id = ? 
        AND p.estado IN ('pendiente', 'en_preparacion', 'listo')
        ORDER BY p.fecha_pedido DESC
        """;

        List<Map<String, Object>> pedidos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> pedido = new HashMap<>();
                pedido.put("id", rs.getInt("id"));
                pedido.put("estado", rs.getString("estado"));
                pedido.put("total", rs.getDouble("total"));
                pedido.put("fechaPedido", rs.getTimestamp("fecha_pedido"));
                pedido.put("metodoPago", rs.getString("metodo_pago"));

                // Obtener detalles específicos del pedido
                List<Map<String, Object>> detalles = obtenerDetallesPedido(conn, rs.getInt("id"));
                pedido.put("detalles", detalles);
                pedido.put("cantidadItems", detalles.size()); // Total de items diferentes

                pedidos.add(pedido);
            }

            return pedidos;

        } catch (SQLException e) {
            System.out.println("Error obteniendo pedidos activos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> obtenerDetallesPedido(Connection conn, Integer pedidoId) throws SQLException {
        String sql = """
        SELECT 
            p.nombre as producto_nombre,
            dp.cantidad,
            dp.precio_unitario,
            (dp.cantidad * dp.precio_unitario) as subtotal
        FROM detalles_pedido dp
        JOIN productos p ON dp.producto_id = p.id
        WHERE dp.pedido_id = ?
        """;

        List<Map<String, Object>> detalles = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("productoNombre", rs.getString("producto_nombre"));
                detalle.put("cantidad", rs.getInt("cantidad"));
                detalle.put("precioUnitario", rs.getDouble("precio_unitario"));
                detalle.put("subtotal", rs.getDouble("subtotal"));

                detalles.add(detalle);
            }
        }
        return detalles;
    }
}