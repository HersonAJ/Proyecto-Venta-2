package com.ventas.ventas.DTOs.productos;

import java.util.List;
import java.util.Map;

public class VentaManualRequest {
    private List<Map<String, Object>> items;
    private Double total;

    // Getters y Setters
    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}