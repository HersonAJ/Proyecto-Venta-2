package com.ventas.ventas.DTOs.Login;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;  // Nuevo campo para JWT
    private String userRole; // Opcional: si quieres mantener el rol
    private String userName; // Opcional: si quieres mantener el nombre

    // Constructores
    public LoginResponse() {}

    public LoginResponse(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }

    // Getters y Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}