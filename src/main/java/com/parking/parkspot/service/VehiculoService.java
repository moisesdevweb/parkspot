package com.parking.parkspot.service;

import com.parking.parkspot.model.entity.*;
import com.parking.parkspot.payload.request.*;
import com.parking.parkspot.payload.response.*;
import com.parking.parkspot.repository.*;
import com.parking.parkspot.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    public List<VehiculoResponse> obtenerVehiculosDeCliente(User cliente) {
        List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(cliente);
        return vehiculos.stream()
                .map(vehiculo -> new VehiculoResponse(
                    vehiculo.getId(),
                    vehiculo.getPlaca(),
                    vehiculo.getMarca(),
                    vehiculo.getModelo(),
                    vehiculo.getColor(),
                    vehiculo.getAño(),
                    vehiculo.getTipo(),
                    vehiculo.getEstado(),
                    cliente.getId(),
                    cliente.getPersona().getNombreCompleto()
                ))
                .collect(Collectors.toList());
    }

    public List<VehiculoBasicoResponse> obtenerVehiculosBasicosDeCliente(User cliente) {
        List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(cliente);
        return vehiculos.stream()
                .map(vehiculo -> new VehiculoBasicoResponse(
                    vehiculo.getId(),
                    vehiculo.getPlaca(),
                    vehiculo.getMarca(),
                    vehiculo.getModelo(),
                    vehiculo.getTipo(),
                    vehiculo.getAño(),
                    vehiculo.getColor()
                ))
                .collect(Collectors.toList());
    }

    public Vehiculo agregarVehiculoACliente(User cliente, VehiculoRequest request) {
        if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
            throw new DuplicateResourceException("La placa ya está registrada");
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(request.getPlaca());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setColor(request.getColor());
        vehiculo.setAño(request.getAño());
        vehiculo.setTipo(request.getTipo());
        vehiculo.setCliente(cliente);
        vehiculo.setEstado(1);

        return vehiculoRepository.save(vehiculo);
    }

    public void editarVehiculo(Long vehiculoId, Long clienteId, VehiculoRequest request) {
        Vehiculo vehiculo = buscarVehiculoPorId(vehiculoId);
        
        if (!vehiculo.getCliente().getId().equals(clienteId)) {
            throw new InvalidOperationException("El vehículo no pertenece a este cliente");
        }

        if (!vehiculo.getPlaca().equals(request.getPlaca()) && 
            vehiculoRepository.existsByPlaca(request.getPlaca())) {
            throw new DuplicateResourceException("La placa ya está registrada");
        }

        vehiculo.setPlaca(request.getPlaca());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setColor(request.getColor());
        vehiculo.setAño(request.getAño());
        vehiculo.setTipo(request.getTipo());

        vehiculoRepository.save(vehiculo);
    }

    public void eliminarVehiculo(Long vehiculoId, Long clienteId) {
        Vehiculo vehiculo = buscarVehiculoPorId(vehiculoId);
        
        if (!vehiculo.getCliente().getId().equals(clienteId)) {
            throw new InvalidOperationException("El vehículo no pertenece a este cliente");
        }

        vehiculoRepository.delete(vehiculo);
    }

    public void actualizarVehiculosDeCliente(User cliente, List<VehiculoUpdateRequest> vehiculosRequest) {
        List<Vehiculo> vehiculosActuales = vehiculoRepository.findByCliente(cliente);

        for (VehiculoUpdateRequest vehiculoReq : vehiculosRequest) {
            if (vehiculoReq.getId() != null) {
                // Actualizar vehículo existente
                Optional<Vehiculo> vehiculoExistente = vehiculosActuales.stream()
                        .filter(v -> v.getId().equals(vehiculoReq.getId()))
                        .findFirst();

                if (vehiculoExistente.isPresent()) {
                    Vehiculo vehiculo = vehiculoExistente.get();
                    
                    if (!vehiculo.getPlaca().equals(vehiculoReq.getPlaca()) && 
                        vehiculoRepository.existsByPlaca(vehiculoReq.getPlaca())) {
                        throw new DuplicateResourceException("La placa " + vehiculoReq.getPlaca() + " ya está registrada");
                    }

                    vehiculo.setPlaca(vehiculoReq.getPlaca());
                    vehiculo.setMarca(vehiculoReq.getMarca());
                    vehiculo.setModelo(vehiculoReq.getModelo());
                    vehiculo.setColor(vehiculoReq.getColor());
                    vehiculo.setAño(vehiculoReq.getAño());
                    vehiculo.setTipo(vehiculoReq.getTipo());
                    vehiculoRepository.save(vehiculo);
                }
            } else {
                // Crear nuevo vehículo
                if (vehiculoRepository.existsByPlaca(vehiculoReq.getPlaca())) {
                    throw new DuplicateResourceException("La placa " + vehiculoReq.getPlaca() + " ya está registrada");
                }

                Vehiculo nuevoVehiculo = new Vehiculo();
                nuevoVehiculo.setPlaca(vehiculoReq.getPlaca());
                nuevoVehiculo.setMarca(vehiculoReq.getMarca());
                nuevoVehiculo.setModelo(vehiculoReq.getModelo());
                nuevoVehiculo.setColor(vehiculoReq.getColor());
                nuevoVehiculo.setAño(vehiculoReq.getAño());
                nuevoVehiculo.setTipo(vehiculoReq.getTipo());
                nuevoVehiculo.setEstado(1);
                nuevoVehiculo.setCliente(cliente);
                vehiculoRepository.save(nuevoVehiculo);
            }
        }
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Vehiculo buscarVehiculoPorId(Long vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
    }
}
