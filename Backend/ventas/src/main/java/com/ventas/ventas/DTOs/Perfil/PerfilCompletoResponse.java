package com.ventas.ventas.DTOs.Perfil;

import java.time.LocalDateTime;

public class PerfilCompletoResponse {

    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private Integer avatarId;
    private String rol;
    private LocalDateTime fechaRegistro;
    private FidelidadData fidelidad;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Integer avatarId) {
        this.avatarId = avatarId;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public FidelidadData getFidelidad() {
        return fidelidad;
    }

    public void setFidelidad(FidelidadData fidelidad) {
        this.fidelidad = fidelidad;
    }
}
