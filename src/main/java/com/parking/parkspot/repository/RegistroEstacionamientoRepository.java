package com.parking.parkspot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.parking.parkspot.model.entity.EspacioEstacionamiento;
import com.parking.parkspot.model.entity.RegistroEstacionamiento;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.entity.Vehiculo;
import com.parking.parkspot.model.enums.EstadoRegistro;

@Repository
public interface RegistroEstacionamientoRepository extends JpaRepository<RegistroEstacionamiento, Long> {
    
    // Buscar registros por vehículo
    List<RegistroEstacionamiento> findByVehiculoOrderByFechaHoraEntradaDesc(Vehiculo vehiculo);
    
    // Buscar registros por espacio
    List<RegistroEstacionamiento> findByEspacioOrderByFechaHoraEntradaDesc(EspacioEstacionamiento espacio);
    
    // Buscar registros por estado
    List<RegistroEstacionamiento> findByEstadoOrderByFechaHoraEntradaDesc(EstadoRegistro estado);
    
    // Buscar registros por vigilante de entrada
    List<RegistroEstacionamiento> findByVigilanteEntradaOrderByFechaHoraEntradaDesc(User vigilante);
    
    // Buscar registros activos (sin fecha de salida)
    @Query("SELECT r FROM RegistroEstacionamiento r WHERE r.estado = 'ACTIVO' AND r.fechaHoraSalida IS NULL")
    List<RegistroEstacionamiento> findRegistrosActivos();
    
    // Buscar registro activo por vehículo
    @Query("SELECT r FROM RegistroEstacionamiento r WHERE r.vehiculo = :vehiculo AND r.estado = 'ACTIVO' AND r.fechaHoraSalida IS NULL")
    Optional<RegistroEstacionamiento> findRegistroActivoPorVehiculo(@Param("vehiculo") Vehiculo vehiculo);
    
    // Buscar registro activo por espacio
    @Query("SELECT r FROM RegistroEstacionamiento r WHERE r.espacio = :espacio AND r.estado = 'ACTIVO' AND r.fechaHoraSalida IS NULL")
    Optional<RegistroEstacionamiento> findRegistroActivoPorEspacio(@Param("espacio") EspacioEstacionamiento espacio);
    
    // Buscar registros de un cliente por estado
    @Query("SELECT r FROM RegistroEstacionamiento r WHERE r.vehiculo.cliente = :cliente AND r.estado = :estado ORDER BY r.fechaHoraEntrada DESC")
    List<RegistroEstacionamiento> findByClienteAndEstado(@Param("cliente") User cliente, @Param("estado") EstadoRegistro estado);
    
    // Contar registros activos
    @Query("SELECT COUNT(r) FROM RegistroEstacionamiento r WHERE r.estado = 'ACTIVO'")
    long countRegistrosActivos();
    
    // Buscar registros por cliente
    @Query("SELECT r FROM RegistroEstacionamiento r WHERE r.vehiculo.cliente = :cliente ORDER BY r.fechaHoraEntrada DESC")
    List<RegistroEstacionamiento> findByCliente(@Param("cliente") User cliente);
}
