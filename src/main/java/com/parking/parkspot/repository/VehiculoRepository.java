package com.parking.parkspot.repository;

import com.parking.parkspot.model.entity.Vehiculo;
import com.parking.parkspot.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    
    List<Vehiculo> findByCliente(User cliente);
    
    List<Vehiculo> findByClienteId(Long clienteId);
    
    Optional<Vehiculo> findByPlaca(String placa);
    
    boolean existsByPlaca(String placa);
}
