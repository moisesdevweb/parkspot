package com.parking.parkspot.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "personas")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCompleto;
    private String apellidos;
    @Column(unique = true, length = 8)
    private String dni;  // DNI del usuario
    private String telefono;
    private String direccion;

    // Estado activo o inactivo
    @Column(name = "estado", nullable = false)
    private Integer estado;

    @OneToOne(mappedBy = "persona")
    private User usuario;
}