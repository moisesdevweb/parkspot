package com.parking.parkspot.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.model.entity.EspacioEstacionamiento;
import com.parking.parkspot.model.entity.RegistroEstacionamiento;
import com.parking.parkspot.model.entity.Reserva;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.entity.Vehiculo;
import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.EstadoRegistro;
import com.parking.parkspot.model.enums.EstadoReserva;
import com.parking.parkspot.payload.request.ActualizarReservaRequest;
import com.parking.parkspot.payload.request.MoverClienteRequest;
import com.parking.parkspot.payload.request.RegistrarEntradaRequest;
import com.parking.parkspot.payload.request.RegistrarSalidaRequest;
import com.parking.parkspot.payload.response.EspacioEstacionamientoResponse;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.RegistroEstacionamientoResponse;
import com.parking.parkspot.payload.response.ReservaResponse;
import com.parking.parkspot.repository.EspacioEstacionamientoRepository;
import com.parking.parkspot.repository.RegistroEstacionamientoRepository;
import com.parking.parkspot.repository.ReservaRepository;
import com.parking.parkspot.repository.UserRepository;
import com.parking.parkspot.repository.VehiculoRepository;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/estacionamiento")
public class EstacionamientoController {

    @Autowired
    private RegistroEstacionamientoRepository registroRepository;

    @Autowired
    private EspacioEstacionamientoRepository espacioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    // ============= GESTIÓN DE ESPACIOS =============

    // Ver todos los espacios (cualquier usuario autenticado)
    @GetMapping("/espacios")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE') or hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerEspacios() {
        List<EspacioEstacionamiento> espacios = espacioRepository.findAll();
        List<EspacioEstacionamientoResponse> espaciosResponse = espacios.stream()
                .map(this::convertirEspacioAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(espaciosResponse);
    }

    // Ver espacios disponibles
    @GetMapping("/espacios/disponibles")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE') or hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerEspaciosDisponibles() {
        List<EspacioEstacionamiento> espacios = espacioRepository.findByEstadoOrderByNumeroAsc(EstadoEspacio.DISPONIBLE);
        List<EspacioEstacionamientoResponse> espaciosResponse = espacios.stream()
                .map(this::convertirEspacioAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(espaciosResponse);
    }

    // ============= REGISTRO DE ENTRADA (VIGILANTE) =============

    @PostMapping("/registrar-entrada")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> registrarEntrada(@Valid @RequestBody RegistrarEntradaRequest entradaRequest,
                                            Authentication authentication) {
        // Obtener vigilante actual
        String username = authentication.getName();
        Optional<User> vigilanteOpt = userRepository.findByUsername(username);
        
        if (!vigilanteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vigilante no encontrado"));
        }

        User vigilante = vigilanteOpt.get();

        // Verificar que el cliente existe y es CLIENTE
        Optional<User> clienteOpt = userRepository.findById(entradaRequest.getClienteId());
        if (!clienteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = clienteOpt.get();
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El usuario seleccionado no es un cliente"));
        }

        // Verificar que el vehículo existe y pertenece al cliente
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(entradaRequest.getVehiculoId());
        if (!vehiculoOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vehículo no encontrado"));
        }

        Vehiculo vehiculo = vehiculoOpt.get();
        if (!vehiculo.getCliente().getId().equals(cliente.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El vehículo no pertenece al cliente seleccionado"));
        }

        // Verificar que el vehículo no esté ya estacionado
        Optional<RegistroEstacionamiento> registroActivoOpt = 
                registroRepository.findRegistroActivoPorVehiculo(vehiculo);
        
        if (registroActivoOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El vehículo ya se encuentra estacionado en el espacio " + 
                           registroActivoOpt.get().getEspacio().getNumero()));
        }

        // Verificar que el espacio existe
        Optional<EspacioEstacionamiento> espacioOpt = espacioRepository.findById(entradaRequest.getEspacioId());
        if (!espacioOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Espacio no encontrado"));
        }

        EspacioEstacionamiento espacio = espacioOpt.get();

        // Verificar el estado del espacio
        if (espacio.estaOcupado()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El espacio " + espacio.getNumero() + " está ocupado"));
        }

        if (espacio.estaEnMantenimiento()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El espacio " + espacio.getNumero() + " está en mantenimiento"));
        }

        // Si el espacio está reservado, verificar que la reserva sea del cliente
        if (espacio.estaReservado()) {
            LocalDateTime ahora = LocalDateTime.now();
            Optional<Reserva> reservaOpt = reservaRepository.findReservaConfirmadaActual(espacio, ahora);
            
            if (reservaOpt.isPresent()) {
                Reserva reserva = reservaOpt.get();
                if (!reserva.getCliente().getId().equals(cliente.getId())) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Error: El espacio " + espacio.getNumero() + 
                                   " está reservado por otro cliente"));
                }
                // Marcar la reserva como utilizada
                reserva.marcarComoUtilizada();
                reservaRepository.save(reserva);
            }
        }

        // Crear el registro de estacionamiento
        RegistroEstacionamiento registro = new RegistroEstacionamiento(vehiculo, espacio, vigilante);
        registro.setObservaciones(entradaRequest.getObservaciones());

        // Cambiar estado del espacio a OCUPADO
        espacio.ocupar();

        // Guardar cambios
        espacioRepository.save(espacio);
        registroRepository.save(registro);

        return ResponseEntity.ok(new MessageResponse(
                "Entrada registrada exitosamente. Cliente: " + cliente.getPersona().getNombreCompleto() + 
                ", Vehículo: " + vehiculo.getPlaca() + ", Espacio: " + espacio.getNumero()));
    }

    // ============= REGISTRO DE SALIDA (VIGILANTE) =============

    @PostMapping("/registrar-salida")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> registrarSalida(@Valid @RequestBody RegistrarSalidaRequest salidaRequest,
                                           Authentication authentication) {
        // Obtener vigilante actual
        String username = authentication.getName();
        Optional<User> vigilanteOpt = userRepository.findByUsername(username);
        
        if (!vigilanteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vigilante no encontrado"));
        }

        User vigilante = vigilanteOpt.get();

        // Verificar que el registro existe y está activo
        Optional<RegistroEstacionamiento> registroOpt = registroRepository.findById(salidaRequest.getRegistroId());
        if (!registroOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Registro no encontrado"));
        }

        RegistroEstacionamiento registro = registroOpt.get();

        if (registro.getEstado() != EstadoRegistro.ACTIVO) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El registro ya no está activo"));
        }

        // Finalizar el registro
        registro.finalizarRegistro(vigilante, salidaRequest.getObservaciones());

        // Liberar el espacio
        EspacioEstacionamiento espacio = registro.getEspacio();
        espacio.liberar();

        // Guardar cambios
        espacioRepository.save(espacio);
        registroRepository.save(registro);

        return ResponseEntity.ok(new MessageResponse(
                "Salida registrada exitosamente. Total a pagar: S/. " + registro.getMontoTotal() + 
                " (" + registro.getTotalHoras() + " horas)"));
    }

    // ============= MOVER CLIENTE A OTRO ESPACIO (ADMIN) =============

    @PutMapping("/mover-cliente")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> moverCliente(@Valid @RequestBody MoverClienteRequest moverRequest) {
        // Verificar que el registro existe y está activo
        Optional<RegistroEstacionamiento> registroOpt = registroRepository.findById(moverRequest.getRegistroId());
        if (!registroOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Registro no encontrado"));
        }

        RegistroEstacionamiento registro = registroOpt.get();

        if (registro.getEstado() != EstadoRegistro.ACTIVO) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El registro ya no está activo"));
        }

        // Verificar que el nuevo espacio existe
        Optional<EspacioEstacionamiento> nuevoEspacioOpt = espacioRepository.findById(moverRequest.getNuevoEspacioId());
        if (!nuevoEspacioOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Nuevo espacio no encontrado"));
        }

        EspacioEstacionamiento nuevoEspacio = nuevoEspacioOpt.get();

        // Verificar que el nuevo espacio está disponible
        if (!nuevoEspacio.estaDisponible()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El nuevo espacio " + nuevoEspacio.getNumero() + 
                           " no está disponible"));
        }

        // Liberar el espacio actual
        EspacioEstacionamiento espacioActual = registro.getEspacio();
        espacioActual.liberar();

        // Ocupar el nuevo espacio
        nuevoEspacio.ocupar();

        // Actualizar el registro
        registro.moverAEspacio(nuevoEspacio, moverRequest.getMotivo());

        // Guardar cambios
        espacioRepository.save(espacioActual);
        espacioRepository.save(nuevoEspacio);
        registroRepository.save(registro);

        return ResponseEntity.ok(new MessageResponse(
                "Cliente movido exitosamente del espacio " + espacioActual.getNumero() + 
                " al espacio " + nuevoEspacio.getNumero()));
    }

    // ============= VER REGISTROS =============

    // VIGILANTE puede ver registros activos
    @GetMapping("/registros-activos")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerRegistrosActivos() {
        List<RegistroEstacionamiento> registros = registroRepository.findRegistrosActivos();
        List<RegistroEstacionamientoResponse> registrosResponse = registros.stream()
                .map(this::convertirRegistroAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(registrosResponse);
    }

    // ADMIN puede ver todos los registros
    @GetMapping("/registros")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerTodosLosRegistros() {
        List<RegistroEstacionamiento> registros = registroRepository.findAll();
        List<RegistroEstacionamientoResponse> registrosResponse = registros.stream()
                .map(this::convertirRegistroAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(registrosResponse);
    }

    // ADMIN puede ver registros por estado
    @GetMapping("/registros/{estado}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerRegistrosPorEstado(@PathVariable String estado) {
        try {
            EstadoRegistro estadoEnum = EstadoRegistro.valueOf(estado.toUpperCase());
            List<RegistroEstacionamiento> registros = registroRepository.findByEstadoOrderByFechaHoraEntradaDesc(estadoEnum);
            
            List<RegistroEstacionamientoResponse> registrosResponse = registros.stream()
                    .map(this::convertirRegistroAResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(registrosResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Estado inválido. Use: ACTIVO, FINALIZADO o CANCELADO"));
        }
    }

    // ============= GESTIÓN DE RESERVAS (ADMIN/VIGILANTE) =============

    // Ver todas las reservas pendientes
    @GetMapping("/reservas/pendientes")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerReservasPendientes() {
        List<Reserva> reservas = reservaRepository.findByEstadoOrderByFechaReservaAsc(EstadoReserva.PENDIENTE);
        List<ReservaResponse> reservasResponse = reservas.stream()
                .map(this::convertirReservaAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reservasResponse);
    }

    // ADMIN puede aprobar/rechazar reserva
    @PutMapping("/reservas/{reservaId}/estado")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> actualizarEstadoReserva(@PathVariable Long reservaId,
                                                   @Valid @RequestBody ActualizarReservaRequest estadoRequest) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(reservaId);
        if (!reservaOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Reserva no encontrada"));
        }

        Reserva reserva = reservaOpt.get();

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo se pueden actualizar reservas pendientes"));
        }

        if (estadoRequest.getNuevoEstado() == EstadoReserva.CONFIRMADA) {
            // Verificar que el espacio esté disponible para el período de la reserva
            Optional<Reserva> reservaConflicto = reservaRepository.findReservaActivaEnEspacio(
                    reserva.getEspacio(), reserva.getFechaInicio(), reserva.getFechaFin());
            
            if (reservaConflicto.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: El espacio ya tiene una reserva confirmada en esas fechas"));
            }

            reserva.confirmar();
            // Cambiar estado del espacio a RESERVADO si la reserva es para ahora
            LocalDateTime ahora = LocalDateTime.now();
            if (reserva.getFechaInicio().isBefore(ahora) && reserva.getFechaFin().isAfter(ahora)) {
                reserva.getEspacio().reservar();
                espacioRepository.save(reserva.getEspacio());
            }

        } else if (estadoRequest.getNuevoEstado() == EstadoReserva.CANCELADA) {
            reserva.cancelar(estadoRequest.getComentario());
        }

        reservaRepository.save(reserva);

        String mensaje = estadoRequest.getNuevoEstado() == EstadoReserva.CONFIRMADA ? 
                "confirmada" : "cancelada";

        return ResponseEntity.ok(new MessageResponse("Reserva " + mensaje + " exitosamente"));
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
