package com.ventas.ventas.DB;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class CrearPedidoDB {

    private final DataSource dataSource;

    public CrearPedidoDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Integer crearPedido(Integer usuarioId, List<Map<String, Object>> items, Double total) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 1. Insertar el pedido principal
            Integer pedidoId = insertarPedido(conn, usuarioId, total);
            if (pedidoId == null) {
                throw new SQLException("Error al crear el pedido principal");
            }

            // 2. Insertar detalles del pedido y personalizaciones
            if (items != null && !items.isEmpty()) {
                procesarItemsPedido(conn, pedidoId, items);
            }

            conn.commit();
            return pedidoId;

        } catch (SQLException e) {
            rollbackTransaction(conn);
            System.out.println("Error creando pedido: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            closeConnection(conn);
        }
    }

    private Integer insertarPedido(Connection conn, Integer usuarioId, Double total) throws SQLException {
        String sql = "INSERT INTO pedidos (usuario_id, total, estado, metodo_pago) VALUES (?, ?, 'pendiente', 'efectivo')";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, usuarioId);
            stmt.setDouble(2, total);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Error al crear pedido, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Error al obtener ID del pedido");
                }
            }
        }
    }

    private void procesarItemsPedido(Connection conn, Integer pedidoId, List<Map<String, Object>> items) throws SQLException {
        String sqlDetalle = "INSERT INTO detalles_pedido (pedido_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        String sqlPersonalizacion = "INSERT INTO personalizaciones_detalle (detalle_pedido_id, ingrediente_id, accion) VALUES (?, ?, 'quitar')";

        try (PreparedStatement stmtDetalle = conn.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtPersonalizacion = conn.prepareStatement(sqlPersonalizacion)) {

            for (Map<String, Object> item : items) {
                Integer productoId = (Integer) item.get("productoId");
                Integer cantidad = (Integer) item.get("cantidad");
                Object precioObj = item.get("precioUnitario");
                Double precioUnitario;
                if (precioObj instanceof Integer) {
                    precioUnitario = ((Integer) precioObj).doubleValue();
                } else if (precioObj instanceof Double) {
                    precioUnitario = (Double) precioObj;
                } else {
                    precioUnitario = 0.0;
                }

                List<Map<String, Object>> personalizaciones = (List<Map<String, Object>>) item.get("personalizaciones");
                List<Integer> ingredientesAQuitar = new ArrayList<>();

                if (personalizaciones != null) {
                    for (Map<String, Object> personalizacion : personalizaciones) {
                        Object ingredienteIdObj = personalizacion.get("ingredienteId");
                        if (ingredienteIdObj instanceof Integer) {
                            ingredientesAQuitar.add((Integer) ingredienteIdObj);
                        }
                    }
                }

                // Insertar detalle del pedido
                stmtDetalle.setInt(1, pedidoId);
                stmtDetalle.setInt(2, productoId);
                stmtDetalle.setInt(3, cantidad);
                stmtDetalle.setDouble(4, precioUnitario);
                stmtDetalle.executeUpdate();

                // Obtener ID del detalle recién insertado
                Integer detallePedidoId = null;
                try (ResultSet generatedKeys = stmtDetalle.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        detallePedidoId = generatedKeys.getInt(1);
                    }
                }

                // Insertar personalizaciones si existen
                if (detallePedidoId != null && !ingredientesAQuitar.isEmpty()) {
                    for (Integer ingredienteId : ingredientesAQuitar) {
                        stmtPersonalizacion.setInt(1, detallePedidoId);
                        stmtPersonalizacion.setInt(2, ingredienteId);
                        stmtPersonalizacion.addBatch();
                    }
                    stmtPersonalizacion.executeBatch();
                }
            }
        }
    }

    private void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}