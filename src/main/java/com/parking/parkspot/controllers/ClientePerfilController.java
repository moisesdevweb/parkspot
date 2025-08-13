package com.parking.parkspot.controllers;

import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.entity.Persona;
import com.parking.parkspot.payload.request.UpdatePerfilRequest;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.PerfilCompletoResponse;
import com.parking.parkspot.repository.UserRepository;
import com.parking.parkspot.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cliente")
public class ClientePerfilController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PersonaRepository personaRepository;

    // Obtener perfil propio
    @GetMapping("/perfil/{username}")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> getPerfilUsuario(@PathVariable String username, Authentication authentication) {
        String currentUsername = authentication.getName();
        if (!currentUsername.equals(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes ver tu propio perfil"));
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        if (user.getPersona() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
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

    // Actualizar perfil propio
    @PutMapping("/perfil/{username}")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<?> updatePerfilUsuario(@PathVariable String username, @Valid @RequestBody UpdatePerfilRequest updateRequest, Authentication authentication) {
        String currentUsername = authentication.getName();
        if (!currentUsername.equals(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes editar tu propio perfil"));
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        Persona persona = user.getPersona();
        if (persona == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        persona.setNombreCompleto(updateRequest.getNombreCompleto());
        persona.setApellidos(updateRequest.getApellidos());
        persona.setDireccion(updateRequest.getDireccion());
        persona.setTelefono(updateRequest.getTelefono());
        personaRepository.save(persona);
        return ResponseEntity.ok(new MessageResponse("Perfil actualizado exitosamente"));
    }
}
