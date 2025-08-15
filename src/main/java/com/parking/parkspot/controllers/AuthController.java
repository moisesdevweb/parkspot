package com.parking.parkspot.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.parking.parkspot.model.entity.Role;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.entity.Vehiculo;
import com.parking.parkspot.model.enums.ERole;
import com.parking.parkspot.payload.request.LoginRequest;
import com.parking.parkspot.payload.request.SignupCompletoRequest;
import com.parking.parkspot.payload.request.SignupRequest;
import com.parking.parkspot.payload.request.VehiculoRequest;
import com.parking.parkspot.payload.response.JwtResponse;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.repository.PersonaRepository;
import com.parking.parkspot.repository.RoleRepository;
import com.parking.parkspot.repository.UserRepository;
import com.parking.parkspot.repository.VehiculoRepository;
import com.parking.parkspot.security.jwt.JwtUtils;
import com.parking.parkspot.security.service.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    // LOGIN - Puede usar username O email
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Buscar usuario por username O por email
        String usernameOrEmail = loginRequest.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        
        // Si no se encuentra por username, intentar por email
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Usuario o email no encontrado"));
        }

        User user = userOpt.get();
        // Verificar estado del usuario si tiene persona asociada
        if (user.getPersona() != null && user.getPersona().getEstado() != 1) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Usuario inactivo, contacte a un administrador"));
        }

        // Para la autenticación de Spring Security, usar el username real del usuario encontrado
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Obtener datos de la persona si están disponibles
        Persona persona = user.getPersona();
        String nombreCompleto = persona != null ? persona.getNombreCompleto() : user.getUsername();
        String apellidos = persona != null ? persona.getApellidos() : "";
        String dni = persona != null ? persona.getDni() : "";
        String direccion = persona != null ? persona.getDireccion() : "";
        String telefono = persona != null ? persona.getTelefono() : "";

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

    // SIGNUP BÁSICO - Lógica de Spring Security debe estar aquí
    @PostMapping("/registar-usuario")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El nombre de usuario ya está en uso"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El email ya está en uso"));
        }

        // Crear una nueva cuenta de usuario
        User user = new User(signUpRequest.getUsername(), 
                           signUpRequest.getEmail(),
                           encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                    .orElseThrow(() -> new RuntimeException("Error: Rol CLIENTE no encontrado"));
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

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado exitosamente"));
    }

    // SIGNUP COMPLETO - Lógica de Spring Security debe estar aquí
    @PostMapping("/registar-completo")
    public ResponseEntity<?> registerUserCompleto(@Valid @RequestBody SignupCompletoRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El nombre de usuario ya está en uso"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El email ya está en uso"));
        }

        if (personaRepository.existsByDni(signUpRequest.getDni())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Ya existe una persona con ese DNI"));
        }

        // Crear la persona primero
        Persona persona = new Persona();
        persona.setNombreCompleto(signUpRequest.getNombreCompleto());
        persona.setApellidos(signUpRequest.getApellidos());
        persona.setDni(signUpRequest.getDni());
        persona.setTelefono(signUpRequest.getTelefono());
        persona.setDireccion(signUpRequest.getDireccion());
        persona.setEstado(1); // Activo por defecto

        personaRepository.save(persona);

        // Crear nueva cuenta de usuario
        User user = new User(signUpRequest.getUsername(),
                           signUpRequest.getEmail(),
                           encoder.encode(signUpRequest.getPassword()));
        user.setPersona(persona);

        // Para SignupCompletoRequest, siempre será CLIENTE (no tiene campo role)
        Role clienteRole = roleRepository.findByName(ERole.ROLE_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Error: Rol CLIENTE no encontrado"));
        Set<Role> roles = new HashSet<>();
        roles.add(clienteRole);

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado exitosamente con datos personales"));
    }

    // REGISTRO DE VEHÍCULO - Puede delegarse al service
    @PostMapping("/register-vehiculo") 
    public ResponseEntity<?> registerVehiculo(@Valid @RequestBody VehiculoRequest vehiculoRequest,
                                            Authentication authentication) {
        // Obtener el usuario actual desde el token
        String username = authentication.getName();

        // Verificar que el usuario existe
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Usuario no encontrado"));
        }

        User user = userOpt.get();

        // Verificar que el usuario tiene rol CLIENTE
        boolean esCliente = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(ERole.ROLE_CLIENTE));

        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo los clientes pueden registrar vehículos"));
        }

        // Verificar que la placa no exista ya
        if (vehiculoRepository.existsByPlaca(vehiculoRequest.getPlaca())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Ya existe un vehículo con esa placa"));
        }

        // Crear el vehículo
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(vehiculoRequest.getPlaca());
        vehiculo.setMarca(vehiculoRequest.getMarca());
        vehiculo.setModelo(vehiculoRequest.getModelo());
        vehiculo.setColor(vehiculoRequest.getColor());
        vehiculo.setTipo(vehiculoRequest.getTipo()); // setTipo() correcto
        vehiculo.setCliente(user);

        vehiculoRepository.save(vehiculo);

        return ResponseEntity.ok(new MessageResponse("Vehículo registrado exitosamente"));
    }
}
