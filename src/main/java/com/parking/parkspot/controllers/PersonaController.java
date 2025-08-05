package com.parking.parkspot.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.model.entity.Persona;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.payload.request.EstadoRequest;
import com.parking.parkspot.payload.request.UpdatePerfilRequest;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.PerfilCompletoResponse;
import com.parking.parkspot.repository.PersonaRepository;
import com.parking.parkspot.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/persona")
public class PersonaController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PersonaRepository personaRepository;

    // Obtener perfil de cualquier usuario (ROLE_CLIENTE, ROLE_VIGILANTE, ROLE_ADMIN)
    @GetMapping("/perfil/{username}")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getPerfilUsuario(@PathVariable String username, 
                                             Authentication authentication) {
        // Verificar que el usuario solo pueda ver su propio perfil
        String currentUsername = authentication.getName();
        if (!currentUsername.equals(username)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo puedes ver tu propio perfil"));
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        if (user.getPersona() == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        PerfilCompletoResponse perfil = new PerfilCompletoResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPersona().getNombreCompleto(),
            user.getPersona().getApellidos(),
            user.getPersona().getDni(),
            user.getPersona().getDireccion(),
            user.getPersona().getTelefono(),
            user.getPersona().getEstado()
        );

        return ResponseEntity.ok(perfil);
    }

    // Actualizar perfil (cada usuario solo puede editar su propio perfil)
    @PutMapping("/perfil/{username}")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updatePerfilUsuario(@PathVariable String username, 
                                                @Valid @RequestBody UpdatePerfilRequest updateRequest,
                                                Authentication authentication) {
        // Verificar que el usuario solo pueda editar su propio perfil
        String currentUsername = authentication.getName();
        if (!currentUsername.equals(username)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo puedes editar tu propio perfil"));
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        Persona persona = user.getPersona();
        
        if (persona == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        // Actualizar campos editables
        persona.setNombreCompleto(updateRequest.getNombreCompleto());
        persona.setApellidos(updateRequest.getApellidos());
        persona.setDireccion(updateRequest.getDireccion());
        persona.setTelefono(updateRequest.getTelefono());

        personaRepository.save(persona);

        return ResponseEntity.ok(new MessageResponse("Perfil actualizado exitosamente"));
    }

    // ROLE_ADMIN puede cambiar estado de ROLE_CLIENTE y ROLE_VIGILANTE
    @PutMapping("/estado/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cambiarEstadoUsuario(@PathVariable Long id, 
                                                 @RequestBody EstadoRequest estadoRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        if (user.getPersona() == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        user.getPersona().setEstado(estadoRequest.getEstado());
        personaRepository.save(user.getPersona());

        String mensaje = estadoRequest.getEstado() == 1 ? "Usuario activado" : "Usuario desactivado";
        return ResponseEntity.ok(new MessageResponse(mensaje + " exitosamente"));
    }

    // ROLE_VIGILANTE puede cambiar estado solo de ROLE_CLIENTE
    @PutMapping("/estado-cliente/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> cambiarEstadoCliente(@PathVariable Long id, 
                                                 @RequestBody EstadoRequest estadoRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        
        // Verificar que sea un ROLE_CLIENTE
        boolean esCliente = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo puedes cambiar el estado de clientes"));
        }

        if (user.getPersona() == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        user.getPersona().setEstado(estadoRequest.getEstado());
        personaRepository.save(user.getPersona());

        String mensaje = estadoRequest.getEstado() == 1 ? "Cliente activado" : "Cliente desactivado";
        return ResponseEntity.ok(new MessageResponse(mensaje + " exitosamente"));
    }

    // Listar todos los usuarios (solo ROLE_ADMIN)
    @GetMapping("/listar")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> listarUsuarios() {
        List<User> usuarios = userRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }
}
