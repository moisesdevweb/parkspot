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

    // Constructors, getters, setters...
}
