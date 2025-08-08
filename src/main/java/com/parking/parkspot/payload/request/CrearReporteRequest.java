package com.parking.parkspot.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CrearReporteRequest {
    
    @NotNull
    private Long clienteId;
    
    @NotNull
    private Long vehiculoId;
    
    @NotBlank
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    private String descripcion;
    
    // Constructor por defecto
    public CrearReporteRequest() {}
    
    // Constructor con parámetros
    public CrearReporteRequest(Long clienteId, Long vehiculoId, String descripcion) {
        this.clienteId = clienteId;
        this.vehiculoId = vehiculoId;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    
    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
