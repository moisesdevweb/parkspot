package com.parking.parkspot.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.parking.parkspot.payload.response.EspacioDragDropResponse;
import com.parking.parkspot.payload.response.RegistroEstacionamientoResponse;
import com.parking.parkspot.payload.response.ReservaResponse;
import com.parking.parkspot.repository.EspacioEstacionamientoRepository;
import com.parking.parkspot.repository.RegistroEstacionamientoRepository;
import com.parking.parkspot.repository.ReservaRepository;
import com.parking.parkspot.repository.UserRepository;
import com.parking.parkspot.repository.VehiculoRepository;
import com.parking.parkspot.service.exception.*;

@Service
@Transactional
public class EstacionamientoService {

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

    public List<EspacioEstacionamientoResponse> obtenerTodosLosEspacios() {
        List<EspacioEstacionamiento> espacios = espacioRepository.findAll();
        return espacios.stream()
                .map(this::convertirEspacioAResponse)
                .collect(Collectors.toList());
    }

    public List<EspacioEstacionamientoResponse> obtenerEspaciosDisponibles() {
        List<EspacioEstacionamiento> espacios = espacioRepository.findByEstadoOrderByNumeroAsc(EstadoEspacio.DISPONIBLE);
        return espacios.stream()
                .map(this::convertirEspacioAResponse)
                .collect(Collectors.toList());
    }

    // ============= ESPACIOS PARA DRAG & DROP =============
    
    public List<EspacioDragDropResponse> obtenerEspaciosConClientes() {
        List<EspacioEstacionamiento> todosLosEspacios = espacioRepository.findAll();
        
        return todosLosEspacios.stream()
                .map(this::convertirEspacioParaDragDrop)
                .collect(Collectors.toList());
    }

    // ============= REGISTRO DE ENTRADA =============

    public String registrarEntrada(RegistrarEntradaRequest entradaRequest, User vigilante) {
        // Verificar que el cliente existe y es CLIENTE
        User cliente = buscarClientePorId(entradaRequest.getClienteId());

        // Verificar que el vehículo existe y pertenece al cliente
        Vehiculo vehiculo = buscarVehiculoPorId(entradaRequest.getVehiculoId());
        validarVehiculoPerteneceACliente(vehiculo, cliente);

        // Verificar que el vehículo no esté ya estacionado
        validarVehiculoNoEstaEstacionado(vehiculo);

        // Verificar que el espacio existe y está disponible
        EspacioEstacionamiento espacio = buscarEspacioPorId(entradaRequest.getEspacioId());
        validarEspacioParaEntrada(espacio, cliente);

        // Si el espacio está reservado, validar y marcar reserva como utilizada
        procesarReservaEnEspacio(espacio, cliente);

        // Crear el registro de estacionamiento
        RegistroEstacionamiento registro = new RegistroEstacionamiento(vehiculo, espacio, vigilante);
        registro.setObservaciones(entradaRequest.getObservaciones());

        // Cambiar estado del espacio a OCUPADO
        espacio.ocupar();

        // Guardar cambios
        espacioRepository.save(espacio);
        registroRepository.save(registro);

        return "Entrada registrada exitosamente. Cliente: " + cliente.getPersona().getNombreCompleto() + 
               ", Vehículo: " + vehiculo.getPlaca() + ", Espacio: " + espacio.getNumero();
    }

    // ============= REGISTRO DE SALIDA =============

    public String registrarSalida(RegistrarSalidaRequest salidaRequest, User vigilante) {
        // Verificar que el registro existe y está activo
        RegistroEstacionamiento registro = buscarRegistroPorId(salidaRequest.getRegistroId());

        if (registro.getEstado() != EstadoRegistro.ACTIVO) {
            throw new InvalidOperationException("El registro ya no está activo");
        }

        // Finalizar el registro
        registro.finalizarRegistro(vigilante, salidaRequest.getObservaciones());

        // Liberar el espacio
        EspacioEstacionamiento espacio = registro.getEspacio();
        espacio.liberar();

        // Guardar cambios
        espacioRepository.save(espacio);
        registroRepository.save(registro);

        return "Salida registrada exitosamente. Total a pagar: S/. " + registro.getMontoTotal() + 
               " (" + registro.getTotalHoras() + " horas)";
    }

    // ============= MOVER CLIENTE =============

    public String moverCliente(MoverClienteRequest moverRequest) {
        // Verificar que el registro existe y está activo
        RegistroEstacionamiento registro = buscarRegistroPorId(moverRequest.getRegistroId());

        if (registro.getEstado() != EstadoRegistro.ACTIVO) {
            throw new InvalidOperationException("El registro ya no está activo");
        }

        // Verificar que el nuevo espacio existe y está disponible
        EspacioEstacionamiento nuevoEspacio = buscarEspacioPorId(moverRequest.getNuevoEspacioId());

        if (!nuevoEspacio.estaDisponible()) {
            throw new InvalidOperationException("El nuevo espacio " + nuevoEspacio.getNumero() + " no está disponible");
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

        return "Cliente movido exitosamente del espacio " + espacioActual.getNumero() + 
               " al espacio " + nuevoEspacio.getNumero();
    }

    // ============= VER REGISTROS =============

    public List<RegistroEstacionamientoResponse> obtenerRegistrosActivos() {
        List<RegistroEstacionamiento> registros = registroRepository.findRegistrosActivos();
        return registros.stream()
                .map(this::convertirRegistroAResponse)
                .collect(Collectors.toList());
    }

    public List<RegistroEstacionamientoResponse> obtenerTodosLosRegistros() {
        List<RegistroEstacionamiento> registros = registroRepository.findAll();
        return registros.stream()
                .map(this::convertirRegistroAResponse)
                .collect(Collectors.toList());
    }

    public List<RegistroEstacionamientoResponse> obtenerRegistrosPorEstado(String estado) {
        try {
            EstadoRegistro estadoEnum = EstadoRegistro.valueOf(estado.toUpperCase());
            List<RegistroEstacionamiento> registros = registroRepository.findByEstadoOrderByFechaHoraEntradaDesc(estadoEnum);
            
            return registros.stream()
                    .map(this::convertirRegistroAResponse)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido. Use: ACTIVO, FINALIZADO o CANCELADO");
        }
    }

    // ============= GESTIÓN DE RESERVAS =============

    public List<ReservaResponse> obtenerReservasPendientes() {
        List<Reserva> reservas = reservaRepository.findByEstadoOrderByFechaReservaAsc(EstadoReserva.PENDIENTE);
        return reservas.stream()
                .map(this::convertirReservaAResponse)
                .collect(Collectors.toList());
    }

    public String actualizarEstadoReserva(Long reservaId, ActualizarReservaRequest estadoRequest) {
        Reserva reserva = buscarReservaPorId(reservaId);

        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new InvalidOperationException("Solo se pueden actualizar reservas pendientes");
        }

        if (estadoRequest.getNuevoEstado() == EstadoReserva.CONFIRMADA) {
            // Verificar que el espacio esté disponible para el período de la reserva
            Optional<Reserva> reservaConflicto = reservaRepository.findReservaActivaEnEspacio(
                    reserva.getEspacio(), reserva.getFechaInicio(), reserva.getFechaFin());
            
            if (reservaConflicto.isPresent()) {
                throw new InvalidOperationException("El espacio ya tiene una reserva confirmada en esas fechas");
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

        String mensaje = estadoRequest.getNuevoEstado() == EstadoReserva.CONFIRMADA ? "confirmada" : "cancelada";
        return "Reserva " + mensaje + " exitosamente";
    }

    // ============= MÉTODOS PRIVADOS DE VALIDACIÓN =============

    private User buscarClientePorId(Long clienteId) {
        Optional<User> clienteOpt = userRepository.findById(clienteId);
        if (!clienteOpt.isPresent()) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }

        User cliente = clienteOpt.get();
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            throw new InvalidOperationException("El usuario seleccionado no es un cliente");
        }

        return cliente;
    }

    private Vehiculo buscarVehiculoPorId(Long vehiculoId) {
        return vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
    }

    private EspacioEstacionamiento buscarEspacioPorId(Long espacioId) {
        return espacioRepository.findById(espacioId)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado"));
    }

    private RegistroEstacionamiento buscarRegistroPorId(Long registroId) {
        return registroRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
    }

    private Reserva buscarReservaPorId(Long reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    private void validarVehiculoPerteneceACliente(Vehiculo vehiculo, User cliente) {
        if (!vehiculo.getCliente().getId().equals(cliente.getId())) {
            throw new InvalidOperationException("El vehículo no pertenece al cliente seleccionado");
        }
    }

    private void validarVehiculoNoEstaEstacionado(Vehiculo vehiculo) {
        Optional<RegistroEstacionamiento> registroActivoOpt = registroRepository.findRegistroActivoPorVehiculo(vehiculo);
        
        if (registroActivoOpt.isPresent()) {
            throw new InvalidOperationException("El vehículo ya se encuentra estacionado en el espacio " + 
                   registroActivoOpt.get().getEspacio().getNumero());
        }
    }

    private void validarEspacioParaEntrada(EspacioEstacionamiento espacio, User cliente) {
        if (espacio.estaOcupado()) {
            throw new InvalidOperationException("El espacio " + espacio.getNumero() + " está ocupado");
        }

        if (espacio.estaEnMantenimiento()) {
            throw new InvalidOperationException("El espacio " + espacio.getNumero() + " está en mantenimiento");
        }
    }

    private void procesarReservaEnEspacio(EspacioEstacionamiento espacio, User cliente) {
        if (espacio.estaReservado()) {
            LocalDateTime ahora = LocalDateTime.now();
            Optional<Reserva> reservaOpt = reservaRepository.findReservaConfirmadaActual(espacio, ahora);
            
            if (reservaOpt.isPresent()) {
                Reserva reserva = reservaOpt.get();
                if (!reserva.getCliente().getId().equals(cliente.getId())) {
                    throw new InvalidOperationException("El espacio " + espacio.getNumero() + 
                           " está reservado por otro cliente");
                }
                // Marcar la reserva como utilizada
                reserva.marcarComoUtilizada();
                reservaRepository.save(reserva);
            }
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

    private EspacioDragDropResponse convertirEspacioParaDragDrop(EspacioEstacionamiento espacio) {
        // Si el espacio está disponible, solo devolver la información básica
        if (espacio.estaDisponible()) {
            return new EspacioDragDropResponse(
                    espacio.getId(),
                    espacio.getNumero(),
                    espacio.getTipo(),
                    espacio.getEstado(),
                    espacio.getDescripcion(),
                    espacio.getTarifaPorHora()
            );
        }
        
        // Si el espacio está ocupado, buscar el registro activo
        Optional<RegistroEstacionamiento> registroOpt = registroRepository
                .findRegistroActivoPorEspacio(espacio);
                
        if (registroOpt.isPresent()) {
            RegistroEstacionamiento registro = registroOpt.get();
            return new EspacioDragDropResponse(
                    espacio.getId(),
                    espacio.getNumero(),
                    espacio.getTipo(),
                    espacio.getEstado(),
                    espacio.getDescripcion(),
                    espacio.getTarifaPorHora(),
                    registro.getId(),
                    registro.getVehiculo().getCliente().getPersona().getNombreCompleto(),
                    registro.getVehiculo().getCliente().getPersona().getApellidos(),
                    registro.getVehiculo().getCliente().getPersona().getDni(),
                    registro.getVehiculo().getPlaca(),
                    registro.getVehiculo().getTipo().toString()
            );
        }
        
        // Fallback si no hay registro (no debería pasar)
        return new EspacioDragDropResponse(
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
