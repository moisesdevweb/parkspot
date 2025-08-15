package com.parking.parkspot.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.payload.request.ActualizarReservaRequest;
import com.parking.parkspot.payload.request.MoverClienteRequest;
import com.parking.parkspot.payload.request.RegistrarEntradaRequest;
import com.parking.parkspot.payload.request.RegistrarSalidaRequest;
import com.parking.parkspot.payload.response.EspacioEstacionamientoResponse;
import com.parking.parkspot.payload.response.EspacioDragDropResponse;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.RegistroEstacionamientoResponse;
import com.parking.parkspot.payload.response.ReservaResponse;
import com.parking.parkspot.service.EstacionamientoService;
import com.parking.parkspot.repository.UserRepository;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/estacionamiento")
public class EstacionamientoController {

    @Autowired
    private EstacionamientoService estacionamientoService;
    @Autowired
    private UserRepository userRepository;

    // ============= GESTIÓN DE ESPACIOS =============

    // Ver todos los espacios (cualquier usuario autenticado)
    @GetMapping("/espacios")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE') or hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerEspacios() {
        List<EspacioEstacionamientoResponse> espacios = estacionamientoService.obtenerTodosLosEspacios();
        return ResponseEntity.ok(espacios);
    }

    // Ver espacios disponibles
    @GetMapping("/espacios/disponibles")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE') or hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerEspaciosDisponibles() {
        List<EspacioEstacionamientoResponse> espacios = estacionamientoService.obtenerEspaciosDisponibles();
        return ResponseEntity.ok(espacios);
    }

    // ============= REGISTRO DE ENTRADA (VIGILANTE) =============

    @PostMapping("/registrar-entrada")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> registrarEntrada(@Valid @RequestBody RegistrarEntradaRequest entradaRequest,
                                            Authentication authentication) {
        // Obtener vigilante actual
        String username = authentication.getName();
        Optional<com.parking.parkspot.model.entity.User> vigilanteOpt = userRepository.findByUsername(username);
        
        if (!vigilanteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vigilante no encontrado"));
        }

        com.parking.parkspot.model.entity.User vigilante = vigilanteOpt.get();
        String mensaje = estacionamientoService.registrarEntrada(entradaRequest, vigilante);
        return ResponseEntity.ok(new MessageResponse(mensaje));
    }

    // ============= REGISTRO DE SALIDA (VIGILANTE) =============

    @PostMapping("/registrar-salida")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> registrarSalida(@Valid @RequestBody RegistrarSalidaRequest salidaRequest,
                                           Authentication authentication) {
        // Obtener vigilante actual
        String username = authentication.getName();
        Optional<com.parking.parkspot.model.entity.User> vigilanteOpt = userRepository.findByUsername(username);
        
        if (!vigilanteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vigilante no encontrado"));
        }

        com.parking.parkspot.model.entity.User vigilante = vigilanteOpt.get();
        String mensaje = estacionamientoService.registrarSalida(salidaRequest, vigilante);
        return ResponseEntity.ok(new MessageResponse(mensaje));
    }

    // ============= MOVER CLIENTE A OTRO ESPACIO (ADMIN) =============

    @PutMapping("/mover-cliente")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> moverCliente(@Valid @RequestBody MoverClienteRequest moverRequest) {
        String mensaje = estacionamientoService.moverCliente(moverRequest);
        return ResponseEntity.ok(new MessageResponse(mensaje));
    }

    // ============= VISTA DRAG & DROP PARA ADMIN =============
    
    // Obtener espacios con información completa para drag & drop
    @GetMapping("/espacios/drag-drop")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerEspaciosParaDragDrop() {
        List<EspacioDragDropResponse> espacios = estacionamientoService.obtenerEspaciosConClientes();
        return ResponseEntity.ok(espacios);
    }

    // ============= VER REGISTROS =============

    // VIGILANTE puede ver registros activos
    @GetMapping("/registros-activos")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')") // <-- agrega ADMIN
    public ResponseEntity<?> obtenerRegistrosActivos() {
        List<RegistroEstacionamientoResponse> registros = estacionamientoService.obtenerRegistrosActivos();
        return ResponseEntity.ok(registros);
    }

    // ADMIN puede ver todos los registros
    @GetMapping("/registros")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerTodosLosRegistros() {
        List<RegistroEstacionamientoResponse> registros = estacionamientoService.obtenerTodosLosRegistros();
        return ResponseEntity.ok(registros);
    }

    // ADMIN puede ver registros por estado
    @GetMapping("/registros/{estado}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerRegistrosPorEstado(@PathVariable String estado) {
        List<RegistroEstacionamientoResponse> registros = estacionamientoService.obtenerRegistrosPorEstado(estado);
        return ResponseEntity.ok(registros);
    }

    // ============= GESTIÓN DE RESERVAS (ADMIN/VIGILANTE) =============

    // Ver todas las reservas pendientes
    @GetMapping("/reservas/pendientes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerReservasPendientes() {
        List<ReservaResponse> reservas = estacionamientoService.obtenerReservasPendientes();
        return ResponseEntity.ok(reservas);
    }

    // ADMIN puede aprobar/rechazar reserva
    @PutMapping("/reservas/{reservaId}/estado")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> actualizarEstadoReserva(@PathVariable Long reservaId,
                                                   @Valid @RequestBody ActualizarReservaRequest estadoRequest) {
        String mensaje = estacionamientoService.actualizarEstadoReserva(reservaId, estadoRequest);
        return ResponseEntity.ok(new MessageResponse(mensaje));
    }
}
