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

    // Constructors, getters, setters...
}
