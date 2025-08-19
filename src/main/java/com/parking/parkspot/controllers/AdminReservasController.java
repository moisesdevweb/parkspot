package com.parking.parkspot.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.model.entity.Reserva;
import com.parking.parkspot.model.enums.EstadoReserva;
import com.parking.parkspot.payload.request.ActualizarReservaRequest;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.ReservaResponse;
import com.parking.parkspot.repository.ReservaRepository;
import com.parking.parkspot.service.EstacionamientoService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminReservasController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private EstacionamientoService estacionamientoService;

    // ============= VER TODAS LAS RESERVAS =============

    @GetMapping("/reservas")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerTodasLasReservas() {
        List<Reserva> reservas = reservaRepository.findAll();
        List<ReservaResponse> reservasResponse = reservas.stream()
                .map(this::convertirReservaAResponse)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(reservasResponse);
    }

    // ============= VER RESERVAS POR ESTADO =============

    @GetMapping("/reservas/estado/{estado}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerReservasPorEstado(@PathVariable String estado) {
        try {
            EstadoReserva estadoEnum = EstadoReserva.valueOf(estado.toUpperCase());
            List<Reserva> reservas = reservaRepository.findByEstadoOrderByFechaReservaDesc(estadoEnum);
            
            List<ReservaResponse> reservasResponse = reservas.stream()
                    .map(this::convertirReservaAResponse)
                    .collect(java.util.stream.Collectors.toList());
                    
            return ResponseEntity.ok(reservasResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Estado inválido. Use: PENDIENTE, CONFIRMADA, CANCELADA o UTILIZADA"));
        }
    }

    // ============= VER RESERVAS PENDIENTES =============

    @GetMapping("/reservas/pendientes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerReservasPendientes() {
        List<Reserva> reservas = reservaRepository.findByEstadoOrderByFechaReservaAsc(EstadoReserva.PENDIENTE);
        List<ReservaResponse> reservasResponse = reservas.stream()
                .map(this::convertirReservaAResponse)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(reservasResponse);
    }

    // ============= GESTIONAR RESERVAS =============

    // ADMIN puede cambiar el estado de una reserva
    @PutMapping("/reservas/{reservaId}/estado")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> actualizarEstadoReserva(@PathVariable Long reservaId,
                                                   @Valid @RequestBody ActualizarReservaRequest estadoRequest) {
        try {
            String mensaje = estacionamientoService.actualizarEstadoReserva(reservaId, estadoRequest);
            return ResponseEntity.ok(new MessageResponse(mensaje));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    // ADMIN puede aprobar una reserva (cambiar a CONFIRMADA)
    @PutMapping("/reservas/{reservaId}/aprobar")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE')") // <-- También vigilante puede aprobar
    public ResponseEntity<?> aprobarReserva(@PathVariable Long reservaId) {
        ActualizarReservaRequest request = new ActualizarReservaRequest(
            EstadoReserva.CONFIRMADA, 
            "Reserva aprobada por administrador o vigilante"
        );
        
        try {
            String mensaje = estacionamientoService.actualizarEstadoReserva(reservaId, request);
            return ResponseEntity.ok(new MessageResponse(mensaje));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    // ADMIN puede rechazar una reserva (cambiar a CANCELADA)
    @PutMapping("/reservas/{reservaId}/rechazar")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE')") // <-- También vigilante puede rechazar
    public ResponseEntity<?> rechazarReserva(@PathVariable Long reservaId) {
        ActualizarReservaRequest request = new ActualizarReservaRequest(
            EstadoReserva.CANCELADA, 
            "Reserva rechazada por administrador o vigilante"
        );
        
        try {
            String mensaje = estacionamientoService.actualizarEstadoReserva(reservaId, request);
            return ResponseEntity.ok(new MessageResponse(mensaje));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    // ============= ESTADÍSTICAS =============

    @GetMapping("/reservas/estadisticas")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerEstadisticasReservas() {
        java.util.Map<String, Object> estadisticas = new java.util.HashMap<>();
        
        estadisticas.put("totalReservas", reservaRepository.count());
        estadisticas.put("reservasPendientes", reservaRepository.countByEstado(EstadoReserva.PENDIENTE));
        estadisticas.put("reservasConfirmadas", reservaRepository.countByEstado(EstadoReserva.CONFIRMADA));
        estadisticas.put("reservasCanceladas", reservaRepository.countByEstado(EstadoReserva.CANCELADA));
        estadisticas.put("reservasUtilizadas", reservaRepository.countByEstado(EstadoReserva.UTILIZADA));
        
        return ResponseEntity.ok(estadisticas);
    }

    // ============= MÉTODO DE CONVERSIÓN =============

    private ReservaResponse convertirReservaAResponse(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getCliente().getPersona().getNombreCompleto(),
                reserva.getCliente().getPersona().getApellidos(),
                reserva.getCliente().getPersona().getDni(),
                reserva.getVehiculo().getPlaca(),
                reserva.getVehiculo().getMarca(),
                reserva.getVehiculo().getModelo(),
                reserva.getEspacio().getNumero(),
                reserva.getFechaReserva(),
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                reserva.getEstado(),
                reserva.getObservaciones()
        );
    }
}
