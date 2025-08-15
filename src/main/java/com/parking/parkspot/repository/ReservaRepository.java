package com.parking.parkspot.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.parking.parkspot.model.entity.EspacioEstacionamiento;
import com.parking.parkspot.model.entity.Reserva;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.entity.Vehiculo;
import com.parking.parkspot.model.enums.EstadoReserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    // Buscar reservas por cliente
    List<Reserva> findByClienteOrderByFechaReservaDesc(User cliente);
    
    // Buscar reservas por estado
    List<Reserva> findByEstadoOrderByFechaReservaDesc(EstadoReserva estado);
    
    // Buscar reservas por cliente y estado
    List<Reserva> findByClienteAndEstadoOrderByFechaReservaDesc(User cliente, EstadoReserva estado);
    
    // Buscar reservas por espacio
    List<Reserva> findByEspacioOrderByFechaReservaDesc(EspacioEstacionamiento espacio);
    
    // Buscar reservas por vehículo
    List<Reserva> findByVehiculoOrderByFechaReservaDesc(Vehiculo vehiculo);
    
    // Buscar reserva activa (CONFIRMADA) para un espacio en un rango de tiempo específico
    @Query("SELECT r FROM Reserva r WHERE r.espacio = :espacio AND r.estado = 'CONFIRMADA' " +
           "AND r.fechaInicio <= :fechaFin AND r.fechaFin >= :fechaInicio")
    Optional<Reserva> findReservaActivaEnEspacio(@Param("espacio") EspacioEstacionamiento espacio,
                                                 @Param("fechaInicio") LocalDateTime fechaInicio,
                                                 @Param("fechaFin") LocalDateTime fechaFin);
    
    // Buscar reservas en conflicto (PENDIENTE o CONFIRMADA) para validar nueva reserva
    @Query("SELECT r FROM Reserva r WHERE r.espacio = :espacio " +
           "AND (r.estado = 'PENDIENTE' OR r.estado = 'CONFIRMADA') " +
           "AND r.fechaInicio < :fechaFin AND r.fechaFin > :fechaInicio")
    List<Reserva> findReservasEnRangoFecha(@Param("espacio") EspacioEstacionamiento espacio,
                                          @Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFin") LocalDateTime fechaFin);
    
    // Buscar reserva confirmada actual para un espacio
    @Query("SELECT r FROM Reserva r WHERE r.espacio = :espacio AND r.estado = 'CONFIRMADA' " +
           "AND r.fechaInicio <= :ahora AND r.fechaFin >= :ahora")
    Optional<Reserva> findReservaConfirmadaActual(@Param("espacio") EspacioEstacionamiento espacio,
                                                  @Param("ahora") LocalDateTime ahora);
    
    // Buscar todas las reservas pendientes
    List<Reserva> findByEstadoOrderByFechaReservaAsc(EstadoReserva estado);
    
    // Contar reservas por estado
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.estado = :estado")
    long countByEstado(@Param("estado") EstadoReserva estado);
}
