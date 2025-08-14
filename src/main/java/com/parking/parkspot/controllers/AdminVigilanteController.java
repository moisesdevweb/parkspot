package com.parking.parkspot.controllers;

import com.parking.parkspot.payload.request.*;
import com.parking.parkspot.payload.response.*;
import com.parking.parkspot.service.VigilanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin-vigilante")
public class AdminVigilanteController {
    
    @Autowired
    private VigilanteService vigilanteService;

    // --- Endpoints de gestión de vigilantes por admin ---

    // Listar vigilantes
    @GetMapping("/listar-vigilantes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> listarVigilantes() {
        List<PerfilCompletoResponse> vigilantes = vigilanteService.listarTodosLosVigilantes();
        return ResponseEntity.ok(vigilantes);
    }

    // Buscar vigilantes por nombre
    @GetMapping("/buscar-vigilantes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> buscarVigilantes(@RequestParam String nombre) {
        List<PerfilCompletoResponse> vigilantes = vigilanteService.buscarVigilantesPorNombre(nombre);
        return ResponseEntity.ok(vigilantes);
    }

    // Ver perfil específico de vigilante
    @GetMapping("/vigilante/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getPerfilVigilante(@PathVariable Long id) {
        PerfilCompletoResponse perfil = vigilanteService.obtenerPerfilVigilante(id);
        return ResponseEntity.ok(perfil);
    }

    // Editar perfil de vigilante
    @PutMapping("/vigilante/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updatePerfilVigilante(@PathVariable Long id, @Valid @RequestBody AdminUpdatePerfilRequest updateRequest) {
        vigilanteService.actualizarPerfilVigilante(id, updateRequest);
        return ResponseEntity.ok(new MessageResponse("Perfil del vigilante actualizado exitosamente"));
    }

    // Cambiar estado de vigilante
    @PutMapping("/vigilante/estado/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cambiarEstadoVigilante(@PathVariable Long id, @RequestBody EstadoRequest estadoRequest) {
        vigilanteService.cambiarEstadoVigilante(id, estadoRequest.getEstado());
        String mensaje = estadoRequest.getEstado() == 1 ? "Vigilante activado" : "Vigilante desactivado";
        return ResponseEntity.ok(new MessageResponse(mensaje + " exitosamente"));
    }

    // Registrar nuevo vigilante
    @PostMapping("/registrar-vigilante")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> registrarVigilante(@Valid @RequestBody SignupRequest signUpRequest) {
        vigilanteService.registrarVigilante(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("Vigilante registrado exitosamente"));
    }

    // Eliminar vigilante (cambiar estado a inactivo)
    @DeleteMapping("/vigilante/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> eliminarVigilante(@PathVariable Long id) {
        vigilanteService.desactivarVigilante(id);
        return ResponseEntity.ok(new MessageResponse("Vigilante desactivado exitosamente"));
    }
}
