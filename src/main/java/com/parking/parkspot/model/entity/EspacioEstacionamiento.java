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

    // Constructors, getters, setters...
}
