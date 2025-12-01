package com.ventas.ventas.DTOs.Perfil;

public class FidelidadData {

    private Integer hotdogsComprados;
    private Integer puntosAcumulados;
    private Integer promocionActual;
    private int promocionesPendientes;
    private Integer metaPromocion;
    private Integer porcentajeCompletado;
    private Integer totalPersonasAcumulando;

    public Integer getHotdogsComprados() {
        return hotdogsComprados;
    }

    public void setHotdogsComprados(Integer hotdogsComprados) {
        this.hotdogsComprados = hotdogsComprados;
    }

    public Integer getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public void setPuntosAcumulados(Integer puntosAcumulados) {
        this.puntosAcumulados = puntosAcumulados;
    }

    public Integer getPromocionActual() {
        return promocionActual;
    }

    public void setPromocionActual(Integer promocionActual) {
        this.promocionActual = promocionActual;
    }

    public Integer getMetaPromocion() {
        return metaPromocion;
    }

    public void setMetaPromocion(Integer metaPromocion) {
        this.metaPromocion = metaPromocion;
    }

    public Integer getPorcentajeCompletado() {
        return porcentajeCompletado;
    }

    public void setPorcentajeCompletado(Integer porcentajeCompletado) {
        this.porcentajeCompletado = porcentajeCompletado;
    }

    public Integer getTotalPersonasAcumulando() {
        return totalPersonasAcumulando;
    }

    public void setTotalPersonasAcumulando(Integer totalPersonasAcumulando) {
        this.totalPersonasAcumulando = totalPersonasAcumulando;
    }

    public int getPromocionesPendientes() {
        return promocionesPendientes;
    }

    public void setPromocionesPendientes(int promocionesPendientes) {
        this.promocionesPendientes = promocionesPendientes;
    }
}
