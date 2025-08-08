package com.parking.parkspot.repository;

import com.parking.parkspot.model.entity.ReporteIncidencia;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.enums.EstadoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReporteIncidenciaRepository extends JpaRepository<ReporteIncidencia, Long> {
    
    // Encontrar reportes por vigilante
    List<ReporteIncidencia> findByVigilante(User vigilante);
    
    // Encontrar reportes por cliente
    List<ReporteIncidencia> findByCliente(User cliente);
    
    // Encontrar reportes por estado
    List<ReporteIncidencia> findByEstado(EstadoReporte estado);
    
    // Encontrar reportes por vigilante y estado
    List<ReporteIncidencia> findByVigilanteAndEstado(User vigilante, EstadoReporte estado);
    
    // Encontrar reportes por rango de fechas
    List<ReporteIncidencia> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Buscar reportes por nombre de cliente
    @Query("SELECT r FROM ReporteIncidencia r WHERE " +
           "LOWER(r.cliente.persona.nombreCompleto) LIKE LOWER(CONCAT('%', :nombre, '%')) OR " +
           "LOWER(r.cliente.persona.apellidos) LIKE LOWER(CONCAT('%', :nombre, '%')) OR " +
           "LOWER(CONCAT(r.cliente.persona.nombreCompleto, ' ', r.cliente.persona.apellidos)) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<ReporteIncidencia> findByClienteNombreContaining(@Param("nombre") String nombre);
    
    // Buscar reportes por placa de vehículo
    @Query("SELECT r FROM ReporteIncidencia r WHERE LOWER(r.vehiculo.placa) LIKE LOWER(CONCAT('%', :placa, '%'))")
    List<ReporteIncidencia> findByVehiculoPlacaContaining(@Param("placa") String placa);
    
    // Ordenar por fecha de creación descendente
    List<ReporteIncidencia> findAllByOrderByFechaCreacionDesc();
    
    // Encontrar reportes pendientes por vigilante
    List<ReporteIncidencia> findByVigilanteAndEstadoOrderByFechaCreacionDesc(User vigilante, EstadoReporte estado);
}
