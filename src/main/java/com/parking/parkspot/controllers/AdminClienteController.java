package com.parking.parkspot.controllers;

import com.parking.parkspot.payload.request.*;
import com.parking.parkspot.payload.response.*;
import com.parking.parkspot.service.ClienteService;
import com.parking.parkspot.service.VehiculoService;
import com.parking.parkspot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin-cliente")
public class AdminClienteController {
    
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private VehiculoService vehiculoService;
    @Autowired
    private UserRepository userRepository;

    // --- Endpoints de gestión de clientes por admin/vigilante ---

    // Buscar clientes por nombre
    @GetMapping("/buscar-clientes")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> buscarClientes(@RequestParam String nombre) {
        List<PerfilCompletoResponse> clientes = clienteService.buscarClientesPorNombre(nombre);
        return ResponseEntity.ok(clientes);
    }

    // Editar perfil de cliente por admin/vigilante
    @PutMapping("/cliente/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updatePerfilCliente(@PathVariable Long id, @Valid @RequestBody AdminUpdatePerfilRequest updateRequest) {
        clienteService.actualizarPerfilCliente(id, updateRequest);
        return ResponseEntity.ok(new MessageResponse("Perfil del cliente actualizado exitosamente"));
    }

    // Cambiar estado de cliente
    @PutMapping("/cliente/estado/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cambiarEstadoCliente(@PathVariable Long id, @RequestBody EstadoRequest estadoRequest) {
        clienteService.cambiarEstadoCliente(id, estadoRequest.getEstado());
        String mensaje = estadoRequest.getEstado() == 1 ? "Cliente activado" : "Cliente desactivado";
        return ResponseEntity.ok(new MessageResponse(mensaje + " exitosamente"));
    }

    // Listar clientes
    @GetMapping("/listar-clientes")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> listarClientes() {
        List<PerfilCompletoResponse> clientes = clienteService.listarTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }

    // Ver perfil específico de cliente con vehículos
    @GetMapping("/cliente/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getPerfilCliente(@PathVariable Long id) {
        PerfilCompletoResponse perfil = clienteService.obtenerPerfilCliente(id);
        return ResponseEntity.ok(perfil);
    }

    // Editar cliente completo (incluyendo vehículos)
    @PutMapping("/cliente-completo/{id}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateClienteCompleto(@PathVariable Long id, @Valid @RequestBody AdminUpdateClienteCompletoRequest updateRequest) {
        clienteService.actualizarClienteCompleto(id, updateRequest);
        return ResponseEntity.ok(new MessageResponse("Perfil completo del cliente actualizado exitosamente"));
    }

    // Obtener vehículos de un cliente
    @GetMapping("/cliente/{id}/vehiculos")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getVehiculosCliente(@PathVariable Long id) {
        Optional<com.parking.parkspot.model.entity.User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cliente no encontrado"));
        }
        
        com.parking.parkspot.model.entity.User cliente = userOpt.get();
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        List<VehiculoResponse> vehiculos = vehiculoService.obtenerVehiculosDeCliente(cliente);
        return ResponseEntity.ok(vehiculos);
    }

    // Agregar vehículo a un cliente
    @PostMapping("/cliente/{id}/vehiculo")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> agregarVehiculoCliente(@PathVariable Long id, @Valid @RequestBody VehiculoRequest vehiculoRequest) {
        Optional<com.parking.parkspot.model.entity.User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cliente no encontrado"));
        }
        
        com.parking.parkspot.model.entity.User cliente = userOpt.get();
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        vehiculoService.agregarVehiculoACliente(cliente, vehiculoRequest);
        return ResponseEntity.ok(new MessageResponse("Vehículo agregado exitosamente"));
    }

    // Editar vehículo de un cliente
    @PutMapping("/cliente/{clienteId}/vehiculo/{vehiculoId}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> editarVehiculoCliente(@PathVariable Long clienteId, @PathVariable Long vehiculoId, @Valid @RequestBody VehiculoRequest vehiculoRequest) {
        Optional<com.parking.parkspot.model.entity.User> userOpt = userRepository.findById(clienteId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cliente no encontrado"));
        }
        
        com.parking.parkspot.model.entity.User cliente = userOpt.get();
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        vehiculoService.editarVehiculo(vehiculoId, clienteId, vehiculoRequest);
        return ResponseEntity.ok(new MessageResponse("Vehículo actualizado exitosamente"));
    }

    // Eliminar vehículo de un cliente
    @DeleteMapping("/cliente/{clienteId}/vehiculo/{vehiculoId}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> eliminarVehiculoCliente(@PathVariable Long clienteId, @PathVariable Long vehiculoId) {
        Optional<com.parking.parkspot.model.entity.User> userOpt = userRepository.findById(clienteId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cliente no encontrado"));
        }
        
        com.parking.parkspot.model.entity.User cliente = userOpt.get();
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        if (!esCliente) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: El usuario no es un cliente"));
        }

        vehiculoService.eliminarVehiculo(vehiculoId, clienteId);
        return ResponseEntity.ok(new MessageResponse("Vehículo eliminado exitosamente"));
    }

    // Registrar cliente completo (datos + vehículo)
    @PostMapping("/registrar-cliente-completo")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> registrarClienteCompleto(@Valid @RequestBody SignupCompletoRequest signUpRequest) {
        clienteService.registrarClienteCompleto(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("Cliente registrado exitosamente con sus datos y vehículo"));
    }

    // Buscar cliente para reporte (vigilante)
    @GetMapping("/cliente-para-reporte/{nombre}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> buscarClienteParaReporte(@PathVariable String nombre) {
        List<PerfilCompletoResponse> clientes = clienteService.buscarClienteParaReporte(nombre);
        return ResponseEntity.ok(clientes);
    }
}