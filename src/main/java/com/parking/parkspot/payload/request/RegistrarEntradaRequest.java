package com.parking.parkspot.payload.request;

import jakarta.validation.constraints.NotNull;

public class RegistrarEntradaRequest {
    @NotNull(message = "El ID del cliente es requerido")
    private Long clienteId;

    @NotNull(message = "El ID del vehículo es requerido")
    private Long vehiculoId;

    @NotNull(message = "El ID del espacio es requerido")
    private Long espacioId;

    private String observaciones;

    // Constructors
    public RegistrarEntradaRequest() {}

    public RegistrarEntradaRequest(Long clienteId, Long vehiculoId, Long espacioId, String observaciones) {
        this.clienteId = clienteId;
        this.vehiculoId = vehiculoId;
        this.espacioId = espacioId;
        this.observaciones = observaciones;
    }

    // Getters and setters
    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
