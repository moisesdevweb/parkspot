package com.parking.parkspot.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.parking.parkspot.model.enums.EstadoReserva;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private User cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id")
    private EspacioEstacionamiento espacio;

    // Fechas
    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva; // Cuando se hizo la reserva

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio; // Cuando inicia la reserva

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin; // Cuando termina la reserva

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado; // PENDIENTE, CONFIRMADA, CANCELADA, UTILIZADA

    @Column(name = "observaciones")
    private String observaciones;

    // Constructors
    public Reserva() {}

    public Reserva(User cliente, Vehiculo vehiculo, EspacioEstacionamiento espacio, 
                   LocalDateTime fechaInicio, LocalDateTime fechaFin, String observaciones) {
        this.cliente = cliente;
        this.vehiculo = vehiculo;
        this.espacio = espacio;
        this.fechaReserva = LocalDateTime.now();
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = EstadoReserva.PENDIENTE;
        this.observaciones = observaciones;
    }

    // Métodos de negocio
    public void confirmar() {
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar(String motivo) {
        this.estado = EstadoReserva.CANCELADA;
        this.observaciones = this.observaciones != null 
            ? this.observaciones + " | Cancelada: " + motivo
            : "Cancelada: " + motivo;
    }

    public void marcarComoUtilizada() {
        this.estado = EstadoReserva.UTILIZADA;
    }

    public boolean estaConfirmada() {
        return this.estado == EstadoReserva.CONFIRMADA;
    }

    public boolean estaPendiente() {
        return this.estado == EstadoReserva.PENDIENTE;
    }

    public boolean estaVigente() {
        LocalDateTime ahora = LocalDateTime.now();
        return this.estado == EstadoReserva.CONFIRMADA && 
               ahora.isAfter(this.fechaInicio) && 
               ahora.isBefore(this.fechaFin);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCliente() {
        return cliente;
    }

    public void setCliente(User cliente) {
        this.cliente = cliente;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public EspacioEstacionamiento getEspacio() {
        return espacio;
    }

    public void setEspacio(EspacioEstacionamiento espacio) {
        this.espacio = espacio;
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
