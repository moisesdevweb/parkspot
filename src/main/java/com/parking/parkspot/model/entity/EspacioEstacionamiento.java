package com.parking.parkspot.model.entity;

import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.TipoEspacio;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "espacios_estacionamiento")
public class EspacioEstacionamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String numero; // A1, A2, B1, B2, etc.

    @Enumerated(EnumType.STRING)
    private TipoEspacio tipo; // REGULAR, DISCAPACITADO, VIP

    @Enumerated(EnumType.STRING)
    private EstadoEspacio estado; // DISPONIBLE, OCUPADO, RESERVADO, MANTENIMIENTO

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tarifa_por_hora")
    private Double tarifaPorHora = 5.0; // Tarifa por defecto

    // Constructors
    public EspacioEstacionamiento() {}

    public EspacioEstacionamiento(String numero, TipoEspacio tipo, String descripcion, Double tarifaPorHora) {
        this.numero = numero;
        this.tipo = tipo;
        this.estado = EstadoEspacio.DISPONIBLE;
        this.descripcion = descripcion;
        this.tarifaPorHora = tarifaPorHora != null ? tarifaPorHora : 5.0;
    }

    // Métodos de negocio
    public void ocupar() {
        this.estado = EstadoEspacio.OCUPADO;
    }

    public void liberar() {
        this.estado = EstadoEspacio.DISPONIBLE;
    }

    public void reservar() {
        this.estado = EstadoEspacio.RESERVADO;
    }

    public void ponerEnMantenimiento() {
        this.estado = EstadoEspacio.MANTENIMIENTO;
    }

    public boolean estaDisponible() {
        return this.estado == EstadoEspacio.DISPONIBLE;
    }

    public boolean estaOcupado() {
        return this.estado == EstadoEspacio.OCUPADO;
    }

    public boolean estaReservado() {
        return this.estado == EstadoEspacio.RESERVADO;
    }

    public boolean estaEnMantenimiento() {
        return this.estado == EstadoEspacio.MANTENIMIENTO;
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
