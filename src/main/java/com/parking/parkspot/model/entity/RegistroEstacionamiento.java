package com.parking.parkspot.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.parking.parkspot.model.enums.EstadoRegistro;

@Entity
@Table(name = "registros_estacionamiento")
public class RegistroEstacionamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id")
    private EspacioEstacionamiento espacio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vigilante_entrada_id")
    private User vigilanteEntrada; // Vigilante que registró la entrada

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vigilante_salida_id")
    private User vigilanteSalida; // Vigilante que registró la salida

    // Fechas y horas
    @Column(name = "fecha_hora_entrada")
    private LocalDateTime fechaHoraEntrada;

    @Column(name = "fecha_hora_salida")
    private LocalDateTime fechaHoraSalida;

    // Estado y pago
    @Enumerated(EnumType.STRING)
    private EstadoRegistro estado; // ACTIVO, FINALIZADO, CANCELADO

    @Column(name = "total_horas")
    private Double totalHoras;

    @Column(name = "monto_total")
    private Double montoTotal;

    @Column(name = "observaciones")
    private String observaciones;

    // Constructors
    public RegistroEstacionamiento() {}

    public RegistroEstacionamiento(Vehiculo vehiculo, EspacioEstacionamiento espacio, User vigilanteEntrada) {
        this.vehiculo = vehiculo;
        this.espacio = espacio;
        this.vigilanteEntrada = vigilanteEntrada;
        this.fechaHoraEntrada = LocalDateTime.now();
        this.estado = EstadoRegistro.ACTIVO;
    }

    // Métodos de negocio
    public void finalizarRegistro(User vigilanteSalida, String observacionesSalida) {
        this.vigilanteSalida = vigilanteSalida;
        this.fechaHoraSalida = LocalDateTime.now();
        this.estado = EstadoRegistro.FINALIZADO;
        
        // Calcular horas y monto
        if (this.fechaHoraEntrada != null && this.fechaHoraSalida != null) {
            long minutos = java.time.Duration.between(this.fechaHoraEntrada, this.fechaHoraSalida).toMinutes();
            this.totalHoras = minutos / 60.0;
            this.montoTotal = Math.ceil(this.totalHoras) * this.espacio.getTarifaPorHora();
        }
        
        // Agregar observaciones de salida
        if (observacionesSalida != null && !observacionesSalida.trim().isEmpty()) {
            this.observaciones = this.observaciones != null 
                ? this.observaciones + " | Salida: " + observacionesSalida
                : "Salida: " + observacionesSalida;
        }
    }

    public void cancelarRegistro(String motivo) {
        this.estado = EstadoRegistro.CANCELADO;
        this.observaciones = this.observaciones != null 
            ? this.observaciones + " | Cancelado: " + motivo
            : "Cancelado: " + motivo;
    }

    public void moverAEspacio(EspacioEstacionamiento nuevoEspacio, String motivo) {
        this.observaciones = this.observaciones != null 
            ? this.observaciones + " | Movido desde " + this.espacio.getNumero() + " a " + nuevoEspacio.getNumero() + ": " + motivo
            : "Movido desde " + this.espacio.getNumero() + " a " + nuevoEspacio.getNumero() + ": " + motivo;
        this.espacio = nuevoEspacio;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getVigilanteEntrada() {
        return vigilanteEntrada;
    }

    public void setVigilanteEntrada(User vigilanteEntrada) {
        this.vigilanteEntrada = vigilanteEntrada;
    }

    public User getVigilanteSalida() {
        return vigilanteSalida;
    }

    public void setVigilanteSalida(User vigilanteSalida) {
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
