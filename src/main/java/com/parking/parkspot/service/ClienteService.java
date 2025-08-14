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
}
