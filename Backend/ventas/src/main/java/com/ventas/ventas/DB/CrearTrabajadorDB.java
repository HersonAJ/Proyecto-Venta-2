package com.ventas.ventas.DB;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class CrearTrabajadorDB {

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;

    public CrearTrabajadorDB(DataSource dataSource, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean crearTrabajador(String nombre, String email, String password, String telefono) {
        String sql = "INSERT INTO usuarios (nombre, email, password, telefono, avatar_id, rol) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setString(3, passwordEncoder.encode(password));
            stmt.setString(4, telefono);
            stmt.setInt(5, 1);
            stmt.setString(6, "trabajador");

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            System.out.println("Error creando trabajador: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean existeUsuario(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println("Error verificando usuario: " + e.getMessage());
        }
        return false;
    }
}