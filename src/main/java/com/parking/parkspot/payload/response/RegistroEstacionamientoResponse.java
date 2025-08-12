package com.parking.parkspot.payload.response;

import java.time.LocalDateTime;

import com.parking.parkspot.model.enums.EstadoRegistro;

public class RegistroEstacionamientoResponse {
    private Long id;
    private String numeroEspacio;
    private String clienteNombre;
    private String clienteApellidos;
    private String clienteDni;
    private String vehiculoPlaca;
    private String vehiculoMarca;
    private String vehiculoModelo;
    private String vigilanteEntrada;
    private String vigilanteSalida;
    private LocalDateTime fechaHoraEntrada;
    private LocalDateTime fechaHoraSalida;
    private EstadoRegistro estado;
    private Double totalHoras;
    private Double montoTotal;
    private String observaciones;

    // Constructor
    public RegistroEstacionamientoResponse(Long id, String numeroEspacio, String clienteNombre, 
                                          String clienteApellidos, String clienteDni, String vehiculoPlaca, 
                                          String vehiculoMarca, String vehiculoModelo, String vigilanteEntrada, 
                                          String vigilanteSalida, LocalDateTime fechaHoraEntrada, 
                                          LocalDateTime fechaHoraSalida, EstadoRegistro estado, 
                                          Double totalHoras, Double montoTotal, String observaciones) {
        this.id = id;
        this.numeroEspacio = numeroEspacio;
        this.clienteNombre = clienteNombre;
        this.clienteApellidos = clienteApellidos;
        this.clienteDni = clienteDni;
        this.vehiculoPlaca = vehiculoPlaca;
        this.vehiculoMarca = vehiculoMarca;
        this.vehiculoModelo = vehiculoModelo;
        this.vigilanteEntrada = vigilanteEntrada;
        this.vigilanteSalida = vigilanteSalida;
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.fechaHoraSalida = fechaHoraSalida;
        this.estado = estado;
        this.totalHoras = totalHoras;
        this.montoTotal = montoTotal;
        this.observaciones = observaciones;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroEspacio() {
        return numeroEspacio;
    }

    public void setNumeroEspacio(String numeroEspacio) {
        this.numeroEspacio = numeroEspacio;
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

    public String getVigilanteEntrada() {
        return vigilanteEntrada;
    }

    public void setVigilanteEntrada(String vigilanteEntrada) {
        this.vigilanteEntrada = vigilanteEntrada;
    }

    public String getVigilanteSalida() {
        return vigilanteSalida;
    }

    public void setVigilanteSalida(String vigilanteSalida) {
        this.vigilanteSalida = vigilanteSalida;
    }

    public LocalDateTime getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public EstadoRegistro getEstado() {
        return estado;
    }

    public void setEstado(EstadoRegistro estado) {
        this.estado = estado;
    }

    public Double getTotalHoras() {
        return totalHoras;
    }

    public void setTotalHoras(Double totalHoras) {
        this.totalHoras = totalHoras;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
