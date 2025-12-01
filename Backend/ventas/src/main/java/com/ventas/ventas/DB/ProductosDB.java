package com.ventas.ventas.DB;

import com.ventas.ventas.DTOs.productos.IngredienteRequest;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;

@Repository
public class ProductosDB {

    private final DataSource dataSource;

    public ProductosDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Integer crearProducto(String nombre, String tipo, Double precioBase,
                                 String descripcion, String imagenUrl,
                                 Integer tiempoPreparacion, List<IngredienteRequest> ingredientes) {

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 1. Insertar el producto
            Integer productoId = insertarProducto(conn, nombre, tipo, precioBase, descripcion, imagenUrl, tiempoPreparacion);
            if (productoId == null) {
                throw new SQLException("Error al crear producto");
            }

            // 2. Procesar ingredientes
            if (ingredientes != null && !ingredientes.isEmpty()) {
                procesarIngredientes(conn, productoId, ingredientes);
            }

            conn.commit();
            return productoId;

        } catch (SQLException e) {
            rollbackTransaction(conn);
            System.out.println("Error creando producto: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            closeConnection(conn);
        }
    }

    private Integer insertarProducto(Connection conn, String nombre, String tipo, Double precioBase,
                                     String descripcion, String imagenUrl, Integer tiempoPreparacion) throws SQLException {
        String sql = "INSERT INTO productos (nombre, tipo, descripcion, precio_base, imagen_url, tiempo_preparacion) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, tipo);
            stmt.setString(3, descripcion != null ? descripcion : "");
            stmt.setDouble(4, precioBase);
            stmt.setString(5, imagenUrl != null ? imagenUrl : "");
            stmt.setInt(6, tiempoPreparacion != null ? tiempoPreparacion : 10);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Error al crear producto, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Error al obtener ID del producto");
                }
            }
        }
    }

    private void procesarIngredientes(Connection conn, Integer productoId, List<IngredienteRequest> ingredientes) throws SQLException {
        String sqlIngrediente = "INSERT INTO ingredientes (nombre, tipo, precio_adicional) VALUES (?, 'base', 0.00)";
        String sqlRelacion = "INSERT INTO producto_ingredientes (producto_id, ingrediente_id, es_obligatorio) VALUES (?, ?, ?)";

        try (PreparedStatement stmtIngrediente = conn.prepareStatement(sqlIngrediente, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtRelacion = conn.prepareStatement(sqlRelacion)) {

            for ( IngredienteRequest ingrediente : ingredientes) {
                String nombreIngrediente = (String) ingrediente.getNombre();
                Boolean esObligatorio = (Boolean) ingrediente.getEsObligatorio();

                // Crear ingrediente o obtener existente
                Integer ingredienteId = crearObtenerIngrediente(conn, stmtIngrediente, nombreIngrediente);

                if (ingredienteId != null) {
                    // Relacionar ingrediente con producto
                    stmtRelacion.setInt(1, productoId);
                    stmtRelacion.setInt(2, ingredienteId);
                    stmtRelacion.setBoolean(3, esObligatorio != null ? esObligatorio : false);
                    stmtRelacion.addBatch();
                }
            }
            stmtRelacion.executeBatch();
        }
    }

    private Integer crearObtenerIngrediente(Connection conn, PreparedStatement stmtIngrediente, String nombre) throws SQLException {
        // Primero verificar si ya existe
        String sqlBuscar = "SELECT id FROM ingredientes WHERE nombre = ? AND tipo = 'base'";
        try (PreparedStatement stmtBuscar = conn.prepareStatement(sqlBuscar)) {
            stmtBuscar.setString(1, nombre);
            ResultSet rs = stmtBuscar.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // Si no existe, crearlo
        stmtIngrediente.setString(1, nombre);
        int affectedRows = stmtIngrediente.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Error al crear ingrediente: " + nombre);
        }

        try (ResultSet generatedKeys = stmtIngrediente.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Error al obtener ID del ingrediente: " + nombre);
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