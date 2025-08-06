package com.parking.parkspot.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.model.entity.Persona;
import com.parking.parkspot.model.entity.Role;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.entity.Vehiculo;
import com.parking.parkspot.model.enums.ERole;
import com.parking.parkspot.payload.request.AdminUpdatePerfilRequest;
import com.parking.parkspot.payload.request.AdminUpdateClienteCompletoRequest;
import com.parking.parkspot.payload.request.VehiculoUpdateRequest;
import com.parking.parkspot.payload.request.EstadoRequest;
import com.parking.parkspot.payload.request.SignupCompletoRequest;
import com.parking.parkspot.payload.request.UpdatePerfilRequest;
import com.parking.parkspot.payload.request.VehiculoRequest;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.PerfilCompletoResponse;
import com.parking.parkspot.payload.response.VehiculoBasicoResponse;
import com.parking.parkspot.repository.PersonaRepository;
import com.parking.parkspot.repository.RoleRepository;
import com.parking.parkspot.repository.UserRepository;
import com.parking.parkspot.repository.VehiculoRepository;
import com.parking.parkspot.payload.response.VehiculoResponse;

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

    @Autowired
    VehiculoRepository vehiculoRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

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


        // Constructor SIN roles para perfil propio
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
            // SIN roles
        );

        return ResponseEntity.ok(perfil);
    }

// ADMIN puede buscar VIGILANTES por nombre
    @GetMapping("/buscar-vigilantes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> buscarVigilantes(@RequestParam String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El parámetro 'nombre' es requerido"));
        }

        List<User> usuarios = userRepository.findAll();
        
        List<PerfilCompletoResponse> vigilantes = usuarios.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE")))
                .filter(user -> user.getPersona() != null && 
                        (user.getPersona().getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()) ||
                         user.getPersona().getApellidos().toLowerCase().contains(nombre.toLowerCase()) ||
                         (user.getPersona().getNombreCompleto() + " " + user.getPersona().getApellidos())
                                .toLowerCase().contains(nombre.toLowerCase())))
                .distinct()
                .map(user -> {
                    // Obtener roles
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());
                    
                    // Constructor CON roles para búsqueda
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
                        roles // CON roles
                    );
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(vigilantes);
    }

    // VIGILANTE y ADMIN pueden buscar CLIENTES por nombre
    @GetMapping("/buscar-clientes")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> buscarClientes(@RequestParam String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El parámetro 'nombre' es requerido"));
        }

        List<User> usuarios = userRepository.findAll();
        
        List<PerfilCompletoResponse> clientes = usuarios.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE")))
                .filter(user -> user.getPersona() != null && 
                        (user.getPersona().getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()) ||
                         user.getPersona().getApellidos().toLowerCase().contains(nombre.toLowerCase())))
                .distinct()
                .map(user -> {
                    // Obtener roles
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());
                    
                    // Obtener vehículos básicos
                    List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(user);
                    List<VehiculoBasicoResponse> vehiculosBasicos = vehiculos.stream()
                            .map(vehiculo -> new VehiculoBasicoResponse(
                                vehiculo.getId(),
                                vehiculo.getPlaca(),
                                vehiculo.getMarca(),
                                vehiculo.getModelo(),
                                vehiculo.getTipo()
                            ))
                            .collect(Collectors.toList());
                    
                    // Constructor CON vehículos
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
                        vehiculosBasicos // ¡USA VehiculoBasicoResponse!
                    );
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(clientes);
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

    // ROLE_ADMIN puede cambiar estado de VIGILANTES solamente
    @PutMapping("/vigilante/estado/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cambiarEstadoVigilante(@PathVariable Long id, 
                                                   @RequestBody EstadoRequest estadoRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        
        // Verificar que sea un ROLE_VIGILANTE
        boolean esVigilante = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE"));
        
        if (!esVigilante) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo puedes cambiar el estado de vigilantes"));
        }

        if (user.getPersona() == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        user.getPersona().setEstado(estadoRequest.getEstado());
        personaRepository.save(user.getPersona());

        String mensaje = estadoRequest.getEstado() == 1 ? "Vigilante activado" : "Vigilante desactivado";
        return ResponseEntity.ok(new MessageResponse(mensaje + " exitosamente"));
    }

    // ROLE_VIGILANTE y ROLE_ADMIN pueden cambiar estado de CLIENTES
    @PutMapping("/cliente/estado/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
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

    // ADMIN puede listar solo VIGILANTES
    @GetMapping("/listar-vigilantes")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> listarVigilantes() {
        List<User> usuarios = userRepository.findAll();
        
        List<PerfilCompletoResponse> vigilantes = usuarios.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE")))
                .distinct()
                .map(user -> {
                    // Obtener roles
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());
                    
                    // Constructor CON roles para lista
                    return new PerfilCompletoResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPersona() != null ? user.getPersona().getNombreCompleto() : null,
                        user.getPersona() != null ? user.getPersona().getApellidos() : null,
                        user.getPersona() != null ? user.getPersona().getDni() : null,
                        user.getPersona() != null ? user.getPersona().getDireccion() : null,
                        user.getPersona() != null ? user.getPersona().getTelefono() : null,
                        user.getPersona() != null ? user.getPersona().getEstado() : null,
                        roles // CON roles
                    );
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(vigilantes);
    }

    // VIGILANTE y ADMIN pueden listar solo CLIENTES
    @GetMapping("/listar-clientes")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> listarClientes() {
        List<User> usuarios = userRepository.findAll();
        
        List<PerfilCompletoResponse> clientes = usuarios.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE")))
                .distinct()
                .map(user -> {
                    // Obtener roles
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());
                    
                    // Obtener vehículos básicos
                    List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(user);
                    List<VehiculoBasicoResponse> vehiculosBasicos = vehiculos.stream()
                            .map(vehiculo -> new VehiculoBasicoResponse(
                                vehiculo.getId(),
                                vehiculo.getPlaca(),
                                vehiculo.getMarca(),
                                vehiculo.getModelo(),
                                vehiculo.getTipo()
                            ))
                            .collect(Collectors.toList());
                    
                    // Constructor CON roles Y vehículos
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
                        vehiculosBasicos // ¡USA VehiculoBasicoResponse!
                    );
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(clientes);
    }


    // VIGILANTE puede ver perfil de CLIENTE por ID CON VEHÍCULOS
    @GetMapping("/cliente/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getPerfilCliente(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        
        // Verificar que sea CLIENTE
        boolean esCliente = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        if (user.getPersona() == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        // Obtener vehículos del cliente
        List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(user);
        List<VehiculoBasicoResponse> vehiculosBasicos = vehiculos.stream()
                .map(vehiculo -> new VehiculoBasicoResponse(
                    vehiculo.getId(),
                    vehiculo.getPlaca(),
                    vehiculo.getMarca(),
                    vehiculo.getModelo(),
                    vehiculo.getTipo()
                ))
                .collect(Collectors.toList());

        // Constructor CON vehículos (sin roles para perfil específico)
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
            null, // SIN roles
            vehiculosBasicos // CON vehículos
        );

        return ResponseEntity.ok(perfil);
    }

    // VIGILANTE puede editar perfil de CLIENTE por ID
    @PutMapping("/cliente/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updatePerfilCliente(@PathVariable Long id, 
                                                @Valid @RequestBody AdminUpdatePerfilRequest updateRequest) { // ¡CAMBIAR A AdminUpdatePerfilRequest!
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        
        // Verificar que sea CLIENTE
        boolean esCliente = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo puedes editar perfiles de clientes"));
        }

        Persona persona = user.getPersona();
        
        if (persona == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        // Verificar que el email no exista en otro usuario
        Optional<User> emailExistente = userRepository.findByEmail(updateRequest.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(user.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El email ya está en uso"));
        }

        // Verificar que el DNI no exista en otra persona
        Optional<Persona> dniExistente = personaRepository.findByDni(updateRequest.getDni());
        if (dniExistente.isPresent() && !dniExistente.get().getId().equals(persona.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El DNI ya está en uso"));
        }

        // Actualizar TODOS los campos editables (incluyendo email y DNI)
        user.setEmail(updateRequest.getEmail()); // ¡AGREGAR EMAIL!
        persona.setNombreCompleto(updateRequest.getNombreCompleto());
        persona.setApellidos(updateRequest.getApellidos());
        persona.setDni(updateRequest.getDni()); // ¡AGREGAR DNI!
        persona.setDireccion(updateRequest.getDireccion());
        persona.setTelefono(updateRequest.getTelefono());

        // Guardar ambas entidades
        userRepository.save(user); // ¡Para el email!
        personaRepository.save(persona);

        return ResponseEntity.ok(new MessageResponse("Perfil del cliente actualizado exitosamente"));
    }

    // ADMIN/VIGILANTE pueden editar perfil COMPLETO de CLIENTE por ID (incluyendo vehículos)
    @PutMapping("/cliente-completo/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateClienteCompleto(@PathVariable Long id, 
                                                  @Valid @RequestBody AdminUpdateClienteCompletoRequest updateRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        
        // Verificar que sea CLIENTE
        boolean esCliente = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo puedes editar perfiles de clientes"));
        }

        Persona persona = user.getPersona();
        
        if (persona == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        // Verificar que el email no exista en otro usuario
        Optional<User> emailExistente = userRepository.findByEmail(updateRequest.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(user.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El email ya está en uso"));
        }

        // Verificar que el DNI no exista en otra persona
        Optional<Persona> dniExistente = personaRepository.findByDni(updateRequest.getDni());
        if (dniExistente.isPresent() && !dniExistente.get().getId().equals(persona.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El DNI ya está en uso"));
        }

        // Actualizar datos personales
        user.setEmail(updateRequest.getEmail());
        persona.setNombreCompleto(updateRequest.getNombreCompleto());
        persona.setApellidos(updateRequest.getApellidos());
        persona.setDni(updateRequest.getDni());
        persona.setDireccion(updateRequest.getDireccion());
        persona.setTelefono(updateRequest.getTelefono());

        // Guardar cambios de usuario y persona
        userRepository.save(user);
        personaRepository.save(persona);

        // Actualizar vehículos si se proporcionaron
        if (updateRequest.getVehiculos() != null && !updateRequest.getVehiculos().isEmpty()) {
            // Obtener vehículos actuales del cliente
            List<Vehiculo> vehiculosActuales = vehiculoRepository.findByCliente(user);
            
            // Procesar cada vehículo del request
            for (VehiculoUpdateRequest vehiculoReq : updateRequest.getVehiculos()) {
                if (vehiculoReq.getId() != null) {
                    // Actualizar vehículo existente
                    Optional<Vehiculo> vehiculoExistente = vehiculosActuales.stream()
                            .filter(v -> v.getId().equals(vehiculoReq.getId()))
                            .findFirst();
                    
                    if (vehiculoExistente.isPresent()) {
                        Vehiculo vehiculo = vehiculoExistente.get();
                        
                        // Verificar que la placa no exista en otro vehículo (si cambió)
                        if (!vehiculo.getPlaca().equals(vehiculoReq.getPlaca()) && 
                            vehiculoRepository.existsByPlaca(vehiculoReq.getPlaca())) {
                            return ResponseEntity.badRequest()
                                    .body(new MessageResponse("Error: La placa " + vehiculoReq.getPlaca() + " ya está registrada"));
                        }
                        
                        // Actualizar datos del vehículo
                        vehiculo.setPlaca(vehiculoReq.getPlaca());
                        vehiculo.setMarca(vehiculoReq.getMarca());
                        vehiculo.setModelo(vehiculoReq.getModelo());
                        vehiculo.setColor(vehiculoReq.getColor());
                        vehiculo.setAño(vehiculoReq.getAño());
                        vehiculo.setTipo(vehiculoReq.getTipo());
                        
                        vehiculoRepository.save(vehiculo);
                    }
                } else {
                    // Crear nuevo vehículo
                    if (vehiculoRepository.existsByPlaca(vehiculoReq.getPlaca())) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Error: La placa " + vehiculoReq.getPlaca() + " ya está registrada"));
                    }

                    Vehiculo nuevoVehiculo = new Vehiculo();
                    nuevoVehiculo.setPlaca(vehiculoReq.getPlaca());
                    nuevoVehiculo.setMarca(vehiculoReq.getMarca());
                    nuevoVehiculo.setModelo(vehiculoReq.getModelo());
                    nuevoVehiculo.setColor(vehiculoReq.getColor());
                    nuevoVehiculo.setAño(vehiculoReq.getAño());
                    nuevoVehiculo.setTipo(vehiculoReq.getTipo());
                    nuevoVehiculo.setEstado(1); // Activo
                    nuevoVehiculo.setCliente(user);

                    vehiculoRepository.save(nuevoVehiculo);
                }
            }
        }

        return ResponseEntity.ok(new MessageResponse("Perfil completo del cliente actualizado exitosamente"));
    }

    // ADMIN puede ver perfil de VIGILANTE por ID
    @GetMapping("/vigilante/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getPerfilVigilante(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        
        // Verificar que sea VIGILANTE
        boolean esVigilante = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE"));
        
        if (!esVigilante) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El usuario no es un vigilante"));
        }

        if (user.getPersona() == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        // Constructor SIN roles para ver perfil específico
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
            // SIN roles
        );

        return ResponseEntity.ok(perfil);
    }

    // ADMIN puede editar perfil COMPLETO de VIGILANTE por ID
    @PutMapping("/vigilante/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updatePerfilVigilante(@PathVariable Long id, 
                                                  @Valid @RequestBody AdminUpdatePerfilRequest updateRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();
        
        // Verificar que sea VIGILANTE
        boolean esVigilante = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_VIGILANTE"));
        
        if (!esVigilante) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo puedes editar perfiles de vigilantes"));
        }

        Persona persona = user.getPersona();
        
        if (persona == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Datos de persona no encontrados"));
        }

        // Verificar que el email no exista en otro usuario
        Optional<User> emailExistente = userRepository.findByEmail(updateRequest.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(user.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El email ya está en uso"));
        }

        // Verificar que el DNI no exista en otra persona
        Optional<Persona> dniExistente = personaRepository.findByDni(updateRequest.getDni());
        if (dniExistente.isPresent() && !dniExistente.get().getId().equals(persona.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El DNI ya está en uso"));
        }

        // Actualizar TODOS los campos editables (excepto username y password)
        user.setEmail(updateRequest.getEmail()); // ¡NUEVO!
        persona.setNombreCompleto(updateRequest.getNombreCompleto());
        persona.setApellidos(updateRequest.getApellidos());
        persona.setDni(updateRequest.getDni()); // ¡NUEVO!
        persona.setDireccion(updateRequest.getDireccion());
        persona.setTelefono(updateRequest.getTelefono());

        // Guardar ambas entidades
        userRepository.save(user); // ¡Para el email!
        personaRepository.save(persona);

        return ResponseEntity.ok(new MessageResponse("Perfil del vigilante actualizado exitosamente"));
    }

    // ADMIN/VIGILANTE pueden ver vehículos de un cliente
    @GetMapping("/cliente/{id}/vehiculos") 
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getVehiculosCliente(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = userOpt.get();
        
        // Verificar que sea CLIENTE
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(cliente);
        
        // Convertir a VehiculoResponse para evitar referencia circular
        List<VehiculoResponse> vehiculosResponse = vehiculos.stream()
                .map(vehiculo -> new VehiculoResponse(
                    vehiculo.getId(),
                    vehiculo.getPlaca(),
                    vehiculo.getMarca(),
                    vehiculo.getModelo(),
                    vehiculo.getColor(),
                    vehiculo.getAño(),
                    vehiculo.getTipo(),
                    vehiculo.getEstado(),
                    cliente.getId(),
                    cliente.getPersona().getNombreCompleto()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(vehiculosResponse);
    }

    // ADMIN/VIGILANTE pueden agregar vehículo a un cliente
    @PostMapping("/cliente/{id}/vehiculo")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> agregarVehiculoCliente(@PathVariable Long id, 
                                                   @Valid @RequestBody VehiculoRequest vehiculoRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = userOpt.get();
        
        // Verificar que sea CLIENTE
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        // Verificar que la placa no exista
        if (vehiculoRepository.existsByPlaca(vehiculoRequest.getPlaca())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: La placa ya está registrada"));
        }

        // Crear vehículo
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(vehiculoRequest.getPlaca());
        vehiculo.setMarca(vehiculoRequest.getMarca());
        vehiculo.setModelo(vehiculoRequest.getModelo());
        vehiculo.setColor(vehiculoRequest.getColor());
        vehiculo.setAño(vehiculoRequest.getAño());
        vehiculo.setTipo(vehiculoRequest.getTipo());
        vehiculo.setCliente(cliente);

        vehiculoRepository.save(vehiculo);

        return ResponseEntity.ok(new MessageResponse("Vehículo agregado exitosamente"));
    }

    // ADMIN/VIGILANTE pueden editar vehículo de un cliente
    @PutMapping("/cliente/{clienteId}/vehiculo/{vehiculoId}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> editarVehiculoCliente(@PathVariable Long clienteId,
                                                  @PathVariable Long vehiculoId,
                                                  @Valid @RequestBody VehiculoRequest vehiculoRequest) {
        // Verificar que el cliente existe
        Optional<User> userOpt = userRepository.findById(clienteId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = userOpt.get();
        
        // Verificar que sea CLIENTE
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        // Verificar que el vehículo existe y pertenece al cliente
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(vehiculoId);
        if (!vehiculoOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vehículo no encontrado"));
        }

        Vehiculo vehiculo = vehiculoOpt.get();
        
        // Verificar que el vehículo pertenece al cliente
        if (!vehiculo.getCliente().getId().equals(clienteId)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El vehículo no pertenece a este cliente"));
        }

        // Verificar que la nueva placa no exista (excepto si es la misma del vehículo actual)
        if (!vehiculo.getPlaca().equals(vehiculoRequest.getPlaca()) && 
            vehiculoRepository.existsByPlaca(vehiculoRequest.getPlaca())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: La placa ya está registrada"));
        }

        // Actualizar datos del vehículo
        vehiculo.setPlaca(vehiculoRequest.getPlaca());
        vehiculo.setMarca(vehiculoRequest.getMarca());
        vehiculo.setModelo(vehiculoRequest.getModelo());
        vehiculo.setColor(vehiculoRequest.getColor());
        vehiculo.setAño(vehiculoRequest.getAño());
        vehiculo.setTipo(vehiculoRequest.getTipo());

        vehiculoRepository.save(vehiculo);

        return ResponseEntity.ok(new MessageResponse("Vehículo actualizado exitosamente"));
    }

    // ADMIN/VIGILANTE pueden eliminar vehículo de un cliente
    @DeleteMapping("/cliente/{clienteId}/vehiculo/{vehiculoId}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> eliminarVehiculoCliente(@PathVariable Long clienteId,
                                                    @PathVariable Long vehiculoId) {
        // Verificar que el cliente existe
        Optional<User> userOpt = userRepository.findById(clienteId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = userOpt.get();
        
        // Verificar que sea CLIENTE
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        // Verificar que el vehículo existe y pertenece al cliente
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(vehiculoId);
        if (!vehiculoOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vehículo no encontrado"));
        }

        Vehiculo vehiculo = vehiculoOpt.get();
        
        // Verificar que el vehículo pertenece al cliente
        if (!vehiculo.getCliente().getId().equals(clienteId)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El vehículo no pertenece a este cliente"));
        }

        vehiculoRepository.delete(vehiculo);

        return ResponseEntity.ok(new MessageResponse("Vehículo eliminado exitosamente"));
    }

    // ADMIN/VIGILANTE pueden registrar CLIENTE COMPLETO (datos + vehículo)
    @PostMapping("/registrar-cliente-completo")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> registrarClienteCompleto(@Valid @RequestBody SignupCompletoRequest signUpRequest) {
        
        // Validar username
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El nombre de usuario ya está tomado"));
        }

        // Validar email
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El correo electrónico ya está en uso"));
        }

        // Validar DNI
        if (personaRepository.existsByDni(signUpRequest.getDni())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El DNI ya está registrado"));
        }

        // Validar placa
        if (vehiculoRepository.existsByPlaca(signUpRequest.getPlaca())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: La placa ya está registrada"));
        }

        // 1. CREAR PERSONA
        Persona persona = new Persona();
        persona.setNombreCompleto(signUpRequest.getNombreCompleto());
        persona.setApellidos(signUpRequest.getApellidos());
        persona.setDni(signUpRequest.getDni());
        persona.setDireccion(signUpRequest.getDireccion());
        persona.setTelefono(signUpRequest.getTelefono());
        persona.setEstado(1); // Activo por defecto

        personaRepository.save(persona);

        // 2. CREAR USUARIO
        User user = new User(signUpRequest.getUsername(), 
                           signUpRequest.getEmail(),
                           encoder.encode(signUpRequest.getPassword()));

        // Asignar rol CLIENTE
        Set<Role> roles = new HashSet<>();
        Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));
        roles.add(clienteRole);

        user.setRoles(roles);
        user.setPersona(persona);

        userRepository.save(user);

        // 3. CREAR VEHÍCULO
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(signUpRequest.getPlaca());
        vehiculo.setMarca(signUpRequest.getMarca());
        vehiculo.setModelo(signUpRequest.getModelo());
        vehiculo.setColor(signUpRequest.getColor());
        vehiculo.setAño(signUpRequest.getAño());
        vehiculo.setTipo(signUpRequest.getTipoVehiculo());
        vehiculo.setCliente(user);
        vehiculo.setEstado(1); // Activo

        vehiculoRepository.save(vehiculo);

        return ResponseEntity.ok(new MessageResponse("Cliente registrado exitosamente con sus datos y vehículo"));
    }
}
