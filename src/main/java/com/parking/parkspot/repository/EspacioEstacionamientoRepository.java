package com.parking.parkspot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.parking.parkspot.model.entity.EspacioEstacionamiento;
import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.TipoEspacio;

@Repository
public interface EspacioEstacionamientoRepository extends JpaRepository<EspacioEstacionamiento, Long> {
    
    // Buscar espacio por número
    Optional<EspacioEstacionamiento> findByNumero(String numero);
    
    // Buscar espacios por estado
    List<EspacioEstacionamiento> findByEstado(EstadoEspacio estado);
    
    // Buscar espacios por tipo
    List<EspacioEstacionamiento> findByTipo(TipoEspacio tipo);
    
    // Buscar espacios disponibles
    List<EspacioEstacionamiento> findByEstadoOrderByNumeroAsc(EstadoEspacio estado);
    
    // Buscar espacios por tipo y estado
    List<EspacioEstacionamiento> findByTipoAndEstadoOrderByNumeroAsc(TipoEspacio tipo, EstadoEspacio estado);
    
    // Contar espacios por estado
    @Query("SELECT COUNT(e) FROM EspacioEstacionamiento e WHERE e.estado = :estado")
    long countByEstado(@Param("estado") EstadoEspacio estado);
    
    // Verificar si existe un espacio con un número específico
    boolean existsByNumero(String numero);
}
