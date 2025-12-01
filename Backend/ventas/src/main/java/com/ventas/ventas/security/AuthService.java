package com.ventas.ventas.security;


import com.ventas.ventas.DTOs.Login.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Service
public class AuthService {

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(DataSource dataSource, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.dataSource = dataSource;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String autenticarYGenerarToken(String email, String password) {
        String sql = "SELECT id, nombre, email, password, rol FROM usuarios WHERE email = ? AND activo = TRUE";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String nombre = rs.getString("nombre");
                String rol = rs.getString("rol");
                Integer userId = rs.getInt("id");

                // Verificar contraseña (encriptada)
                if (passwordEncoder.matches(password, storedPassword)) {
                    return jwtUtil.generateToken(email, rol, nombre, userId);
                }
            }
        } catch (Exception e) {
            System.out.println("Error en autenticación: " + e.getMessage());
        }
        return null;
    }

    public boolean registrarUsuario(String nombre, String email, String password, String telefono, String rol) {
        String sql = "INSERT INTO usuarios (nombre, email, password, telefono, rol) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setString(3, passwordEncoder.encode(password)); // Encriptar contraseña
            stmt.setString(4, telefono);
            stmt.setString(5, rol);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error registrando usuario: " + e.getMessage());
            return false;
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
