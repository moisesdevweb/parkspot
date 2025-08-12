package com.parking.parkspot.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.parkspot.model.entity.Persona;
import com.parking.parkspot.repository.PersonaRepository;

import com.parking.parkspot.model.enums.ERole;
import com.parking.parkspot.model.entity.Role;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.payload.request.LoginRequest;
import com.parking.parkspot.payload.request.SignupRequest;
import com.parking.parkspot.payload.response.JwtResponse;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.repository.RoleRepository;
import com.parking.parkspot.repository.UserRepository;
import com.parking.parkspot.security.jwt.JwtUtils;
import com.parking.parkspot.security.service.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    PersonaRepository personaRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Primero verificar que el usuario existe y está activo
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }
        // Verificar que la persona asociada esté activa
        if (user.getPersona() == null || user.getPersona().getEstado() == 0) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario inactivo. Contacte al administrador"));
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        // Obtener datos de la persona (ya sabemos que user existe y tiene persona activa)
        String nombreCompleto = user.getPersona().getNombreCompleto();
        String apellidos = user.getPersona().getApellidos();
        String dni = user.getPersona().getDni();
        String direccion = user.getPersona().getDireccion();
        String telefono = user.getPersona().getTelefono();

        return ResponseEntity.ok(new JwtResponse(jwt,
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles,
            nombreCompleto,
            apellidos,
            dni,
            direccion,
            telefono));
    }

    // Registro público: solo permite crear clientes
    @PostMapping("/registrarme")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El nombre de usuario ya está tomado!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El correo electrónico ya está en uso!"));
        }
        // Validar que el DNI no exista
        if (personaRepository.existsByDni(signUpRequest.getDni())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El DNI ya está registrado!"));
        }

        // Crear la entidad Persona
        Persona persona = new Persona();
        persona.setNombreCompleto(signUpRequest.getNombreCompleto());
        persona.setApellidos(signUpRequest.getApellidos());
        persona.setDni(signUpRequest.getDni());
        persona.setDireccion(signUpRequest.getDireccion());
        persona.setTelefono(signUpRequest.getTelefono());
        persona.setEstado(1);
        personaRepository.save(persona);

        // Solo rol cliente
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));
        user.setPersona(persona);
        Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Error: No se encontró el rol."));
        Set<Role> roles = new HashSet<>();
        roles.add(clienteRole);
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado exitosamente!"));
    }

    // Registro por admin: puede crear admin, vigilante o cliente
    @PostMapping("/registrar-usuario")
    public ResponseEntity<?> registrarUsuarioPorAdmin(@Valid @RequestBody SignupRequest signUpRequest, Authentication authentication) {
        // Solo admin puede acceder
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(new MessageResponse("Acceso denegado: solo el admin puede registrar usuarios especiales."));
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El nombre de usuario ya está tomado!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El correo electrónico ya está en uso!"));
        }
        if (personaRepository.existsByDni(signUpRequest.getDni())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El DNI ya está registrado!"));
        }

        Persona persona = new Persona();
        persona.setNombreCompleto(signUpRequest.getNombreCompleto());
        persona.setApellidos(signUpRequest.getApellidos());
        persona.setDni(signUpRequest.getDni());
        persona.setDireccion(signUpRequest.getDireccion());
        persona.setTelefono(signUpRequest.getTelefono());
        persona.setEstado(1);
        personaRepository.save(persona);

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));
        user.setPersona(persona);
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty()) {
            Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                    .orElseThrow(() -> new RuntimeException("Error: No se encontró el rol."));
            roles.add(clienteRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: No se encontró el rol."));
                        roles.add(adminRole);
                        break;
                    case "vigilante":
                        Role vigilanteRole = roleRepository.findByName(ERole.ROLE_VIGILANTE)
                                .orElseThrow(() -> new RuntimeException("Error: No se encontró el rol."));
                        roles.add(vigilanteRole);
                        break;
                    default:
                        Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                                .orElseThrow(() -> new RuntimeException("Error: No se encontró el rol."));
                        roles.add(clienteRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Usuario registrado exitosamente por admin!"));
    }
}

