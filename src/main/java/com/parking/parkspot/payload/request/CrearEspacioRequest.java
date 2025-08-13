package com.parking.parkspot.payload.request;

import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.TipoEspacio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CrearEspacioRequest {
    @NotBlank(message = "El número del espacio es requerido")
    private String numero;

    @NotNull(message = "El tipo de espacio es requerido")
    private TipoEspacio tipo;

    private EstadoEspacio estado;
    private String descripcion;

    @NotNull(message = "La tarifa por hora es requerida")
    @Positive(message = "La tarifa por hora debe ser positiva")
    private Double tarifaPorHora;

    public CrearEspacioRequest() {}

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public TipoEspacio getTipo() { return tipo; }
    public void setTipo(TipoEspacio tipo) { this.tipo = tipo; }
    public EstadoEspacio getEstado() { return estado; }
    public void setEstado(EstadoEspacio estado) { this.estado = estado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getTarifaPorHora() { return tarifaPorHora; }
    public void setTarifaPorHora(Double tarifaPorHora) { this.tarifaPorHora = tarifaPorHora; }
}
