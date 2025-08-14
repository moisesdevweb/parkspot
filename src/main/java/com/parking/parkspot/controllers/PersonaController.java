package com.parking.parkspot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.payload.request.UpdatePerfilRequest;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.PerfilCompletoResponse;
import com.parking.parkspot.service.PersonaService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/persona")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

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

        String mensaje = personaService.actualizarMiPerfil(username, updateRequest);
        return ResponseEntity.ok(new MessageResponse(mensaje));
    }

    // Obtener perfil propio (autogestión)
    @GetMapping("/perfil/{username}")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getPerfilUsuario(@PathVariable String username, Authentication authentication) {
        String currentUsername = authentication.getName();
        if (!currentUsername.equals(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes ver tu propio perfil"));
        }

        PerfilCompletoResponse perfil = personaService.obtenerPerfilCompleto(username);
        return ResponseEntity.ok(perfil);
    }
}
