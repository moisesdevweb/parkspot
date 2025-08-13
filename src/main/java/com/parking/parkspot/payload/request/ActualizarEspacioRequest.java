package com.parking.parkspot.payload.request;

import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.TipoEspacio;

public class ActualizarEspacioRequest {
    private TipoEspacio tipo;
    private EstadoEspacio estado;
    private String descripcion;
    private Double tarifaPorHora;

    public ActualizarEspacioRequest() {}

    public TipoEspacio getTipo() { return tipo; }
    public void setTipo(TipoEspacio tipo) { this.tipo = tipo; }
    public EstadoEspacio getEstado() { return estado; }
    public void setEstado(EstadoEspacio estado) { this.estado = estado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getTarifaPorHora() { return tarifaPorHora; }
    public void setTarifaPorHora(Double tarifaPorHora) { this.tarifaPorHora = tarifaPorHora; }
}
