package com.parking.parkspot.payload.response;

import java.time.LocalDateTime;

import com.parking.parkspot.model.enums.EstadoReserva;

public class ReservaResponse {
    private Long id;
    private String clienteNombre;
    private String clienteApellidos;
    private String clienteDni;
    private String vehiculoPlaca;
    private String vehiculoMarca;
    private String vehiculoModelo;
    private String numeroEspacio;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoReserva estado;
    private String observaciones;

    // Constructor
    public ReservaResponse(Long id, String clienteNombre, String clienteApellidos, String clienteDni, 
                          String vehiculoPlaca, String vehiculoMarca, String vehiculoModelo, 
                          String numeroEspacio, LocalDateTime fechaReserva, LocalDateTime fechaInicio, 
                          LocalDateTime fechaFin, EstadoReserva estado, String observaciones) {
        this.id = id;
        this.clienteNombre = clienteNombre;
        this.clienteApellidos = clienteApellidos;
        this.clienteDni = clienteDni;
        this.vehiculoPlaca = vehiculoPlaca;
        this.vehiculoMarca = vehiculoMarca;
        this.vehiculoModelo = vehiculoModelo;
        this.numeroEspacio = numeroEspacio;
        this.fechaReserva = fechaReserva;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteApellidos() {
        return clienteApellidos;
    }

    public void setClienteApellidos(String clienteApellidos) {
        this.clienteApellidos = clienteApellidos;
    }

    public String getClienteDni() {
        return clienteDni;
    }

    public void setClienteDni(String clienteDni) {
        this.clienteDni = clienteDni;
    }

    public String getVehiculoPlaca() {
        return vehiculoPlaca;
    }

    public void setVehiculoPlaca(String vehiculoPlaca) {
        this.vehiculoPlaca = vehiculoPlaca;
    }

    public String getVehiculoMarca() {
        return vehiculoMarca;
    }

    public void setVehiculoMarca(String vehiculoMarca) {
        this.vehiculoMarca = vehiculoMarca;
    }

    public String getVehiculoModelo() {
        return vehiculoModelo;
    }

    public void setVehiculoModelo(String vehiculoModelo) {
        this.vehiculoModelo = vehiculoModelo;
    }

    public String getNumeroEspacio() {
        return numeroEspacio;
    }

    public void setNumeroEspacio(String numeroEspacio) {
        this.numeroEspacio = numeroEspacio;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
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

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
