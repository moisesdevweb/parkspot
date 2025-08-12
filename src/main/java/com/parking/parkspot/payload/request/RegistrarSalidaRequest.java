package com.parking.parkspot.payload.request;

import jakarta.validation.constraints.NotNull;

public class RegistrarSalidaRequest {
    @NotNull(message = "El ID del registro es requerido")
    private Long registroId;

    private String observaciones;

    // Constructors
    public RegistrarSalidaRequest() {}

    public RegistrarSalidaRequest(Long registroId, String observaciones) {
        this.registroId = registroId;
        this.observaciones = observaciones;
    }

    // Getters and setters
    public Long getRegistroId() {
        return registroId;
    }

    public void setRegistroId(Long registroId) {
        this.registroId = registroId;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
