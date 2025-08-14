package com.parking.parkspot.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.parkspot.service.exception.ResourceNotFoundException;
import com.parking.parkspot.service.exception.DatosInconsistentesException;
import com.parking.parkspot.model.entity.Persona;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.payload.request.UpdatePerfilRequest;
import com.parking.parkspot.payload.response.PerfilCompletoResponse;
import com.parking.parkspot.repository.PersonaRepository;
import com.parking.parkspot.repository.UserRepository;

@Service
@Transactional
public class PersonaService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private VehiculoService vehiculoService;

    @Transactional(readOnly = true)
    public PerfilCompletoResponse obtenerPerfilCompleto(String username) {
        User user = buscarUsuarioPorUsername(username);
        
        if (user.getPersona() == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        // Obtener vehículos solo si es cliente
        boolean esCliente = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));

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
            esCliente ? vehiculoService.obtenerVehiculosBasicosDeCliente(user) : null
        );
    }

    public String actualizarMiPerfil(String username, UpdatePerfilRequest request) {
        User user = buscarUsuarioPorUsername(username);
        Persona persona = user.getPersona();

        if (persona == null) {
            throw new DatosInconsistentesException("Datos de persona no encontrados");
        }

        // Actualizar solo los campos permitidos en autogestión
        persona.setNombreCompleto(request.getNombreCompleto());
        persona.setApellidos(request.getApellidos());
        persona.setDireccion(request.getDireccion());
        persona.setTelefono(request.getTelefono());

        personaRepository.save(persona);
        
        return "Perfil actualizado exitosamente";
    }

    public PerfilCompletoResponse obtenerPerfilPorUsername(String username) {
        return obtenerPerfilCompleto(username);
    }

    public void actualizarPerfilPropio(String username, UpdatePerfilRequest request) {
        actualizarMiPerfil(username, request);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private User buscarUsuarioPorUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return userOpt.get();
    }
}
