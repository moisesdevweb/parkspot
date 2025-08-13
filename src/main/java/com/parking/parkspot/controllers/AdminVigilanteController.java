package com.parking.parkspot.controllers;

import com.parking.parkspot.model.entity.*;
import com.parking.parkspot.model.enums.ERole;
import com.parking.parkspot.payload.request.*;
import com.parking.parkspot.payload.response.*;
import com.parking.parkspot.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin-vigilante")
public class AdminVigilanteController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PersonaRepository personaRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;

    // --- Endpoints de gestión de vigilantes por admin ---

    // Listar vigilantes
    @GetMapping("/listar-vigilantes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> listarVigilantes() {
        List<User> usuarios = userRepository.findAll();
        List<PerfilCompletoResponse> vigilantes = usuarios.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE")))
                .distinct()
                .map(user -> {
                    List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList());
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
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(vigilantes);
    }

    // Buscar vigilantes por nombre
    @GetMapping("/buscar-vigilantes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> buscarVigilantes(@RequestParam String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El parámetro 'nombre' es requerido"));
        }
        List<User> usuarios = userRepository.findAll();
        List<PerfilCompletoResponse> vigilantes = usuarios.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE")))
                .filter(user -> user.getPersona() != null &&
                        (user.getPersona().getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()) ||
                         user.getPersona().getApellidos().toLowerCase().contains(nombre.toLowerCase())))
                .distinct()
                .map(user -> {
                    List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList());
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
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(vigilantes);
    }

    // Ver perfil específico de vigilante
    @GetMapping("/vigilante/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getPerfilVigilante(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        boolean esVigilante = user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE"));
        if (!esVigilante) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un vigilante"));
        }
        if (user.getPersona() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        
        List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList());
        PerfilCompletoResponse perfil = new PerfilCompletoResponse(
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
        return ResponseEntity.ok(perfil);
    }

    // Editar perfil de vigilante
    @PutMapping("/vigilante/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updatePerfilVigilante(@PathVariable Long id, @Valid @RequestBody AdminUpdatePerfilRequest updateRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        boolean esVigilante = user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE"));
        if (!esVigilante) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes editar perfiles de vigilantes"));
        }
        Persona persona = user.getPersona();
        if (persona == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        
        // Validar email único
        Optional<User> emailExistente = userRepository.findByEmail(updateRequest.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El email ya está en uso"));
        }
        
        // Validar DNI único
        Optional<Persona> dniExistente = personaRepository.findByDni(updateRequest.getDni());
        if (dniExistente.isPresent() && !dniExistente.get().getId().equals(persona.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El DNI ya está en uso"));
        }
        
        // Actualizar datos
        user.setEmail(updateRequest.getEmail());
        persona.setNombreCompleto(updateRequest.getNombreCompleto());
        persona.setApellidos(updateRequest.getApellidos());
        persona.setDni(updateRequest.getDni());
        persona.setDireccion(updateRequest.getDireccion());
        persona.setTelefono(updateRequest.getTelefono());
        
        // Actualizar estado si se proporciona
        if (updateRequest.getEstado() != null) {
            persona.setEstado(updateRequest.getEstado());
        }
        
        userRepository.save(user);
        personaRepository.save(persona);
        return ResponseEntity.ok(new MessageResponse("Perfil del vigilante actualizado exitosamente"));
    }

    // Cambiar estado de vigilante
    @PutMapping("/vigilante/estado/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cambiarEstadoVigilante(@PathVariable Long id, @RequestBody EstadoRequest estadoRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        boolean esVigilante = user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE"));
        if (!esVigilante) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes cambiar el estado de vigilantes"));
        }
        if (user.getPersona() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        
        user.getPersona().setEstado(estadoRequest.getEstado());
        personaRepository.save(user.getPersona());
        
        String mensaje = estadoRequest.getEstado() == 1 ? "Vigilante activado" : "Vigilante desactivado";
        return ResponseEntity.ok(new MessageResponse(mensaje + " exitosamente"));
    }

    // Registrar nuevo vigilante
    @PostMapping("/registrar-vigilante")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> registrarVigilante(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El nombre de usuario ya está tomado"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El correo electrónico ya está en uso"));
        }
        if (personaRepository.existsByDni(signUpRequest.getDni())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El DNI ya está registrado"));
        }
        
        // Crear persona
        Persona persona = new Persona();
        persona.setNombreCompleto(signUpRequest.getNombreCompleto());
        persona.setApellidos(signUpRequest.getApellidos());
        persona.setDni(signUpRequest.getDni());
        persona.setDireccion(signUpRequest.getDireccion());
        persona.setTelefono(signUpRequest.getTelefono());
        persona.setEstado(1); // Activo por defecto
        personaRepository.save(persona);
        
        // Crear usuario
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));
        
        // Asignar rol de vigilante
        Set<Role> roles = new HashSet<>();
        Role vigilanteRole = roleRepository.findByName(ERole.ROLE_VIGILANTE)
                .orElseThrow(() -> new RuntimeException("Error: Rol vigilante no encontrado"));
        roles.add(vigilanteRole);
        
        user.setRoles(roles);
        user.setPersona(persona);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("Vigilante registrado exitosamente"));
    }

    // Eliminar vigilante (cambiar estado a inactivo)
    @DeleteMapping("/vigilante/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> eliminarVigilante(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        boolean esVigilante = user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE"));
        if (!esVigilante) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un vigilante"));
        }
        if (user.getPersona() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        
        // En lugar de eliminar, desactivamos el vigilante
        user.getPersona().setEstado(0);
        personaRepository.save(user.getPersona());
        
        return ResponseEntity.ok(new MessageResponse("Vigilante desactivado exitosamente"));
    }
}
