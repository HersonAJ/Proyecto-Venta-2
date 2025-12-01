package com.ventas.ventas.DB;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Repository
public class PedidosEntregadosDB {

    private final DataSource dataSource;

    public PedidosEntregadosDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> marcarPedidoComoEntregado(Integer pedidoId, Integer trabajadorId) {
        Connection conn = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 1. Obtener información completa del pedido y usuario
            Map<String, Object> infoPedido = obtenerInfoCompletaPedido(conn, pedidoId);
            if (infoPedido == null) {
                resultado.put("success", false);
                resultado.put("message", "Pedido no encontrado");
                return resultado;
            }

            Integer usuarioId = (Integer) infoPedido.get("usuarioId");
            String clienteNombre = (String) infoPedido.get("clienteNombre");
            Double totalPedido = (Double) infoPedido.get("total");

            // 2. Contar productos "Cheveres" en el pedido
            int cheveresEnPedido = contarCheveresEnPedido(conn, pedidoId);

            // 3. Actualizar estado del pedido
            boolean pedidoActualizado = actualizarEstadoPedido(conn, pedidoId);
            if (!pedidoActualizado) {
                resultado.put("success", false);
                resultado.put("message", "No se pudo actualizar el pedido");
                conn.rollback();
                return resultado;
            }

            // 4. Registrar la venta
            boolean ventaRegistrada = registrarVenta(conn, pedidoId, usuarioId, trabajadorId, totalPedido);
            if (!ventaRegistrada) {
                resultado.put("success", false);
                resultado.put("message", "Error al registrar la venta");
                conn.rollback();
                return resultado;
            }

            // 5. Actualizar fidelidad del usuario
            Map<String, Object> fidelidadActualizada = actualizarFidelidad(conn, usuarioId, cheveresEnPedido);

            // 6. Commit de todas las operaciones
            conn.commit();

            resultado.put("success", true);
            resultado.put("message", "Pedido marcado como entregado exitosamente");
            resultado.put("fidelidadActualizada", fidelidadActualizada);
            resultado.put("clienteNombre", clienteNombre);
            resultado.put("ventaRegistrada", true);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Error marcando pedido como entregado: " + e.getMessage());
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
    
    private Map<String, Object> obtenerInfoCompletaPedido(Connection conn, Integer pedidoId) throws SQLException {
        String sql = """
            SELECT p.usuario_id, u.nombre as cliente_nombre, p.total
            FROM pedidos p
            JOIN usuarios u ON p.usuario_id = u.id
            WHERE p.id = ? AND p.estado = 'pendiente'
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> info = new HashMap<>();
                info.put("usuarioId", rs.getInt("usuario_id"));
                info.put("clienteNombre", rs.getString("cliente_nombre"));
                info.put("total", rs.getDouble("total"));
                return info;
            }
        }
        return null;
    }

    private int contarCheveresEnPedido(Connection conn, Integer pedidoId) throws SQLException {
        String sql = """
            SELECT SUM(dp.cantidad) as total_cheveres
            FROM detalles_pedido dp
            JOIN productos p ON dp.producto_id = p.id
            WHERE dp.pedido_id = ? AND p.tipo = 'cheveres'
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total_cheveres");
            }
        }
        return 0;
    }

    private boolean actualizarEstadoPedido(Connection conn, Integer pedidoId) throws SQLException {
        String sql = "UPDATE pedidos SET estado = 'entregado' WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private boolean registrarVenta(Connection conn, Integer pedidoId, Integer usuarioId,
                                   Integer trabajadorId, Double total) throws SQLException {
        String sql = """
            INSERT INTO ventas (pedido_id, usuario_id, trabajador_id, total, metodo_pago, tipo_venta, descripcion)
            VALUES (?, ?, ?, ?, 'efectivo', 'normal', ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            stmt.setInt(2, usuarioId);
            stmt.setInt(3, trabajadorId);
            stmt.setDouble(4, total);

            String descripcion = "Pedido #" + pedidoId + " - Cliente ID: " + usuarioId;
            stmt.setString(5, descripcion);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private Map<String, Object> actualizarFidelidad(Connection conn, Integer usuarioId, int cheveresEnPedido) throws SQLException {
        String sqlSelect = "SELECT promocion_actual, promociones_pendientes FROM fidelidad WHERE usuario_id = ?";
        String sqlInsert = "INSERT INTO fidelidad (usuario_id, hotdogs_comprados, puntos_acumulados, promocion_actual, promociones_pendientes) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdate = "UPDATE fidelidad SET hotdogs_comprados = hotdogs_comprados + ?, puntos_acumulados = puntos_acumulados + ?, promocion_actual = ?, promociones_pendientes = ? WHERE usuario_id = ?";

        int promocionActual = 0;
        int promocionesPendientes = 0;
        boolean existeFidelidad = false;

        // Verificar si existe registro
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                promocionActual = rs.getInt("promocion_actual");
                promocionesPendientes = rs.getInt("promociones_pendientes");
                existeFidelidad = true;
            }
        }

        int totalPuntos = cheveresEnPedido;

        // Calcular nuevas promociones ganadas en ESTE pedido
        int totalAcumulado = promocionActual + totalPuntos;
        int nuevasPromociones = totalAcumulado / 7;
        int nuevoPromocionActual = totalAcumulado % 7;
        int totalPromocionesPendientes = promocionesPendientes + nuevasPromociones;

        // Insertar o actualizar
        if (!existeFidelidad && totalPuntos > 0) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                stmt.setInt(1, usuarioId);
                stmt.setInt(2, totalPuntos);
                stmt.setInt(3, totalPuntos);
                stmt.setInt(4, nuevoPromocionActual);
                stmt.setInt(5, totalPromocionesPendientes);
                stmt.executeUpdate();
            }
        } else if (existeFidelidad && totalPuntos > 0) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setInt(1, totalPuntos);
                stmt.setInt(2, totalPuntos);
                stmt.setInt(3, nuevoPromocionActual);
                stmt.setInt(4, totalPromocionesPendientes);
                stmt.setInt(5, usuarioId);
                stmt.executeUpdate();
            }
        }

        // Registrar en historial si ganó promociones
        if (nuevasPromociones > 0) {
            registrarPromocionesGanadas(conn, usuarioId, nuevasPromociones);
        }

        Map<String, Object> fidelidad = new HashMap<>();
        fidelidad.put("promocionActual", nuevoPromocionActual);
        fidelidad.put("promocionesPendientes", totalPromocionesPendientes);
        fidelidad.put("nuevasPromocionesGanadas", nuevasPromociones);
        fidelidad.put("metaPromocion", 7);
        fidelidad.put("cheveresEnEstePedido", cheveresEnPedido);

        return fidelidad;
    }

    private void registrarPromocionesGanadas(Connection conn, Integer usuarioId, int nuevasPromociones) throws SQLException {
        String sql = "INSERT INTO historial_promociones (usuario_id, tipo, descripcion) VALUES (?, 'hotdog_gratis', ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < nuevasPromociones; i++) {
                stmt.setInt(1, usuarioId);
                stmt.setString(2, "Promoción ganada por acumulación de 7 Cheveres");
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}