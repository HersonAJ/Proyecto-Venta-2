package com.ventas.ventas.DB.reportes;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReporteUsuariosDB {

    private final DataSource dataSource;

    public ReporteUsuariosDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> obtenerTodosUsuarios(Integer pagina, Integer limite) {
        Connection conn = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            conn = dataSource.getConnection();

            // 1. Calcular offset para paginación
            int offset = 0;
            if (pagina != null && pagina > 0 && limite != null && limite > 0) {
                offset = (pagina - 1) * limite;
            }

            // Si no se especifica límite, usar un valor por defecto (ej: 50)
            int limit = (limite != null && limite > 0) ? limite : 50;

            // 2. Construir consulta SQL (excluye al admin con id=1)
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("""
                SELECT 
                    id,
                    nombre,
                    email,
                    telefono,
                    avatar_id,
                    rol,
                    fecha_registro,
                    activo
                FROM usuarios
                WHERE id != 1  -- Excluir al usuario admin principal
                ORDER BY fecha_registro DESC
                """);

            // 3. Agregar paginación si es necesario
            if (limit > 0) {
                sqlBuilder.append(" LIMIT ? OFFSET ?");
            }

            String sql = sqlBuilder.toString();

            // 4. Ejecutar consulta para obtener usuarios
            List<Map<String, Object>> usuarios = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (limit > 0) {
                    stmt.setInt(1, limit);
                    stmt.setInt(2, offset);
                }

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Map<String, Object> usuario = mapearUsuario(rs);
                    usuarios.add(usuario);
                }
            }

            // 5. Obtener total de registros (sin paginación)
            int totalRegistros = obtenerTotalUsuarios(conn);

            // 6. Obtener estadísticas por rol
            Map<String, Integer> estadisticasRoles = obtenerEstadisticasRoles(conn);

            // 7. Preparar resultado
            resultado.put("success", true);
            resultado.put("usuarios", usuarios);
            resultado.put("totalRegistros", totalRegistros);
            resultado.put("estadisticasRoles", estadisticasRoles);

            // 8. Información de paginación
            if (limit > 0) {
                int totalPaginas = (int) Math.ceil((double) totalRegistros / limit);
                resultado.put("paginaActual", pagina != null ? pagina : 1);
                resultado.put("limite", limit);
                resultado.put("totalPaginas", totalPaginas);
                resultado.put("hayMasPaginas", (pagina != null && pagina < totalPaginas));
            }

        } catch (SQLException e) {
            System.out.println("Error obteniendo reporte de usuarios: " + e.getMessage());
            e.printStackTrace();
            resultado.put("success", false);
            resultado.put("message", "Error al obtener el reporte de usuarios");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return resultado;
    }

    private int obtenerTotalUsuarios(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM usuarios WHERE id != 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }


    private Map<String, Integer> obtenerEstadisticasRoles(Connection conn) throws SQLException {
        String sql = """
            SELECT 
                rol,
                COUNT(*) as cantidad
            FROM usuarios
            WHERE id != 1
            GROUP BY rol
            ORDER BY rol
            """;

        Map<String, Integer> estadisticas = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String rol = rs.getString("rol");
                int cantidad = rs.getInt("cantidad");
                estadisticas.put(rol, cantidad);
            }
        }

        return estadisticas;
    }

    private Map<String, Object> mapearUsuario(ResultSet rs) throws SQLException {
        Map<String, Object> usuario = new HashMap<>();

        usuario.put("id", rs.getInt("id"));
        usuario.put("nombre", rs.getString("nombre"));
        usuario.put("email", rs.getString("email"));
        usuario.put("telefono", rs.getString("telefono"));
        usuario.put("avatar_id", rs.getInt("avatar_id"));
        usuario.put("rol", rs.getString("rol"));
        usuario.put("fecha_registro", rs.getTimestamp("fecha_registro"));
        usuario.put("activo", rs.getBoolean("activo"));

        return usuario;
    }

    /**
     * Método opcional: Obtiene usuarios con filtro por rol
     * (Por si en el futuro se necesita filtrar)
     */
    public Map<String, Object> obtenerUsuariosPorRol(String rolFiltro, Integer pagina, Integer limite) {
        Connection conn = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            conn = dataSource.getConnection();

            // Calcular offset para paginación
            int offset = 0;
            if (pagina != null && pagina > 0 && limite != null && limite > 0) {
                offset = (pagina - 1) * limite;
            }

            int limit = (limite != null && limite > 0) ? limite : 50;

            // Construir consulta SQL con filtro de rol
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("""
                SELECT 
                    id,
                    nombre,
                    email,
                    telefono,
                    avatar_id,
                    rol,
                    fecha_registro,
                    activo
                FROM usuarios
                WHERE id != 1 AND rol = ?
                ORDER BY fecha_registro DESC
                """);

            if (limit > 0) {
                sqlBuilder.append(" LIMIT ? OFFSET ?");
            }

            String sql = sqlBuilder.toString();

            // Ejecutar consulta
            List<Map<String, Object>> usuarios = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, rolFiltro);

                if (limit > 0) {
                    stmt.setInt(2, limit);
                    stmt.setInt(3, offset);
                }

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Map<String, Object> usuario = mapearUsuario(rs);
                    usuarios.add(usuario);
                }
            }

            // Obtener total de registros con este filtro
            int totalRegistros = obtenerTotalUsuariosPorRol(conn, rolFiltro);

            // Preparar resultado
            resultado.put("success", true);
            resultado.put("usuarios", usuarios);
            resultado.put("totalRegistros", totalRegistros);
            resultado.put("rolFiltro", rolFiltro);

            // Información de paginación
            if (limit > 0) {
                int totalPaginas = (int) Math.ceil((double) totalRegistros / limit);
                resultado.put("paginaActual", pagina != null ? pagina : 1);
                resultado.put("limite", limit);
                resultado.put("totalPaginas", totalPaginas);
                resultado.put("hayMasPaginas", (pagina != null && pagina < totalPaginas));
            }

        } catch (SQLException e) {
            System.out.println("Error obteniendo usuarios por rol: " + e.getMessage());
            e.printStackTrace();
            resultado.put("success", false);
            resultado.put("message", "Error al obtener usuarios");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return resultado;
    }

    /**
     * Obtiene total de usuarios por rol
     */
    private int obtenerTotalUsuariosPorRol(Connection conn, String rol) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM usuarios WHERE id != 1 AND rol = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rol);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }
}
