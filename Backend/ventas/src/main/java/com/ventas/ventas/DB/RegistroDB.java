package com.ventas.ventas.DB;

import com.ventas.ventas.DTOs.Login.JwtUtil;
import com.ventas.ventas.modelos.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Repository
public class RegistroDB {

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;

    public RegistroDB(DataSource dataSource, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
    }

    public String registrarUsuarioYGenerarToken(Usuario usuario, JwtUtil jwtUtil) {
        if (!usuario.getAceptaTerminos()) {
            return null;
        }

        String sql = "INSERT INTO usuarios (nombre, email, password, telefono, avatar_id, rol) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, passwordEncoder.encode(usuario.getPassword()));
            stmt.setString(4, usuario.getTelefono());
            stmt.setInt(5, usuario.getAvatarId() != null ? usuario.getAvatarId() : 1);
            stmt.setString(6, "cliente");

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer userId = generatedKeys.getInt(1);
                        return jwtUtil.generateToken(
                                usuario.getEmail(),
                                "cliente",
                                usuario.getNombre(),
                                userId
                        );
                    }
                }
                return null;
            }
            return null;

        } catch (Exception e) {
            System.out.println("Error registrando usuario y generando token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean existeUsuario(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            System.out.println("Error verificando usuario: " + e.getMessage());
        }
        return false;
    }
}