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
public class VigilanteService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;

    public List<PerfilCompletoResponse> listarTodosLosVigilantes() {
        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .filter(this::esVigilante)
                .distinct()
                .map(this::convertirAPerfilCompleto)
                .collect(Collectors.toList());
    }

    public List<PerfilCompletoResponse> buscarVigilantesPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El parámetro 'nombre' es requerido");
        }

        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .filter(this::esVigilante)
                .filter(user -> user.getPersona() != null &&
                        (user.getPersona().getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()) ||
                         user.getPersona().getApellidos().toLowerCase().contains(nombre.toLowerCase())))
                .distinct()
                .map(this::convertirAPerfilCompleto)
                .collect(Collectors.toList());
    }

    public PerfilCompletoResponse obtenerPerfilVigilante(Long vigilanteId) {
        User vigilante = buscarVigilantePorId(vigilanteId);
        
        if (vigilante.getPersona() == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        return convertirAPerfilCompleto(vigilante);
    }

    public void actualizarPerfilVigilante(Long vigilanteId, AdminUpdatePerfilRequest request) {
        User vigilante = buscarVigilantePorId(vigilanteId);
        Persona persona = vigilante.getPersona();

        if (persona == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        // Validar unicidad de email
        validarEmailUnico(request.getEmail(), vigilante.getId());
        
        // Validar unicidad de DNI
        validarDniUnico(request.getDni(), persona.getId());

        // Actualizar datos
        vigilante.setEmail(request.getEmail());
        persona.setNombreCompleto(request.getNombreCompleto());
        persona.setApellidos(request.getApellidos());
        persona.setDni(request.getDni());
        persona.setDireccion(request.getDireccion());
        persona.setTelefono(request.getTelefono());

        if (request.getEstado() != null) {
            persona.setEstado(request.getEstado());
        }

        userRepository.save(vigilante);
        personaRepository.save(persona);
    }

    public void cambiarEstadoVigilante(Long vigilanteId, Integer nuevoEstado) {
        User vigilante = buscarVigilantePorId(vigilanteId);
        
        if (vigilante.getPersona() == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        vigilante.getPersona().setEstado(nuevoEstado);
        personaRepository.save(vigilante.getPersona());
    }

    public User registrarVigilante(SignupRequest request) {
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

        // Asignar rol de vigilante
        Set<Role> roles = new HashSet<>();
        Role vigilanteRole = roleRepository.findByName(ERole.ROLE_VIGILANTE)
                .orElseThrow(() -> new RuntimeException("Rol vigilante no encontrado"));
        roles.add(vigilanteRole);

        user.setRoles(roles);
        user.setPersona(persona);
        return userRepository.save(user);
    }

    public void desactivarVigilante(Long vigilanteId) {
        User vigilante = buscarVigilantePorId(vigilanteId);
        
        if (vigilante.getPersona() == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        // En lugar de eliminar, desactivamos el vigilante
        vigilante.getPersona().setEstado(0);
        personaRepository.save(vigilante.getPersona());
    }

    // ========== MÉTODOS PRIVADOS ==========

    private User buscarVigilantePorId(Long vigilanteId) {
        Optional<User> userOpt = userRepository.findById(vigilanteId);
        if (!userOpt.isPresent()) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        User user = userOpt.get();
        if (!esVigilante(user)) {
            throw new InvalidOperationException("El usuario no es un vigilante");
        }
        
        return user;
    }

    private boolean esVigilante(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE"));
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
            null // Los vigilantes no tienen vehículos
        );
    }
}
