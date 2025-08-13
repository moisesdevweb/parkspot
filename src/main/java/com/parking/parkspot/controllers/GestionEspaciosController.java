package com.parking.parkspot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.model.entity.EspacioEstacionamiento;
import com.parking.parkspot.model.enums.EstadoEspacio;


import com.parking.parkspot.payload.response.EspacioEstacionamientoResponse;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.request.CrearEspacioRequest;
import com.parking.parkspot.payload.request.ActualizarEspacioRequest;
import com.parking.parkspot.repository.EspacioEstacionamientoRepository;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/espacios")
public class GestionEspaciosController {

    @Autowired
    private EspacioEstacionamientoRepository espacioRepository;


    // Crear un espacio individual (admin define todos los campos)
    @PostMapping("/crear")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> crearEspacio(@Valid @RequestBody CrearEspacioRequest espacioRequest) {
        // Verificar que no existe un espacio con ese número
        if (espacioRepository.existsByNumero(espacioRequest.getNumero())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Ya existe un espacio con el número " + espacioRequest.getNumero()));
        }

        // Crear el espacio con todos los campos
        EspacioEstacionamiento espacio = new EspacioEstacionamiento(
                espacioRequest.getNumero(),
                espacioRequest.getTipo(),
                espacioRequest.getDescripcion(),
                espacioRequest.getTarifaPorHora()
        );
        if (espacioRequest.getEstado() != null) {
            espacio.setEstado(espacioRequest.getEstado());
        }

        espacioRepository.save(espacio);

        return ResponseEntity.ok(new MessageResponse("Espacio " + espacio.getNumero() + " creado exitosamente"));
    }

    // Actualizar un espacio
    @PutMapping("/{espacioId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> actualizarEspacio(@PathVariable Long espacioId,
                                             @Valid @RequestBody ActualizarEspacioRequest request) {
        Optional<EspacioEstacionamiento> espacioOpt = espacioRepository.findById(espacioId);
        if (!espacioOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Espacio no encontrado"));
        }

        EspacioEstacionamiento espacio = espacioOpt.get();

        // Solo permitir cambiar ciertos campos si el espacio no está ocupado
        if (espacio.estaOcupado() && request.getEstado() != null && request.getEstado() != EstadoEspacio.OCUPADO) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: No se puede cambiar el estado de un espacio ocupado"));
        }

        // Actualizar campos
        if (request.getTipo() != null) {
            espacio.setTipo(request.getTipo());
        }
        if (request.getEstado() != null) {
            espacio.setEstado(request.getEstado());
        }
        if (request.getDescripcion() != null) {
            espacio.setDescripcion(request.getDescripcion());
        }
        if (request.getTarifaPorHora() != null) {
            espacio.setTarifaPorHora(request.getTarifaPorHora());
        }

        espacioRepository.save(espacio);

        return ResponseEntity.ok(new MessageResponse("Espacio " + espacio.getNumero() + " actualizado exitosamente"));
    }

    // Cambiar estado de un espacio (mantenimiento, disponible, etc.)
    @PutMapping("/{espacioId}/estado")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cambiarEstadoEspacio(@PathVariable Long espacioId,
                                                @RequestParam EstadoEspacio nuevoEstado) {
        Optional<EspacioEstacionamiento> espacioOpt = espacioRepository.findById(espacioId);
        if (!espacioOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Espacio no encontrado"));
        }

        EspacioEstacionamiento espacio = espacioOpt.get();

        // Validaciones según el nuevo estado
        if (nuevoEstado == EstadoEspacio.OCUPADO && espacio.estaOcupado()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El espacio ya está ocupado"));
        }

        // No permitir poner OCUPADO manualmente (solo por registro de entrada)
        if (nuevoEstado == EstadoEspacio.OCUPADO) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El estado OCUPADO solo se asigna automáticamente"));
        }

        espacio.setEstado(nuevoEstado);
        espacioRepository.save(espacio);

        String estadoTexto = "";
        switch (nuevoEstado) {
            case DISPONIBLE:
                estadoTexto = "disponible";
                break;
            case RESERVADO:
                estadoTexto = "reservado";
                break;
            case MANTENIMIENTO:
                estadoTexto = "en mantenimiento";
                break;
            default:
                estadoTexto = nuevoEstado.toString().toLowerCase();
        }

        return ResponseEntity.ok(new MessageResponse("Espacio " + espacio.getNumero() + " marcado como " + estadoTexto));
    }

    // Eliminar un espacio (solo si no está ocupado)
    @DeleteMapping("/{espacioId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> eliminarEspacio(@PathVariable Long espacioId) {
        Optional<EspacioEstacionamiento> espacioOpt = espacioRepository.findById(espacioId);
        if (!espacioOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Espacio no encontrado"));
        }

        EspacioEstacionamiento espacio = espacioOpt.get();

        if (espacio.estaOcupado()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: No se puede eliminar un espacio ocupado"));
        }

        espacioRepository.delete(espacio);
        return ResponseEntity.ok(new MessageResponse("Espacio " + espacio.getNumero() + " eliminado exitosamente"));
    }

    // Ver todos los espacios con su estado actual
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerTodosLosEspacios() {
        List<EspacioEstacionamiento> espacios = espacioRepository.findAll();
        List<EspacioEstacionamientoResponse> espaciosResponse = espacios.stream()
                .map(this::convertirEspacioAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(espaciosResponse);
    }


    // Método de conversión
    private EspacioEstacionamientoResponse convertirEspacioAResponse(EspacioEstacionamiento espacio) {
        return new EspacioEstacionamientoResponse(
                espacio.getId(),
                espacio.getNumero(),
                espacio.getTipo(),
                espacio.getEstado(),
                espacio.getDescripcion(),
                espacio.getTarifaPorHora()
        );
    }

}
