package com.ventas.ventas.DB;


import com.ventas.ventas.DTOs.Perfil.FidelidadData;
import com.ventas.ventas.DTOs.Perfil.PerfilCompletoResponse;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class PerfilDB {

    private final DataSource dataSource;

    public PerfilDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PerfilCompletoResponse obtenerPerfilCompleto(String email) {
        // Query para obtener datos del usuario + fidelidad
        String sql = """
    SELECT u.id, u.nombre, u.email, u.telefono, u.avatar_id, u.rol, u.fecha_registro,
           COALESCE(f.hotdogs_comprados, 0) as hotdogs_comprados,
           COALESCE(f.puntos_acumulados, 0) as puntos_acumulados,
           COALESCE(f.promocion_actual, 0) as promocion_actual,
           COALESCE(f.promociones_pendientes, 0) as promociones_pendientes, -- NUEVO CAMPO
           (SELECT COUNT(*) FROM fidelidad WHERE hotdogs_comprados > 0) as total_personas_acumulando
    FROM usuarios u
    LEFT JOIN fidelidad f ON u.id = f.usuario_id
    WHERE u.email = ? AND u.activo = true
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PerfilCompletoResponse response = new PerfilCompletoResponse();
                // Mapear datos del usuario
                response.setId(rs.getInt("id"));
                response.setNombre(rs.getString("nombre"));
                response.setEmail(rs.getString("email"));
                response.setTelefono(rs.getString("telefono"));
                response.setAvatarId(rs.getInt("avatar_id"));
                response.setRol(rs.getString("rol"));
                response.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());

                // Mapear fidelidad
                FidelidadData fidelidad = new FidelidadData();
                fidelidad.setHotdogsComprados(rs.getInt("hotdogs_comprados"));
                fidelidad.setPuntosAcumulados(rs.getInt("puntos_acumulados"));
                fidelidad.setPromocionActual(rs.getInt("promocion_actual"));
                fidelidad.setPromocionesPendientes(rs.getInt("promociones_pendientes"));
                fidelidad.setMetaPromocion(5); // Meta fija de 5 hotdogs
                fidelidad.setPorcentajeCompletado((fidelidad.getPromocionActual() * 100) / 5);
                fidelidad.setTotalPersonasAcumulando(rs.getInt("total_personas_acumulando"));

                response.setFidelidad(fidelidad);
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean actualizarAvatar(Integer usuarioId, Integer nuevoAvatarId) {
        String sql = "UPDATE usuarios SET avatar_id = ? WHERE id = ? AND activo = true";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nuevoAvatarId);
            stmt.setInt(2, usuarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
