package com.parking.parkspot.payload.response;

import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.TipoEspacio;

public class EspacioEstacionamientoResponse {
    private Long id;
    private String numero;
    private TipoEspacio tipo;
    private EstadoEspacio estado;
    private String descripcion;
    private Double tarifaPorHora;

    // Constructor
    public EspacioEstacionamientoResponse(Long id, String numero, TipoEspacio tipo, EstadoEspacio estado, 
                                         String descripcion, Double tarifaPorHora) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.estado = estado;
        this.descripcion = descripcion;
        this.tarifaPorHora = tarifaPorHora;
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
}
