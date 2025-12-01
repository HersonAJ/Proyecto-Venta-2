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
public class PedidosPendientesDB {

    private final DataSource dataSource;

    public PedidosPendientesDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Map<String, Object>> obtenerPedidosPendientes() {
        String sql = """
            SELECT 
                p.id as pedido_id,
                p.fecha_pedido,
                p.total,
                p.metodo_pago,
                u.nombre as cliente_nombre,
                u.telefono as cliente_telefono
            FROM pedidos p
            JOIN usuarios u ON p.usuario_id = u.id
            WHERE p.estado = 'pendiente'
            ORDER BY p.fecha_pedido ASC
            """;

        List<Map<String, Object>> pedidos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> pedido = new HashMap<>();
                pedido.put("id", rs.getInt("pedido_id"));
                pedido.put("fechaPedido", rs.getTimestamp("fecha_pedido"));
                pedido.put("total", rs.getDouble("total"));
                pedido.put("metodoPago", rs.getString("metodo_pago"));
                pedido.put("clienteNombre", rs.getString("cliente_nombre"));
                pedido.put("clienteTelefono", rs.getString("cliente_telefono"));

                // Obtener detalles específicos del pedido (qué hay que preparar)
                List<Map<String, Object>> detalles = obtenerDetallesPedido(conn, rs.getInt("pedido_id"));
                pedido.put("detalles", detalles);

                pedidos.add(pedido);
            }

            return pedidos;

        } catch (SQLException e) {
            System.out.println("Error obteniendo pedidos pendientes: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> obtenerDetallesPedido(Connection conn, Integer pedidoId) throws SQLException {
        String sql = """
            SELECT 
                dp.id as detalle_id,
                p.nombre as producto_nombre,
                p.tipo as producto_tipo,
                dp.cantidad,
                dp.precio_unitario,
                (dp.cantidad * dp.precio_unitario) as subtotal,
                GROUP_CONCAT(
                    CASE 
                        WHEN pd.accion = 'quitar' THEN CONCAT('Sin ', i.nombre)
                        ELSE i.nombre
                    END SEPARATOR ', '
                ) as personalizaciones
            FROM detalles_pedido dp
            JOIN productos p ON dp.producto_id = p.id
            LEFT JOIN personalizaciones_detalle pd ON dp.id = pd.detalle_pedido_id
            LEFT JOIN ingredientes i ON pd.ingrediente_id = i.id
            WHERE dp.pedido_id = ?
            GROUP BY dp.id, p.nombre, p.tipo, dp.cantidad, dp.precio_unitario
            ORDER BY p.tipo, p.nombre
            """;

        List<Map<String, Object>> detalles = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("id", rs.getInt("detalle_id"));
                detalle.put("productoNombre", rs.getString("producto_nombre"));
                detalle.put("productoTipo", rs.getString("producto_tipo"));
                detalle.put("cantidad", rs.getInt("cantidad"));
                detalle.put("precioUnitario", rs.getDouble("precio_unitario"));
                detalle.put("subtotal", rs.getDouble("subtotal"));

                String personalizaciones = rs.getString("personalizaciones");
                detalle.put("personalizaciones", personalizaciones != null ? personalizaciones : "Sin personalizaciones");

                detalles.add(detalle);
            }
        }
        return detalles;
    }
}
