package com.parking.parkspot.payload.request;

import com.parking.parkspot.model.enums.EstadoReporte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ActualizarEstadoReporteRequest {
    
    @NotNull
    private EstadoReporte nuevoEstado;
    
    @Size(max = 500, message = "El comentario no puede tener más de 500 caracteres")
    private String comentarioAdmin;
    
    // Constructor por defecto
    public ActualizarEstadoReporteRequest() {}
    
    // Constructor con parámetros
    public ActualizarEstadoReporteRequest(EstadoReporte nuevoEstado, String comentarioAdmin) {
        this.nuevoEstado = nuevoEstado;
        this.comentarioAdmin = comentarioAdmin;
    }
    
    // Getters y Setters
    public EstadoReporte getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(EstadoReporte nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    
    public String getComentarioAdmin() { return comentarioAdmin; }
    public void setComentarioAdmin(String comentarioAdmin) { this.comentarioAdmin = comentarioAdmin; }
}
