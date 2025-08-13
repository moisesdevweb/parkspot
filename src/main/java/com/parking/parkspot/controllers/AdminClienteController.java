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
@RequestMapping("/api/admin-cliente")
public class AdminClienteController {
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

    // --- Endpoints de gestión de clientes por admin/vigilante ---

    // Buscar clientes por nombre
    @GetMapping("/buscar-clientes")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> buscarClientes(@RequestParam String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El parámetro 'nombre' es requerido"));
        }
        List<User> usuarios = userRepository.findAll();
        List<PerfilCompletoResponse> clientes = usuarios.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE")))
                .filter(user -> user.getPersona() != null &&
                        (user.getPersona().getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()) ||
                         user.getPersona().getApellidos().toLowerCase().contains(nombre.toLowerCase())))
                .distinct()
                .map(user -> {
                    List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList());
                    List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(user);
                    List<VehiculoBasicoResponse> vehiculosBasicos = vehiculos.stream()
                            .map(vehiculo -> new VehiculoBasicoResponse(
                                vehiculo.getId(),
                                vehiculo.getPlaca(),
                                vehiculo.getMarca(),
                                vehiculo.getModelo(),
                                vehiculo.getTipo(),
                                vehiculo.getAño(),
                                vehiculo.getColor()
                            ))
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
                        vehiculosBasicos
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // Editar perfil de cliente por admin/vigilante
    @PutMapping("/cliente/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updatePerfilCliente(@PathVariable Long id, @Valid @RequestBody AdminUpdatePerfilRequest updateRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        boolean esCliente = user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes editar perfiles de clientes"));
        }
        Persona persona = user.getPersona();
        if (persona == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        Optional<User> emailExistente = userRepository.findByEmail(updateRequest.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El email ya está en uso"));
        }
        Optional<Persona> dniExistente = personaRepository.findByDni(updateRequest.getDni());
        if (dniExistente.isPresent() && !dniExistente.get().getId().equals(persona.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El DNI ya está en uso"));
        }
        user.setEmail(updateRequest.getEmail());
        persona.setNombreCompleto(updateRequest.getNombreCompleto());
        persona.setApellidos(updateRequest.getApellidos());
        persona.setDni(updateRequest.getDni());
        persona.setDireccion(updateRequest.getDireccion());
        persona.setTelefono(updateRequest.getTelefono());
        if (updateRequest.getEstado() != null) {
            persona.setEstado(updateRequest.getEstado());
        }
        userRepository.save(user);
        personaRepository.save(persona);
        return ResponseEntity.ok(new MessageResponse("Perfil del cliente actualizado exitosamente"));
    }

    // Cambiar estado de cliente
    @PutMapping("/cliente/estado/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cambiarEstadoCliente(@PathVariable Long id, @RequestBody EstadoRequest estadoRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        boolean esCliente = user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes cambiar el estado de clientes"));
        }
        if (user.getPersona() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        user.getPersona().setEstado(estadoRequest.getEstado());
        personaRepository.save(user.getPersona());
        String mensaje = estadoRequest.getEstado() == 1 ? "Cliente activado" : "Cliente desactivado";
        return ResponseEntity.ok(new MessageResponse(mensaje + " exitosamente"));
    }

    // Listar clientes
    @GetMapping("/listar-clientes")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> listarClientes() {
        List<User> usuarios = userRepository.findAll();
        List<PerfilCompletoResponse> clientes = usuarios.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE")))
                .distinct()
                .map(user -> {
                    List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList());
                    List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(user);
                    List<VehiculoBasicoResponse> vehiculosBasicos = vehiculos.stream()
                            .map(vehiculo -> new VehiculoBasicoResponse(
                                vehiculo.getId(),
                                vehiculo.getPlaca(),
                                vehiculo.getMarca(),
                                vehiculo.getModelo(),
                                vehiculo.getTipo(),
                                vehiculo.getAño(),
                                vehiculo.getColor()
                            ))
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
                        vehiculosBasicos
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    // Ver perfil específico de cliente con vehículos
    @GetMapping("/cliente/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getPerfilCliente(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        boolean esCliente = user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }
        if (user.getPersona() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(user);
        List<VehiculoBasicoResponse> vehiculosBasicos = vehiculos.stream()
                .map(vehiculo -> new VehiculoBasicoResponse(
                    vehiculo.getId(),
                    vehiculo.getPlaca(),
                    vehiculo.getMarca(),
                    vehiculo.getModelo(),
                    vehiculo.getTipo(),
                    vehiculo.getAño(),
                    vehiculo.getColor()
                ))
                .collect(Collectors.toList());
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
            null,
            vehiculosBasicos
        );
        return ResponseEntity.ok(perfil);
    }

    // Editar cliente completo (incluyendo vehículos)
    @PutMapping("/cliente-completo/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateClienteCompleto(@PathVariable Long id, @Valid @RequestBody AdminUpdateClienteCompletoRequest updateRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usuario no encontrado"));
        }
        User user = userOpt.get();
        boolean esCliente = user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Solo puedes editar perfiles de clientes"));
        }
        Persona persona = user.getPersona();
        if (persona == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Datos de persona no encontrados"));
        }
        Optional<User> emailExistente = userRepository.findByEmail(updateRequest.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El email ya está en uso"));
        }
        Optional<Persona> dniExistente = personaRepository.findByDni(updateRequest.getDni());
        if (dniExistente.isPresent() && !dniExistente.get().getId().equals(persona.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El DNI ya está en uso"));
        }
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
        if (updateRequest.getVehiculos() != null && !updateRequest.getVehiculos().isEmpty()) {
            List<Vehiculo> vehiculosActuales = vehiculoRepository.findByCliente(user);
            for (VehiculoUpdateRequest vehiculoReq : updateRequest.getVehiculos()) {
                if (vehiculoReq.getId() != null) {
                    Optional<Vehiculo> vehiculoExistente = vehiculosActuales.stream()
                            .filter(v -> v.getId().equals(vehiculoReq.getId()))
                            .findFirst();
                    if (vehiculoExistente.isPresent()) {
                        Vehiculo vehiculo = vehiculoExistente.get();
                        if (!vehiculo.getPlaca().equals(vehiculoReq.getPlaca()) && vehiculoRepository.existsByPlaca(vehiculoReq.getPlaca())) {
                            return ResponseEntity.badRequest().body(new MessageResponse("Error: La placa " + vehiculoReq.getPlaca() + " ya está registrada"));
                        }
                        vehiculo.setPlaca(vehiculoReq.getPlaca());
                        vehiculo.setMarca(vehiculoReq.getMarca());
                        vehiculo.setModelo(vehiculoReq.getModelo());
                        vehiculo.setColor(vehiculoReq.getColor());
                        vehiculo.setAño(vehiculoReq.getAño());
                        vehiculo.setTipo(vehiculoReq.getTipo());
                        vehiculoRepository.save(vehiculo);
                    }
                } else {
                    if (vehiculoRepository.existsByPlaca(vehiculoReq.getPlaca())) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Error: La placa " + vehiculoReq.getPlaca() + " ya está registrada"));
                    }
                    Vehiculo nuevoVehiculo = new Vehiculo();
                    nuevoVehiculo.setPlaca(vehiculoReq.getPlaca());
                    nuevoVehiculo.setMarca(vehiculoReq.getMarca());
                    nuevoVehiculo.setModelo(vehiculoReq.getModelo());
                    nuevoVehiculo.setColor(vehiculoReq.getColor());
                    nuevoVehiculo.setAño(vehiculoReq.getAño());
                    nuevoVehiculo.setTipo(vehiculoReq.getTipo());
                    nuevoVehiculo.setEstado(1);
                    nuevoVehiculo.setCliente(user);
                    vehiculoRepository.save(nuevoVehiculo);
                }
            }
        }
        return ResponseEntity.ok(new MessageResponse("Perfil completo del cliente actualizado exitosamente"));
    }

    // Obtener vehículos de un cliente
    @GetMapping("/cliente/{id}/vehiculos")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getVehiculosCliente(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cliente no encontrado"));
        }
        User cliente = userOpt.get();
        boolean esCliente = cliente.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }
        List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(cliente);
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

    // Agregar vehículo a un cliente
    @PostMapping("/cliente/{id}/vehiculo")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> agregarVehiculoCliente(@PathVariable Long id, @Valid @RequestBody VehiculoRequest vehiculoRequest) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cliente no encontrado"));
        }
        User cliente = userOpt.get();
        boolean esCliente = cliente.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }
        if (vehiculoRepository.existsByPlaca(vehiculoRequest.getPlaca())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: La placa ya está registrada"));
        }
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

    // Editar vehículo de un cliente
    @PutMapping("/cliente/{clienteId}/vehiculo/{vehiculoId}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> editarVehiculoCliente(@PathVariable Long clienteId, @PathVariable Long vehiculoId, @Valid @RequestBody VehiculoRequest vehiculoRequest) {
        Optional<User> userOpt = userRepository.findById(clienteId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cliente no encontrado"));
        }
        User cliente = userOpt.get();
        boolean esCliente = cliente.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(vehiculoId);
        if (!vehiculoOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Vehículo no encontrado"));
        }
        Vehiculo vehiculo = vehiculoOpt.get();
        if (!vehiculo.getCliente().getId().equals(clienteId)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El vehículo no pertenece a este cliente"));
        }
        if (!vehiculo.getPlaca().equals(vehiculoRequest.getPlaca()) && vehiculoRepository.existsByPlaca(vehiculoRequest.getPlaca())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: La placa ya está registrada"));
        }
        vehiculo.setPlaca(vehiculoRequest.getPlaca());
        vehiculo.setMarca(vehiculoRequest.getMarca());
        vehiculo.setModelo(vehiculoRequest.getModelo());
        vehiculo.setColor(vehiculoRequest.getColor());
        vehiculo.setAño(vehiculoRequest.getAño());
        vehiculo.setTipo(vehiculoRequest.getTipo());
        vehiculoRepository.save(vehiculo);
        return ResponseEntity.ok(new MessageResponse("Vehículo actualizado exitosamente"));
    }

    // Eliminar vehículo de un cliente
    @DeleteMapping("/cliente/{clienteId}/vehiculo/{vehiculoId}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> eliminarVehiculoCliente(@PathVariable Long clienteId, @PathVariable Long vehiculoId) {
        Optional<User> userOpt = userRepository.findById(clienteId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cliente no encontrado"));
        }
        User cliente = userOpt.get();
        boolean esCliente = cliente.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(vehiculoId);
        if (!vehiculoOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Vehículo no encontrado"));
        }
        Vehiculo vehiculo = vehiculoOpt.get();
        if (!vehiculo.getCliente().getId().equals(clienteId)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El vehículo no pertenece a este cliente"));
        }
        vehiculoRepository.delete(vehiculo);
        return ResponseEntity.ok(new MessageResponse("Vehículo eliminado exitosamente"));
    }

    // Registrar cliente completo (datos + vehículo)
    @PostMapping("/registrar-cliente-completo")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> registrarClienteCompleto(@Valid @RequestBody SignupCompletoRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El nombre de usuario ya está tomado"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El correo electrónico ya está en uso"));
        }
        if (personaRepository.existsByDni(signUpRequest.getDni())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El DNI ya está registrado"));
        }
        if (vehiculoRepository.existsByPlaca(signUpRequest.getPlaca())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: La placa ya está registrada"));
        }
        Persona persona = new Persona();
        persona.setNombreCompleto(signUpRequest.getNombreCompleto());
        persona.setApellidos(signUpRequest.getApellidos());
        persona.setDni(signUpRequest.getDni());
        persona.setDireccion(signUpRequest.getDireccion());
        persona.setTelefono(signUpRequest.getTelefono());
        persona.setEstado(1);
        personaRepository.save(persona);
        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));
        roles.add(clienteRole);
        user.setRoles(roles);
        user.setPersona(persona);
        userRepository.save(user);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(signUpRequest.getPlaca());
        vehiculo.setMarca(signUpRequest.getMarca());
        vehiculo.setModelo(signUpRequest.getModelo());
        vehiculo.setColor(signUpRequest.getColor());
        vehiculo.setAño(signUpRequest.getAño());
        vehiculo.setTipo(signUpRequest.getTipoVehiculo());
        vehiculo.setCliente(user);
        vehiculo.setEstado(1);
        vehiculoRepository.save(vehiculo);
        return ResponseEntity.ok(new MessageResponse("Cliente registrado exitosamente con sus datos y vehículo"));
    }

    // Buscar cliente para reporte (vigilante)
    @GetMapping("/cliente-para-reporte/{nombre}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> buscarClienteParaReporte(@PathVariable String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El parámetro 'nombre' es requerido"));
        }
        List<User> usuarios = userRepository.findAll();
        List<PerfilCompletoResponse> clientes = usuarios.stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE")))
                .filter(user -> user.getPersona() != null &&
                        (user.getPersona().getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()) ||
                         user.getPersona().getApellidos().toLowerCase().contains(nombre.toLowerCase()) ||
                         (user.getPersona().getNombreCompleto() + " " + user.getPersona().getApellidos())
                                .toLowerCase().contains(nombre.toLowerCase())))
                .distinct()
                .map(user -> {
                    List<Vehiculo> vehiculos = vehiculoRepository.findByCliente(user);
                    List<VehiculoBasicoResponse> vehiculosBasicos = vehiculos.stream()
                            .map(vehiculo -> new VehiculoBasicoResponse(
                                vehiculo.getId(),
                                vehiculo.getPlaca(),
                                vehiculo.getMarca(),
                                vehiculo.getModelo(),
                                vehiculo.getTipo(),
                                vehiculo.getAño(),
                                vehiculo.getColor()
                            ))
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
                        null,
                        vehiculosBasicos
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }
}