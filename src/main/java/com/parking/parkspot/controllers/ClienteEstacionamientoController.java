package com.parking.parkspot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.model.entity.RegistroEstacionamiento;
import com.parking.parkspot.model.entity.Reserva;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.enums.EstadoRegistro;
import com.parking.parkspot.payload.request.CrearReservaRequest;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.RegistroEstacionamientoResponse;
import com.parking.parkspot.payload.response.ReservaResponse;
import com.parking.parkspot.repository.RegistroEstacionamientoRepository;
import com.parking.parkspot.repository.ReservaRepository;
import com.parking.parkspot.repository.UserRepository;
import com.parking.parkspot.service.ClienteService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cliente")
public class ClienteEstacionamientoController {

    @Autowired
    private RegistroEstacionamientoRepository registroRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClienteService clienteService;

    // CLIENTE puede ver su historial de estacionamientos
    @GetMapping("/mis-registros")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerMisRegistros(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> clienteOpt = userRepository.findByUsername(username);
        
        if (!clienteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = clienteOpt.get();
        List<RegistroEstacionamiento> registros = registroRepository.findByCliente(cliente);
        
        List<RegistroEstacionamientoResponse> registrosResponse = registros.stream()
                .map(this::convertirRegistroAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(registrosResponse);
    }

    // CLIENTE puede ver sus registros activos (vehículos actualmente estacionados)
    @GetMapping("/mis-registros-activos")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerMisRegistrosActivos(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> clienteOpt = userRepository.findByUsername(username);
        
        if (!clienteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = clienteOpt.get();
        List<RegistroEstacionamiento> registros = registroRepository.findByClienteAndEstado(cliente, EstadoRegistro.ACTIVO);
        
        List<RegistroEstacionamientoResponse> registrosResponse = registros.stream()
                .map(this::convertirRegistroAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(registrosResponse);
    }

    // ============= CREAR RESERVA =============

    // CLIENTE puede crear una nueva reserva
    @PostMapping("/crear-reserva")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> crearReserva(@Valid @RequestBody CrearReservaRequest reservaRequest,
                                         Authentication authentication) {
        String username = authentication.getName();
        Optional<User> clienteOpt = userRepository.findByUsername(username);
        
        if (!clienteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        try {
            User cliente = clienteOpt.get();
            String mensaje = clienteService.crearReserva(reservaRequest, cliente);
            return ResponseEntity.ok(new MessageResponse(mensaje));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    // ============= VER RESERVAS =============

    // CLIENTE puede ver sus reservas
    @GetMapping("/mis-reservas")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerMisReservas(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> clienteOpt = userRepository.findByUsername(username);
        
        if (!clienteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = clienteOpt.get();
        List<Reserva> reservas = reservaRepository.findByClienteOrderByFechaReservaDesc(cliente);
        
        List<ReservaResponse> reservasResponse = reservas.stream()
                .map(this::convertirReservaAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservasResponse);
    }

    // CLIENTE puede ver sus reservas por estado
    @GetMapping("/mis-reservas/{estado}")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerMisReservasPorEstado(@PathVariable String estado,
                                                        Authentication authentication) {
        String username = authentication.getName();
        Optional<User> clienteOpt = userRepository.findByUsername(username);
        
        if (!clienteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        try {
            com.parking.parkspot.model.enums.EstadoReserva estadoEnum = 
                    com.parking.parkspot.model.enums.EstadoReserva.valueOf(estado.toUpperCase());
            
            User cliente = clienteOpt.get();
            List<Reserva> reservas = reservaRepository.findByClienteAndEstadoOrderByFechaReservaDesc(cliente, estadoEnum);
            
            List<ReservaResponse> reservasResponse = reservas.stream()
                    .map(this::convertirReservaAResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(reservasResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Estado inválido. Use: PENDIENTE, CONFIRMADA, CANCELADA o UTILIZADA"));
        }
    }

    // ============= MÉTODOS DE CONVERSIÓN =============

    private RegistroEstacionamientoResponse convertirRegistroAResponse(RegistroEstacionamiento registro) {
        return new RegistroEstacionamientoResponse(
                registro.getId(),
                registro.getEspacio().getNumero(),
                registro.getVehiculo().getCliente().getPersona().getNombreCompleto(),
                registro.getVehiculo().getCliente().getPersona().getApellidos(),
                registro.getVehiculo().getCliente().getPersona().getDni(),
                registro.getVehiculo().getPlaca(),
                registro.getVehiculo().getMarca(),
                registro.getVehiculo().getModelo(),
                registro.getVigilanteEntrada().getPersona().getNombreCompleto(),
                registro.getVigilanteSalida() != null ? registro.getVigilanteSalida().getPersona().getNombreCompleto() : null,
                registro.getFechaHoraEntrada(),
                registro.getFechaHoraSalida(),
                registro.getEstado(),
                registro.getTotalHoras(),
                registro.getMontoTotal(),
                registro.getObservaciones()
        );
    }

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
