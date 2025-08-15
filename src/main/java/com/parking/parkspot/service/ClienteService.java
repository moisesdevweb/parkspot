package com.parking.parkspot.service;

import com.parking.parkspot.model.entity.*;
import com.parking.parkspot.model.enums.ERole;
import com.parking.parkspot.payload.request.*;
import com.parking.parkspot.payload.response.*;
import com.parking.parkspot.repository.*;
import com.parking.parkspot.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private EspacioEstacionamientoRepository espacioRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private VehiculoService vehiculoService;

    public List<PerfilCompletoResponse> buscarClientesPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El parámetro 'nombre' es requerido");
        }

        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .filter(user -> esCliente(user))
                .filter(user -> user.getPersona() != null &&
                        (user.getPersona().getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()) ||
                         user.getPersona().getApellidos().toLowerCase().contains(nombre.toLowerCase())))
                .distinct()
                .map(this::convertirAPerfilCompleto)
                .collect(Collectors.toList());
    }

    public void actualizarPerfilCliente(Long clienteId, AdminUpdatePerfilRequest request) {
        User cliente = buscarClientePorId(clienteId);
        Persona persona = cliente.getPersona();

        if (persona == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        // Validar unicidad de email
        validarEmailUnico(request.getEmail(), cliente.getId());
        
        // Validar unicidad de DNI
        validarDniUnico(request.getDni(), persona.getId());

        // Actualizar datos
        cliente.setEmail(request.getEmail());
        persona.setNombreCompleto(request.getNombreCompleto());
        persona.setApellidos(request.getApellidos());
        persona.setDni(request.getDni());
        persona.setDireccion(request.getDireccion());
        persona.setTelefono(request.getTelefono());

        if (request.getEstado() != null) {
            persona.setEstado(request.getEstado());
        }

        userRepository.save(cliente);
        personaRepository.save(persona);
    }

    public void cambiarEstadoCliente(Long clienteId, Integer nuevoEstado) {
        User cliente = buscarClientePorId(clienteId);
        
        if (cliente.getPersona() == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        cliente.getPersona().setEstado(nuevoEstado);
        personaRepository.save(cliente.getPersona());
    }

    public List<PerfilCompletoResponse> listarTodosLosClientes() {
        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .filter(this::esCliente)
                .distinct()
                .map(this::convertirAPerfilCompleto)
                .collect(Collectors.toList());
    }

    public PerfilCompletoResponse obtenerPerfilCliente(Long clienteId) {
        User cliente = buscarClientePorId(clienteId);
        
        if (cliente.getPersona() == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        return convertirAPerfilCompletoSinRoles(cliente);
    }

    public void actualizarClienteCompleto(Long clienteId, AdminUpdateClienteCompletoRequest request) {
        User cliente = buscarClientePorId(clienteId);
        Persona persona = cliente.getPersona();

        if (persona == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        // Validar unicidad
        validarEmailUnico(request.getEmail(), cliente.getId());
        validarDniUnico(request.getDni(), persona.getId());

        // Actualizar datos personales
        cliente.setEmail(request.getEmail());
        persona.setNombreCompleto(request.getNombreCompleto());
        persona.setApellidos(request.getApellidos());
        persona.setDni(request.getDni());
        persona.setDireccion(request.getDireccion());
        persona.setTelefono(request.getTelefono());

        if (request.getEstado() != null) {
            persona.setEstado(request.getEstado());
        }

        userRepository.save(cliente);
        personaRepository.save(persona);

        // Procesar vehículos si se proporcionan
        if (request.getVehiculos() != null && !request.getVehiculos().isEmpty()) {
            vehiculoService.actualizarVehiculosDeCliente(cliente, request.getVehiculos());
        }
    }

    public User registrarClienteCompleto(SignupCompletoRequest request) {
        // Validaciones de unicidad
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("El nombre de usuario ya está tomado");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("El correo electrónico ya está en uso");
        }
        if (personaRepository.existsByDni(request.getDni())) {
            throw new DuplicateResourceException("El DNI ya está registrado");
        }
        if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
            throw new DuplicateResourceException("La placa ya está registrada");
        }

        // Crear persona
        Persona persona = new Persona();
        persona.setNombreCompleto(request.getNombreCompleto());
        persona.setApellidos(request.getApellidos());
        persona.setDni(request.getDni());
        persona.setDireccion(request.getDireccion());
        persona.setTelefono(request.getTelefono());
        persona.setEstado(1);
        personaRepository.save(persona);

        // Crear usuario
        User user = new User(request.getUsername(), request.getEmail(), encoder.encode(request.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Rol cliente no encontrado"));
        roles.add(clienteRole);
        user.setRoles(roles);
        user.setPersona(persona);
        userRepository.save(user);

        // Crear vehículo
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(request.getPlaca());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setColor(request.getColor());
        vehiculo.setAño(request.getAño());
        vehiculo.setTipo(request.getTipoVehiculo());
        vehiculo.setCliente(user);
        vehiculo.setEstado(1);
        vehiculoRepository.save(vehiculo);

        return user;
    }

    public List<PerfilCompletoResponse> buscarClienteParaReporte(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El parámetro 'nombre' es requerido");
        }

        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .filter(this::esCliente)
                .filter(user -> user.getPersona() != null &&
                        (user.getPersona().getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()) ||
                         user.getPersona().getApellidos().toLowerCase().contains(nombre.toLowerCase()) ||
                         (user.getPersona().getNombreCompleto() + " " + user.getPersona().getApellidos())
                                .toLowerCase().contains(nombre.toLowerCase())))
                .distinct()
                .map(this::convertirAPerfilCompletoSinRoles)
                .collect(Collectors.toList());
    }

    // ========== MÉTODOS PRIVADOS ==========

    private User buscarClientePorId(Long clienteId) {
        Optional<User> userOpt = userRepository.findById(clienteId);
        if (!userOpt.isPresent()) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        
        User user = userOpt.get();
        if (!esCliente(user)) {
            throw new InvalidOperationException("El usuario no es un cliente");
        }
        
        return user;
    }

    private boolean esCliente(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
    }

    private void validarEmailUnico(String email, Long usuarioId) {
        Optional<User> emailExistente = userRepository.findByEmail(email);
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(usuarioId)) {
            throw new DuplicateResourceException("El email ya está en uso");
        }
    }

    private void validarDniUnico(String dni, Long personaId) {
        Optional<Persona> dniExistente = personaRepository.findByDni(dni);
        if (dniExistente.isPresent() && !dniExistente.get().getId().equals(personaId)) {
            throw new DuplicateResourceException("El DNI ya está en uso");
        }
    }

    private PerfilCompletoResponse convertirAPerfilCompleto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        
        List<VehiculoBasicoResponse> vehiculos = vehiculoService.obtenerVehiculosBasicosDeCliente(user);

        return new PerfilCompletoResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPersona().getNombreCompleto(),
            user.getPersona().getApellidos(),
            user.getPersona().getDni(),
            user.getPersona().getDireccion(),
            user.getPersona().getTelefono(),
            user.getPersona().getEstado(),
            roles,
            vehiculos
        );
    }

    private PerfilCompletoResponse convertirAPerfilCompletoSinRoles(User user) {
        List<VehiculoBasicoResponse> vehiculos = vehiculoService.obtenerVehiculosBasicosDeCliente(user);

        return new PerfilCompletoResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPersona().getNombreCompleto(),
            user.getPersona().getApellidos(),
            user.getPersona().getDni(),
            user.getPersona().getDireccion(),
            user.getPersona().getTelefono(),
            user.getPersona().getEstado(),
            null,
            vehiculos
        );
    }

    // ============= CREAR RESERVA =============
    
    public String crearReserva(CrearReservaRequest request, User cliente) {
        LocalDateTime ahora = LocalDateTime.now();
        
        // Validar que las fechas sean lógicas
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new InvalidOperationException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        // Validación flexible: permitir reservas desde mañana en adelante
        LocalDateTime inicioDelDiaSiguiente = ahora.toLocalDate().plusDays(1).atStartOfDay();
        
        if (request.getFechaInicio().isBefore(inicioDelDiaSiguiente)) {
            throw new InvalidOperationException("Las reservas deben ser a partir del día siguiente (desde las 00:00)");
        }
        
        // Validar que la duración máxima sea razonable (ej: máximo 3 días)
        if (request.getFechaInicio().plusDays(3).isBefore(request.getFechaFin())) {
            throw new InvalidOperationException("La reserva no puede exceder 3 días de duración");
        }
        
        // Validar límite de reservas por cliente (máximo 2 reservas activas)
        long reservasActivas = reservaRepository.countReservasActivasPorCliente(cliente);
        if (reservasActivas >= 2) {
            throw new InvalidOperationException("No puedes tener más de 2 reservas activas. " +
                    "Cancela o espera a que se utilice una reserva existente.");
        }
        
        // Validar que el vehículo existe y pertenece al cliente
        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
                
        if (!vehiculo.getCliente().getId().equals(cliente.getId())) {
            throw new InvalidOperationException("El vehículo no pertenece al cliente");
        }
        
        // Validar que el espacio existe y está disponible
        EspacioEstacionamiento espacio = espacioRepository.findById(request.getEspacioId())
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado"));
                
        if (!espacio.estaDisponible()) {
            throw new InvalidOperationException("El espacio " + espacio.getNumero() + " no está disponible");
        }
        
        // Verificar que no hay conflictos de horarios en ese espacio
        List<Reserva> reservasConflicto = reservaRepository.findReservasEnRangoFecha(
                espacio, request.getFechaInicio(), request.getFechaFin());
                
        if (!reservasConflicto.isEmpty()) {
            throw new InvalidOperationException("Ya existe una reserva en ese horario para el espacio " + espacio.getNumero());
        }
        
        // Crear la nueva reserva
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setCliente(cliente);
        nuevaReserva.setVehiculo(vehiculo);
        nuevaReserva.setEspacio(espacio);
        nuevaReserva.setFechaReserva(java.time.LocalDateTime.now());
        nuevaReserva.setFechaInicio(request.getFechaInicio());
        nuevaReserva.setFechaFin(request.getFechaFin());
        nuevaReserva.setEstado(com.parking.parkspot.model.enums.EstadoReserva.PENDIENTE);
        nuevaReserva.setObservaciones(request.getObservaciones());
        
        // Guardar la reserva
        reservaRepository.save(nuevaReserva);
        
        return "Reserva creada exitosamente para el espacio " + espacio.getNumero() + 
               " del " + request.getFechaInicio() + " al " + request.getFechaFin();
    }
}
