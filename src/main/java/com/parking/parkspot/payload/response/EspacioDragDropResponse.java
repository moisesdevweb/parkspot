package com.parking.parkspot.payload.response;

import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.TipoEspacio;

public class EspacioDragDropResponse {
    private Long id;
    private String numero;
    private TipoEspacio tipo;
    private EstadoEspacio estado;
    private String descripcion;
    private Double tarifaPorHora;
    
    // Información del cliente si está ocupado
    private Long registroId;
    private String clienteNombre;
    private String clienteApellidos;
    private String clienteDni;
    private String vehiculoPlaca;
    private String vehiculoTipo;

    // Constructor para espacio disponible
    public EspacioDragDropResponse(Long id, String numero, TipoEspacio tipo, EstadoEspacio estado, 
                                  String descripcion, Double tarifaPorHora) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.estado = estado;
        this.descripcion = descripcion;
        this.tarifaPorHora = tarifaPorHora;
    }

    // Constructor para espacio ocupado con cliente
    public EspacioDragDropResponse(Long id, String numero, TipoEspacio tipo, EstadoEspacio estado, 
                                  String descripcion, Double tarifaPorHora,
                                  Long registroId, String clienteNombre, String clienteApellidos, 
                                  String clienteDni, String vehiculoPlaca, String vehiculoTipo) {
        this(id, numero, tipo, estado, descripcion, tarifaPorHora);
        this.registroId = registroId;
        this.clienteNombre = clienteNombre;
        this.clienteApellidos = clienteApellidos;
        this.clienteDni = clienteDni;
        this.vehiculoPlaca = vehiculoPlaca;
        this.vehiculoTipo = vehiculoTipo;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public TipoEspacio getTipo() {
        return tipo;
    }

    public void setTipo(TipoEspacio tipo) {
        this.tipo = tipo;
    }

    public EstadoEspacio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEspacio estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getTarifaPorHora() {
        return tarifaPorHora;
    }

    public void setTarifaPorHora(Double tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }

    public Long getRegistroId() {
        return registroId;
    }

    public void setRegistroId(Long registroId) {
        this.registroId = registroId;
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

    public String getVehiculoTipo() {
        return vehiculoTipo;
    }

    public void setVehiculoTipo(String vehiculoTipo) {
        this.vehiculoTipo = vehiculoTipo;
    }

    // Método auxiliar para verificar si el espacio tiene cliente
    public boolean tieneCliente() {
        return registroId != null;
    }
}
