package com.parking.parkspot.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class CrearReservaRequest {
    @NotNull(message = "El ID del vehículo es requerido")
    private Long vehiculoId;

    @NotNull(message = "El ID del espacio es requerido")
    private Long espacioId;

    @NotNull(message = "La fecha de inicio es requerida")
    @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") // ✅ SIN segundos
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") // ✅ SIN segundos  
    private LocalDateTime fechaFin;

    private String observaciones;

    // Constructors
    public CrearReservaRequest() {}

    public CrearReservaRequest(Long vehiculoId, Long espacioId, LocalDateTime fechaInicio, 
                              LocalDateTime fechaFin, String observaciones) {
        this.vehiculoId = vehiculoId;
        this.espacioId = espacioId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.observaciones = observaciones;
    }

    // Getters and setters
    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Long getEspacioId() {
        return espacioId;
    }

    public void setEspacioId(Long espacioId) {
        this.espacioId = espacioId;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
