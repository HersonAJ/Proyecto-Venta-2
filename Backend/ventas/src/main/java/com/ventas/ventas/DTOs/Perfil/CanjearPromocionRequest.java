package com.ventas.ventas.DTOs.Perfil;

public class CanjearPromocionRequest {
    private Integer usuarioId;
    private Integer cantidad; // Cuántas promociones canjear (normalmente 1)

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
