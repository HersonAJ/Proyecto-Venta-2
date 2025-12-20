package com.ventas.ventas.DB.reportes;

import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReporteVentasAdminDB {

    private final DataSource dataSource;

    public ReporteVentasAdminDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> obtenerReporteVentas(Date fechaInicio, Date fechaFin,
                                                    Integer pagina, Integer limite) {
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

            // 2. Construir consulta SQL con filtros
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("""
                SELECT 
                    v.id,
                    v.pedido_id,
                    v.usuario_id,
                    COALESCE(u.nombre, 'Cliente no registrado') as cliente_nombre,
                    COALESCE(t.nombre, 'Trabajador no asignado') as trabajador_nombre,
                    v.fecha_venta,
                    v.total,
                    v.metodo_pago,
                    v.tipo_venta,
                    v.descripcion
                FROM ventas v
                LEFT JOIN usuarios u ON v.usuario_id = u.id
                LEFT JOIN usuarios t ON v.trabajador_id = t.id
                WHERE 1=1
                -- Excluir ventas de prueba (usuarios específicos de prueba)
                AND (v.usuario_id IS NULL OR v.usuario_id NOT IN (1, 2, 3, 4, 5))
                """);

            // 3. Agregar filtros de fecha si se proporcionan
            List<Object> parametros = new ArrayList<>();

            if (fechaInicio != null) {
                sqlBuilder.append(" AND DATE(v.fecha_venta) >= ?");
                parametros.add(fechaInicio);
            }

            if (fechaFin != null) {
                sqlBuilder.append(" AND DATE(v.fecha_venta) <= ?");
                parametros.add(fechaFin);
            }

            // 4. Agregar orden y paginación
            sqlBuilder.append(" ORDER BY v.fecha_venta DESC");

            if (limit > 0) {
                sqlBuilder.append(" LIMIT ? OFFSET ?");
                parametros.add(limit);
                parametros.add(offset);
            }

            String sql = sqlBuilder.toString();

            // 5. Ejecutar consulta para obtener ventas
            List<Map<String, Object>> ventas = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < parametros.size(); i++) {
                    Object param = parametros.get(i);
                    if (param instanceof Date) {
                        stmt.setDate(i + 1, (Date) param);
                    } else if (param instanceof Integer) {
                        stmt.setInt(i + 1, (Integer) param);
                    }
                }

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Map<String, Object> venta = new HashMap<>();
                    venta.put("id", rs.getInt("id"));
                    venta.put("pedido_id", rs.getInt("pedido_id"));
                    venta.put("usuario_id", rs.getInt("usuario_id"));
                    venta.put("cliente_nombre", rs.getString("cliente_nombre"));
                    venta.put("trabajador_nombre", rs.getString("trabajador_nombre"));
                    venta.put("fecha_venta", rs.getTimestamp("fecha_venta"));
                    venta.put("total", rs.getDouble("total"));
                    venta.put("metodo_pago", rs.getString("metodo_pago"));
                    venta.put("tipo_venta", rs.getString("tipo_venta"));
                    venta.put("descripcion", rs.getString("descripcion"));

                    ventas.add(venta);
                }
            }

            // 6. Obtener total de registros (sin paginación para estadísticas)
            int totalRegistros = obtenerTotalVentas(conn, fechaInicio, fechaFin);

            // 7. Obtener estadísticas: total por día
            List<Map<String, Object>> ventasPorDia = obtenerVentasPorDia(conn, fechaInicio, fechaFin);

            // 8. Calcular total general en el periodo
            double totalGeneral = calcularTotalGeneral(conn, fechaInicio, fechaFin);

            // 9. Obtener ventas por mes (si el rango es amplio)
            List<Map<String, Object>> ventasPorMes = null;
            if (fechaInicio != null && fechaFin != null) {
                long diasDiferencia = (fechaFin.getTime() - fechaInicio.getTime()) / (1000 * 60 * 60 * 24);
                if (diasDiferencia > 60) { // Si el rango es mayor a 2 meses
                    ventasPorMes = obtenerVentasPorMes(conn, fechaInicio, fechaFin);
                }
            }

            // 10. Preparar resultado
            resultado.put("success", true);
            resultado.put("ventas", ventas);
            resultado.put("totalRegistros", totalRegistros);
            resultado.put("totalGeneral", totalGeneral);
            resultado.put("ventasPorDia", ventasPorDia);

            if (ventasPorMes != null) {
                resultado.put("ventasPorMes", ventasPorMes);
            }

            // Información de paginación
            if (limit > 0) {
                int totalPaginas = (int) Math.ceil((double) totalRegistros / limit);
                resultado.put("paginaActual", pagina != null ? pagina : 1);
                resultado.put("limite", limit);
                resultado.put("totalPaginas", totalPaginas);
                resultado.put("hayMasPaginas", (pagina != null && pagina < totalPaginas));
            }

        } catch (SQLException e) {
            System.out.println("Error obteniendo reporte de ventas: " + e.getMessage());
            e.printStackTrace();
            resultado.put("success", false);
            resultado.put("message", "Error al obtener el reporte de ventas");
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

    private int obtenerTotalVentas(Connection conn, Date fechaInicio, Date fechaFin) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("""
            SELECT COUNT(*) as total
            FROM ventas v
            WHERE 1=1
            -- Excluir ventas de prueba
            AND (v.usuario_id IS NULL OR v.usuario_id NOT IN (1, 2, 3, 4, 5))
            """);

        List<Object> parametros = new ArrayList<>();

        if (fechaInicio != null) {
            sqlBuilder.append(" AND DATE(v.fecha_venta) >= ?");
            parametros.add(fechaInicio);
        }

        if (fechaFin != null) {
            sqlBuilder.append(" AND DATE(v.fecha_venta) <= ?");
            parametros.add(fechaFin);
        }

        String sql = sqlBuilder.toString();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                Object param = parametros.get(i);
                if (param instanceof Date) {
                    stmt.setDate(i + 1, (Date) param);
                }
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    private List<Map<String, Object>> obtenerVentasPorDia(Connection conn, Date fechaInicio, Date fechaFin) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("""
            SELECT 
                DATE(v.fecha_venta) as fecha,
                COUNT(*) as cantidad_ventas,
                SUM(v.total) as total_dia
            FROM ventas v
            WHERE 1=1
            -- Excluir ventas de prueba
            AND (v.usuario_id IS NULL OR v.usuario_id NOT IN (1, 2, 3, 4, 5))
            """);

        List<Object> parametros = new ArrayList<>();

        if (fechaInicio != null) {
            sqlBuilder.append(" AND DATE(v.fecha_venta) >= ?");
            parametros.add(fechaInicio);
        }

        if (fechaFin != null) {
            sqlBuilder.append(" AND DATE(v.fecha_venta) <= ?");
            parametros.add(fechaFin);
        }

        sqlBuilder.append(" GROUP BY DATE(v.fecha_venta) ORDER BY fecha DESC");

        String sql = sqlBuilder.toString();
        List<Map<String, Object>> ventasPorDia = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                Object param = parametros.get(i);
                if (param instanceof Date) {
                    stmt.setDate(i + 1, (Date) param);
                }
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> ventaDia = new HashMap<>();
                ventaDia.put("fecha", rs.getDate("fecha"));
                ventaDia.put("cantidad_ventas", rs.getInt("cantidad_ventas"));
                ventaDia.put("total_dia", rs.getDouble("total_dia"));

                ventasPorDia.add(ventaDia);
            }
        }

        return ventasPorDia;
    }

    private List<Map<String, Object>> obtenerVentasPorMes(Connection conn, Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = """
            SELECT 
                EXTRACT(YEAR FROM v.fecha_venta) as año,
                EXTRACT(MONTH FROM v.fecha_venta) as mes,
                COUNT(*) as cantidad_ventas,
                SUM(v.total) as total_mes
            FROM ventas v
            WHERE 1=1
            -- Excluir ventas de prueba
            AND (v.usuario_id IS NULL OR v.usuario_id NOT IN (1, 2, 3, 4, 5))
            AND DATE(v.fecha_venta) >= ?
            AND DATE(v.fecha_venta) <= ?
            GROUP BY EXTRACT(YEAR FROM v.fecha_venta), EXTRACT(MONTH FROM v.fecha_venta)
            ORDER BY año DESC, mes DESC
            """;

        List<Map<String, Object>> ventasPorMes = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, fechaInicio);
            stmt.setDate(2, fechaFin);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> ventaMes = new HashMap<>();
                ventaMes.put("año", rs.getInt("año"));
                ventaMes.put("mes", rs.getInt("mes"));
                ventaMes.put("cantidad_ventas", rs.getInt("cantidad_ventas"));
                ventaMes.put("total_mes", rs.getDouble("total_mes"));

                ventasPorMes.add(ventaMes);
            }
        }

        return ventasPorMes;
    }

    private double calcularTotalGeneral(Connection conn, Date fechaInicio, Date fechaFin) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("""
            SELECT COALESCE(SUM(v.total), 0) as total_general
            FROM ventas v
            WHERE 1=1
            -- Excluir ventas de prueba
            AND (v.usuario_id IS NULL OR v.usuario_id NOT IN (1, 2, 3, 4, 5))
            """);

        List<Object> parametros = new ArrayList<>();

        if (fechaInicio != null) {
            sqlBuilder.append(" AND DATE(v.fecha_venta) >= ?");
            parametros.add(fechaInicio);
        }

        if (fechaFin != null) {
            sqlBuilder.append(" AND DATE(v.fecha_venta) <= ?");
            parametros.add(fechaFin);
        }

        String sql = sqlBuilder.toString();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                Object param = parametros.get(i);
                if (param instanceof Date) {
                    stmt.setDate(i + 1, (Date) param);
                }
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_general");
            }
        }

        return 0.0;
    }

    public Map<String, Object> obtenerEstadisticasVentas() {
        Connection conn = null;
        Map<String, Object> resultado = new HashMap<>();

        try {
            conn = dataSource.getConnection();

            // Ventas del día actual
            String sqlHoy = """
                SELECT 
                    COALESCE(SUM(total), 0) as total_hoy,
                    COUNT(*) as ventas_hoy
                FROM ventas
                WHERE DATE(fecha_venta) = CURRENT_DATE
                AND (usuario_id IS NULL OR usuario_id NOT IN (1, 2, 3, 4, 5))
                """;

            // Ventas del mes actual
            String sqlMes = """
                SELECT 
                    COALESCE(SUM(total), 0) as total_mes,
                    COUNT(*) as ventas_mes
                FROM ventas
                WHERE EXTRACT(YEAR FROM fecha_venta) = EXTRACT(YEAR FROM CURRENT_DATE)
                AND EXTRACT(MONTH FROM fecha_venta) = EXTRACT(MONTH FROM CURRENT_DATE)
                AND (usuario_id IS NULL OR usuario_id NOT IN (1, 2, 3, 4, 5))
                """;

            // Promedio diario del mes
            String sqlPromedio = """
                SELECT 
                    COALESCE(AVG(total_dia), 0) as promedio_diario
                FROM (
                    SELECT DATE(fecha_venta) as fecha, SUM(total) as total_dia
                    FROM ventas
                    WHERE EXTRACT(YEAR FROM fecha_venta) = EXTRACT(YEAR FROM CURRENT_DATE)
                    AND EXTRACT(MONTH FROM fecha_venta) = EXTRACT(MONTH FROM CURRENT_DATE)
                    AND (usuario_id IS NULL OR usuario_id NOT IN (1, 2, 3, 4, 5))
                    GROUP BY DATE(fecha_venta)
                ) as ventas_diarias
                """;

            try (PreparedStatement stmtHoy = conn.prepareStatement(sqlHoy);
                 PreparedStatement stmtMes = conn.prepareStatement(sqlMes);
                 PreparedStatement stmtPromedio = conn.prepareStatement(sqlPromedio)) {

                // Ventas hoy
                ResultSet rsHoy = stmtHoy.executeQuery();
                if (rsHoy.next()) {
                    resultado.put("totalHoy", rsHoy.getDouble("total_hoy"));
                    resultado.put("ventasHoy", rsHoy.getInt("ventas_hoy"));
                }

                // Ventas mes
                ResultSet rsMes = stmtMes.executeQuery();
                if (rsMes.next()) {
                    resultado.put("totalMes", rsMes.getDouble("total_mes"));
                    resultado.put("ventasMes", rsMes.getInt("ventas_mes"));
                }

                // Promedio diario
                ResultSet rsPromedio = stmtPromedio.executeQuery();
                if (rsPromedio.next()) {
                    resultado.put("promedioDiario", rsPromedio.getDouble("promedio_diario"));
                }

                resultado.put("success", true);
            }

        } catch (SQLException e) {
            System.out.println("Error obteniendo estadísticas: " + e.getMessage());
            e.printStackTrace();
            resultado.put("success", false);
            resultado.put("message", "Error al obtener estadísticas");
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
}