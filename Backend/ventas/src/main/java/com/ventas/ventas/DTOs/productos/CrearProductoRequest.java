package com.ventas.ventas.DTOs.productos;

import java.util.List;
import java.util.Map;

public class CrearProductoRequest {
    private String nombre;
    private String tipo;
    private Double precioBase;
    private String descripcion;
    private String imagenUrl;
    private Integer tiempoPreparacion;
    private List<IngredienteRequest> ingredientes;

    // Constructor vacío
    public CrearProductoRequest() {}

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getPrecioBase() { return precioBase; }
    public void setPrecioBase(Double precioBase) { this.precioBase = precioBase; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Integer getTiempoPreparacion() { return tiempoPreparacion; }
    public void setTiempoPreparacion(Integer tiempoPreparacion) { this.tiempoPreparacion = tiempoPreparacion; }

    public List<IngredienteRequest> getIngredientes() { return ingredientes;}
    public void setIngredientes(List<IngredienteRequest> ingredientes) { this.ingredientes = ingredientes;}
}
