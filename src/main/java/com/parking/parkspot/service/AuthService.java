package com.parking.parkspot.service;

import com.parking.parkspot.model.entity.*;
import com.parking.parkspot.model.enums.ERole;
import com.parking.parkspot.payload.request.SignupRequest;
import com.parking.parkspot.repository.*;
import com.parking.parkspot.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;

    public User registrarUsuario(SignupRequest signUpRequest, Set<String> strRoles) {
        // Validaciones de unicidad
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new DuplicateResourceException("El nombre de usuario ya está tomado");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new DuplicateResourceException("El correo electrónico ya está en uso");
        }
        if (personaRepository.existsByDni(signUpRequest.getDni())) {
            throw new DuplicateResourceException("El DNI ya está registrado");
        }

        // Crear persona
        Persona persona = new Persona();
        persona.setNombreCompleto(signUpRequest.getNombreCompleto());
        persona.setApellidos(signUpRequest.getApellidos());
        persona.setDni(signUpRequest.getDni());
        persona.setDireccion(signUpRequest.getDireccion());
        persona.setTelefono(signUpRequest.getTelefono());
        persona.setEstado(1);
        personaRepository.save(persona);

        // Crear usuario
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), 
                           encoder.encode(signUpRequest.getPassword()));

        // Asignar roles
        Set<Role> roles = determinarRoles(strRoles);
        user.setRoles(roles);
        user.setPersona(persona);

        return userRepository.save(user);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Set<Role> determinarRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));
            roles.add(clienteRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));
                        roles.add(adminRole);
                        break;
                    case "vigilante":
                        Role vigilanteRole = roleRepository.findByName(ERole.ROLE_VIGILANTE)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));
                        roles.add(vigilanteRole);
                        break;
                    default:
                        Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));
                        roles.add(clienteRole);
                }
            });
        }

        return roles;
    }
}
