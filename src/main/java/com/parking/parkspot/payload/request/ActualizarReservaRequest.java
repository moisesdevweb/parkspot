package com.parking.parkspot.payload.request;

import jakarta.validation.constraints.NotNull;

import com.parking.parkspot.model.enums.EstadoReserva;

public class ActualizarReservaRequest {
    @NotNull(message = "El nuevo estado es requerido")
    private EstadoReserva nuevoEstado;

    private String comentario;

    // Constructors
    public ActualizarReservaRequest() {}

    public ActualizarReservaRequest(EstadoReserva nuevoEstado, String comentario) {
        this.nuevoEstado = nuevoEstado;
        this.comentario = comentario;
    }

    // Getters and setters
    public EstadoReserva getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(EstadoReserva nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
