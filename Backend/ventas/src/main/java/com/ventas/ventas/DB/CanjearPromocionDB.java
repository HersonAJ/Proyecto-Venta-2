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
public class CanjearPromocionDB {

    private final DataSource dataSource;

    public CanjearPromocionDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> canjearPromocion(Integer usuarioId, Integer cantidad) {
        Connection conn = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 1. Verificar que el usuario existe y tiene promociones suficientes
            Map<String, Object> infoUsuario = obtenerInfoUsuarioYFidelidad(conn, usuarioId);
            if (infoUsuario == null) {
                resultado.put("success", false);
                resultado.put("message", "Usuario no encontrado");
                return resultado;
            }

            int promocionesPendientes = (int) infoUsuario.get("promocionesPendientes");
            String nombreUsuario = (String) infoUsuario.get("nombreUsuario");

            // 2. Validar que tiene suficientes promociones
            if (promocionesPendientes < cantidad) {
                resultado.put("success", false);
                resultado.put("message",
                        String.format("El usuario solo tiene %d promoción(es) disponible(s)", promocionesPendientes));
                return resultado;
            }

            // 3. Actualizar fidelidad - reducir promociones_pendientes
            boolean actualizado = actualizarPromocionesPendientes(conn, usuarioId, promocionesPendientes - cantidad);
            if (!actualizado) {
                resultado.put("success", false);
                resultado.put("message", "Error al actualizar las promociones");
                conn.rollback();
                return resultado;
            }

            // 4. Incrementar contador de promociones canjeadas
            incrementarPromocionesCanjeadas(conn, usuarioId, cantidad);

            // 5. Registrar en historial de promociones
            registrarEnHistorial(conn, usuarioId, cantidad);

            // 6. Commit de todas las operaciones
            conn.commit();

            resultado.put("success", true);
            resultado.put("message",
                    String.format("Se canjearon %d promoción(es) para %s", cantidad, nombreUsuario));
            resultado.put("promocionesRestantes", promocionesPendientes - cantidad);
            resultado.put("nombreUsuario", nombreUsuario);
            resultado.put("cantidadCanjeada", cantidad);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Error canjeando promoción: " + e.getMessage());
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

    private Map<String, Object> obtenerInfoUsuarioYFidelidad(Connection conn, Integer usuarioId) throws SQLException {
        String sql = """
            SELECT u.nombre, COALESCE(f.promociones_pendientes, 0) as promociones_pendientes
            FROM usuarios u
            LEFT JOIN fidelidad f ON u.id = f.usuario_id
            WHERE u.id = ? AND u.activo = true
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> info = new HashMap<>();
                info.put("nombreUsuario", rs.getString("nombre"));
                info.put("promocionesPendientes", rs.getInt("promociones_pendientes"));
                return info;
            }
        }
        return null;
    }

    private boolean actualizarPromocionesPendientes(Connection conn, Integer usuarioId, int nuevasPromocionesPendientes) throws SQLException {
        // Primero verificar si existe registro en fidelidad
        String sqlCheck = "SELECT COUNT(*) FROM fidelidad WHERE usuario_id = ?";
        String sqlUpdate = "UPDATE fidelidad SET promociones_pendientes = ? WHERE usuario_id = ?";
        String sqlInsert = "INSERT INTO fidelidad (usuario_id, promociones_pendientes) VALUES (?, ?)";

        boolean existeFidelidad = false;

        try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                existeFidelidad = rs.getInt(1) > 0;
            }
        }

        if (existeFidelidad) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setInt(1, nuevasPromocionesPendientes);
                stmt.setInt(2, usuarioId);
                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        } else {
            // Si no existe registro, crear uno
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                stmt.setInt(1, usuarioId);
                stmt.setInt(2, nuevasPromocionesPendientes);
                int affectedRows = stmt.executeUpdate();
                return affectedRows > 0;
            }
        }
    }

    private void incrementarPromocionesCanjeadas(Connection conn, Integer usuarioId, int cantidad) throws SQLException {
        String sqlUpdate = "UPDATE fidelidad SET promociones_canjeadas = COALESCE(promociones_canjeadas, 0) + ? WHERE usuario_id = ?";
        String sqlInsert = "INSERT INTO fidelidad (usuario_id, promociones_canjeadas) VALUES (?, ?)";

        // Verificar si existe
        String sqlCheck = "SELECT COUNT(*) FROM fidelidad WHERE usuario_id = ?";
        boolean existeFidelidad = false;

        try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                existeFidelidad = rs.getInt(1) > 0;
            }
        }

        if (existeFidelidad) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setInt(1, cantidad);
                stmt.setInt(2, usuarioId);
                stmt.executeUpdate();
            }
        } else {
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                stmt.setInt(1, usuarioId);
                stmt.setInt(2, cantidad);
                stmt.executeUpdate();
            }
        }
    }

    private void registrarEnHistorial(Connection conn, Integer usuarioId, int cantidad) throws SQLException {
        String sql = "INSERT INTO historial_promociones (usuario_id, tipo, descripcion) VALUES (?, 'hotdog_gratis', ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < cantidad; i++) {
                stmt.setInt(1, usuarioId);
                stmt.setString(2, "Promoción canjeada - Hot Dog Gratis");
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public Map<String, Object> consultarPromocionesPorNombre(String nombre) {
        Map<String, Object> resultado = new HashMap<>();

        String sql = """
        SELECT u.id, u.nombre, u.email, 
               COALESCE(f.promociones_pendientes, 0) as promociones_pendientes,
               COALESCE(f.promociones_canjeadas, 0) as promociones_canjeadas,
               COALESCE(f.promocion_actual, 0) as promocion_actual
        FROM usuarios u
        LEFT JOIN fidelidad f ON u.id = f.usuario_id
        WHERE u.activo = true 
        AND (LOWER(REPLACE(REPLACE(REPLACE(u.nombre, 'á', 'a'), 'é', 'e'), 'í', 'i')) LIKE LOWER(?)
             OR LOWER(u.nombre) LIKE LOWER(?))
        LIMIT 10
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 🟡 CAMBIO: Preparar parámetro de búsqueda
            String parametroBusqueda = "%" + nombre.trim() + "%";
            stmt.setString(1, parametroBusqueda);
            stmt.setString(2, parametroBusqueda);

            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> usuarios = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("id", rs.getInt("id"));
                usuario.put("nombre", rs.getString("nombre"));
                usuario.put("email", rs.getString("email"));
                usuario.put("promocionesPendientes", rs.getInt("promociones_pendientes"));
                usuario.put("promocionesCanjeadas", rs.getInt("promociones_canjeadas"));
                usuario.put("promocionActual", rs.getInt("promocion_actual"));
                usuarios.add(usuario);
            }

            if (!usuarios.isEmpty()) {
                resultado.put("success", true);
                resultado.put("usuarios", usuarios);
                resultado.put("totalEncontrados", usuarios.size());
            } else {
                resultado.put("success", false);
                resultado.put("message", "No se encontraron usuarios con ese nombre");
            }

        } catch (SQLException e) {
            resultado.put("success", false);
            resultado.put("message", "Error consultando promociones");
        }
        return resultado;
    }

    public Map<String, Object> consultarPromocionesUsuario(Integer usuarioId) {
        Map<String, Object> resultado = new HashMap<>();
        String sql = """
        SELECT u.id, u.nombre, u.email, 
               COALESCE(f.promociones_pendientes, 0) as promociones_pendientes,
               COALESCE(f.promociones_canjeadas, 0) as promociones_canjeadas,
               COALESCE(f.promocion_actual, 0) as promocion_actual
        FROM usuarios u
        LEFT JOIN fidelidad f ON u.id = f.usuario_id
        WHERE u.id = ? AND u.activo = true
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                resultado.put("success", true);
                resultado.put("id", rs.getInt("id"));
                resultado.put("nombre", rs.getString("nombre"));
                resultado.put("email", rs.getString("email"));
                resultado.put("promocionesPendientes", rs.getInt("promociones_pendientes"));
                resultado.put("promocionesCanjeadas", rs.getInt("promociones_canjeadas"));
                resultado.put("promocionActual", rs.getInt("promocion_actual"));
            } else {
                resultado.put("success", false);
                resultado.put("message", "Usuario no encontrado");
            }
        } catch (SQLException e) {
            resultado.put("success", false);
            resultado.put("message", "Error consultando promociones");
        }
        return resultado;
    }
}