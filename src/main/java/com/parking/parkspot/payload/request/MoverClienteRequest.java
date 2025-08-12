package com.parking.parkspot.payload.request;

import jakarta.validation.constraints.NotNull;

public class MoverClienteRequest {
    @NotNull(message = "El ID del registro es requerido")
    private Long registroId;

    @NotNull(message = "El ID del nuevo espacio es requerido")
    private Long nuevoEspacioId;

    private String motivo;

    // Constructors
    public MoverClienteRequest() {}

    public MoverClienteRequest(Long registroId, Long nuevoEspacioId, String motivo) {
        this.registroId = registroId;
        this.nuevoEspacioId = nuevoEspacioId;
        this.motivo = motivo;
    }

    // Getters and setters
    public Long getRegistroId() {
        return registroId;
    }

    public void setRegistroId(Long registroId) {
        this.registroId = registroId;
    }

    public Long getNuevoEspacioId() {
        return nuevoEspacioId;
    }

    public void setNuevoEspacioId(Long nuevoEspacioId) {
        this.nuevoEspacioId = nuevoEspacioId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
