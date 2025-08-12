package com.parking.parkspot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.model.entity.EspacioEstacionamiento;
import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.TipoEspacio;
import com.parking.parkspot.payload.response.EspacioEstacionamientoResponse;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.repository.EspacioEstacionamientoRepository;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/espacios")
public class GestionEspaciosController {

    @Autowired
    private EspacioEstacionamientoRepository espacioRepository;

    // Crear un espacio individual
    @PostMapping("/crear")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> crearEspacio(@Valid @RequestBody CrearEspacioRequest espacioRequest) {
        // Verificar que no existe un espacio con ese número
        if (espacioRepository.existsByNumero(espacioRequest.getNumero())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Ya existe un espacio con el número " + espacioRequest.getNumero()));
        }

        // Crear el espacio
        EspacioEstacionamiento espacio = new EspacioEstacionamiento(
                espacioRequest.getNumero(),
                espacioRequest.getTipo(),
                espacioRequest.getDescripcion(),
                espacioRequest.getTarifaPorHora()
        );

        espacioRepository.save(espacio);

        return ResponseEntity.ok(new MessageResponse("Espacio " + espacio.getNumero() + " creado exitosamente"));
    }

    // Crear múltiples espacios de una vez (ideal para inicializar el estacionamiento)
    @PostMapping("/crear-masivo")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> crearEspaciosMasivo(@Valid @RequestBody CrearEspaciosMasivoRequest request) {
        int espaciosCreados = 0;
        int espaciosExistentes = 0;

        for (String filas : request.getFilas()) {
            for (int i = 1; i <= request.getEspaciosPorFila(); i++) {
                String numeroEspacio = filas + i;
                
                // Solo crear si no existe
                if (!espacioRepository.existsByNumero(numeroEspacio)) {
                    EspacioEstacionamiento espacio = new EspacioEstacionamiento(
                            numeroEspacio,
                            request.getTipoDefecto(),
                            "Espacio de estacionamiento " + numeroEspacio,
                            request.getTarifaDefecto()
                    );
                    espacioRepository.save(espacio);
                    espaciosCreados++;
                } else {
                    espaciosExistentes++;
                }
            }
        }

        return ResponseEntity.ok(new MessageResponse(
                "Espacios creados: " + espaciosCreados + 
                ", Ya existían: " + espaciosExistentes));
    }

    // Actualizar un espacio
    @PutMapping("/{espacioId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> actualizarEspacio(@PathVariable Long espacioId,
                                             @Valid @RequestBody ActualizarEspacioRequest request) {
        Optional<EspacioEstacionamiento> espacioOpt = espacioRepository.findById(espacioId);
        if (!espacioOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Espacio no encontrado"));
        }

        EspacioEstacionamiento espacio = espacioOpt.get();

        // Solo permitir cambiar ciertos campos si el espacio no está ocupado
        if (espacio.estaOcupado() && request.getEstado() != null && request.getEstado() != EstadoEspacio.OCUPADO) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: No se puede cambiar el estado de un espacio ocupado"));
        }

        // Actualizar campos
        if (request.getTipo() != null) {
            espacio.setTipo(request.getTipo());
        }
        if (request.getEstado() != null) {
            espacio.setEstado(request.getEstado());
        }
        if (request.getDescripcion() != null) {
            espacio.setDescripcion(request.getDescripcion());
        }
        if (request.getTarifaPorHora() != null) {
            espacio.setTarifaPorHora(request.getTarifaPorHora());
        }

        espacioRepository.save(espacio);

        return ResponseEntity.ok(new MessageResponse("Espacio " + espacio.getNumero() + " actualizado exitosamente"));
    }

    // Cambiar estado de un espacio (mantenimiento, disponible, etc.)
    @PutMapping("/{espacioId}/estado")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> cambiarEstadoEspacio(@PathVariable Long espacioId,
                                                @RequestParam EstadoEspacio nuevoEstado) {
        Optional<EspacioEstacionamiento> espacioOpt = espacioRepository.findById(espacioId);
        if (!espacioOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Espacio no encontrado"));
        }

        EspacioEstacionamiento espacio = espacioOpt.get();

        // Validaciones según el nuevo estado
        if (nuevoEstado == EstadoEspacio.OCUPADO && espacio.estaOcupado()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El espacio ya está ocupado"));
        }

        // No permitir poner OCUPADO manualmente (solo por registro de entrada)
        if (nuevoEstado == EstadoEspacio.OCUPADO) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El estado OCUPADO solo se asigna automáticamente"));
        }

        espacio.setEstado(nuevoEstado);
        espacioRepository.save(espacio);

        String estadoTexto = "";
        switch (nuevoEstado) {
            case DISPONIBLE:
                estadoTexto = "disponible";
                break;
            case RESERVADO:
                estadoTexto = "reservado";
                break;
            case MANTENIMIENTO:
                estadoTexto = "en mantenimiento";
                break;
            default:
                estadoTexto = nuevoEstado.toString().toLowerCase();
        }

        return ResponseEntity.ok(new MessageResponse("Espacio " + espacio.getNumero() + " marcado como " + estadoTexto));
    }

    // Eliminar un espacio (solo si no está ocupado)
    @DeleteMapping("/{espacioId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> eliminarEspacio(@PathVariable Long espacioId) {
        Optional<EspacioEstacionamiento> espacioOpt = espacioRepository.findById(espacioId);
        if (!espacioOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Espacio no encontrado"));
        }

        EspacioEstacionamiento espacio = espacioOpt.get();

        if (espacio.estaOcupado()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: No se puede eliminar un espacio ocupado"));
        }

        espacioRepository.delete(espacio);
        return ResponseEntity.ok(new MessageResponse("Espacio " + espacio.getNumero() + " eliminado exitosamente"));
    }

    // Ver todos los espacios con su estado actual
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerTodosLosEspacios() {
        List<EspacioEstacionamiento> espacios = espacioRepository.findAll();
        List<EspacioEstacionamientoResponse> espaciosResponse = espacios.stream()
                .map(this::convertirEspacioAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(espaciosResponse);
    }

    // Inicializar estacionamiento con espacios predeterminados
    @PostMapping("/inicializar-default")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> inicializarEspaciosDefault() {
        // Verificar si ya existen espacios
        if (espacioRepository.count() > 0) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Ya existen espacios creados. Use crear-masivo para agregar más"));
        }

        int espaciosCreados = 0;
        String[] filas = {"A", "B", "C", "D", "E"};  // 5 filas
        int espaciosPorFila = 10;  // 10 espacios por fila = 50 total

        for (String fila : filas) {
            for (int i = 1; i <= espaciosPorFila; i++) {
                String numeroEspacio = fila + i;
                
                TipoEspacio tipo = TipoEspacio.REGULAR;
                // Hacer algunos espacios especiales
                if (i <= 2) {
                    tipo = TipoEspacio.DISCAPACITADO;  // A1, A2, B1, B2, etc. para discapacitados
                } else if (i >= 9) {
                    tipo = TipoEspacio.VIP;  // A9, A10, B9, B10, etc. espacios VIP
                }

                EspacioEstacionamiento espacio = new EspacioEstacionamiento(
                        numeroEspacio,
                        tipo,
                        "Espacio de estacionamiento " + numeroEspacio,
                        tipo == TipoEspacio.VIP ? 10.0 : tipo == TipoEspacio.DISCAPACITADO ? 3.0 : 5.0
                );
                espacioRepository.save(espacio);
                espaciosCreados++;
            }
        }

        return ResponseEntity.ok(new MessageResponse(
                "Estacionamiento inicializado con " + espaciosCreados + " espacios (A1-E10)"));
    }

    // Método de conversión
    private EspacioEstacionamientoResponse convertirEspacioAResponse(EspacioEstacionamiento espacio) {
        return new EspacioEstacionamientoResponse(
                espacio.getId(),
                espacio.getNumero(),
                espacio.getTipo(),
                espacio.getEstado(),
                espacio.getDescripcion(),
                espacio.getTarifaPorHora()
        );
    }

    // ============= CLASES REQUEST INTERNAS =============

    public static class CrearEspacioRequest {
        private String numero;
        private TipoEspacio tipo;
        private String descripcion;
        private Double tarifaPorHora;

        // Getters y setters
        public String getNumero() { return numero; }
        public void setNumero(String numero) { this.numero = numero; }
        public TipoEspacio getTipo() { return tipo; }
        public void setTipo(TipoEspacio tipo) { this.tipo = tipo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Double getTarifaPorHora() { return tarifaPorHora; }
        public void setTarifaPorHora(Double tarifaPorHora) { this.tarifaPorHora = tarifaPorHora; }
    }

    public static class CrearEspaciosMasivoRequest {
        private List<String> filas;  // ["A", "B", "C"]
        private Integer espaciosPorFila;  // 10
        private TipoEspacio tipoDefecto;  // REGULAR
        private Double tarifaDefecto;  // 5.0

        // Getters y setters
        public List<String> getFilas() { return filas; }
        public void setFilas(List<String> filas) { this.filas = filas; }
        public Integer getEspaciosPorFila() { return espaciosPorFila; }
        public void setEspaciosPorFila(Integer espaciosPorFila) { this.espaciosPorFila = espaciosPorFila; }
        public TipoEspacio getTipoDefecto() { return tipoDefecto; }
        public void setTipoDefecto(TipoEspacio tipoDefecto) { this.tipoDefecto = tipoDefecto; }
        public Double getTarifaDefecto() { return tarifaDefecto; }
        public void setTarifaDefecto(Double tarifaDefecto) { this.tarifaDefecto = tarifaDefecto; }
    }

    public static class ActualizarEspacioRequest {
        private TipoEspacio tipo;
        private EstadoEspacio estado;
        private String descripcion;
        private Double tarifaPorHora;

        // Getters y setters
        public TipoEspacio getTipo() { return tipo; }
        public void setTipo(TipoEspacio tipo) { this.tipo = tipo; }
        public EstadoEspacio getEstado() { return estado; }
        public void setEstado(EstadoEspacio estado) { this.estado = estado; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Double getTarifaPorHora() { return tarifaPorHora; }
        public void setTarifaPorHora(Double tarifaPorHora) { this.tarifaPorHora = tarifaPorHora; }
    }
}
