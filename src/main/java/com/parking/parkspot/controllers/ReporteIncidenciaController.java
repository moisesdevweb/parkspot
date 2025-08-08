package com.parking.parkspot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.parking.parkspot.model.entity.ImagenReporte;
import com.parking.parkspot.model.entity.ReporteIncidencia;
import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.entity.Vehiculo;
import com.parking.parkspot.model.enums.EstadoReporte;
import com.parking.parkspot.payload.request.ActualizarEstadoReporteRequest;
import com.parking.parkspot.payload.request.CrearReporteRequest;
import com.parking.parkspot.payload.response.ImagenReporteResponse;
import com.parking.parkspot.payload.response.MessageResponse;
import com.parking.parkspot.payload.response.ReporteIncidenciaResponse;
import com.parking.parkspot.repository.ImagenReporteRepository;
import com.parking.parkspot.repository.ReporteIncidenciaRepository;
import com.parking.parkspot.repository.UserRepository;
import com.parking.parkspot.repository.VehiculoRepository;
import com.parking.parkspot.service.FileStorageService;

import jakarta.validation.Valid;
import java.nio.file.Path;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reportes")
public class ReporteIncidenciaController {

    @Autowired
    private ReporteIncidenciaRepository reporteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private ImagenReporteRepository imagenReporteRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // VIGILANTE puede crear reporte de incidencia
    @PostMapping("/crear")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> crearReporte(@Valid @RequestBody CrearReporteRequest reporteRequest,
                                         Authentication authentication) {
        // Obtener usuario vigilante actual
        String username = authentication.getName();
        Optional<User> vigilanteOpt = userRepository.findByUsername(username);
        
        if (!vigilanteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vigilante no encontrado"));
        }

        User vigilante = vigilanteOpt.get();

        // Verificar que el cliente existe y es CLIENTE
        Optional<User> clienteOpt = userRepository.findById(reporteRequest.getClienteId());
        if (!clienteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Cliente no encontrado"));
        }

        User cliente = clienteOpt.get();
        boolean esCliente = cliente.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_CLIENTE"));
        
        if (!esCliente) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El usuario seleccionado no es un cliente"));
        }

        // Verificar que el vehículo existe y pertenece al cliente
        Optional<Vehiculo> vehiculoOpt = vehiculoRepository.findById(reporteRequest.getVehiculoId());
        if (!vehiculoOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vehículo no encontrado"));
        }

        Vehiculo vehiculo = vehiculoOpt.get();
        if (!vehiculo.getCliente().getId().equals(cliente.getId())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El vehículo no pertenece al cliente seleccionado"));
        }

        // Crear el reporte
        ReporteIncidencia reporte = new ReporteIncidencia(
                reporteRequest.getDescripcion(),
                vigilante,
                cliente,
                vehiculo
        );

        reporteRepository.save(reporte);

        return ResponseEntity.ok(new MessageResponse("Reporte de incidencia creado exitosamente. ID: " + reporte.getId()));
    }

    // VIGILANTE puede subir imágenes a un reporte que creó
    @PostMapping("/{reporteId}/imagenes")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> subirImagenes(@PathVariable Long reporteId,
                                          @RequestParam("imagenes") MultipartFile[] archivos,
                                          Authentication authentication) {
        // Verificar que el reporte existe
        Optional<ReporteIncidencia> reporteOpt = reporteRepository.findById(reporteId);
        if (!reporteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Reporte no encontrado"));
        }

        ReporteIncidencia reporte = reporteOpt.get();

        // Verificar que el vigilante actual es quien creó el reporte
        String username = authentication.getName();
        if (!reporte.getVigilante().getUsername().equals(username)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo puedes subir imágenes a tus propios reportes"));
        }

        // Verificar que el reporte esté en estado PENDIENTE
        if (reporte.getEstado() != EstadoReporte.PENDIENTE) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Solo se pueden agregar imágenes a reportes en estado PENDIENTE"));
        }

        List<String> nombresArchivos = new ArrayList<>();

        try {
            // Procesar cada archivo
            for (MultipartFile archivo : archivos) {
                if (!archivo.isEmpty()) {
                    // Validar tipo de archivo (solo imágenes)
                    String contentType = archivo.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Error: Solo se permiten archivos de imagen"));
                    }

                    // Guardar archivo en el sistema de archivos
                    String nombreArchivo = fileStorageService.storeFile(archivo);
                    
                    // Crear registro en base de datos
                    ImagenReporte imagen = new ImagenReporte(
                            archivo.getOriginalFilename(),
                            fileStorageService.getFileStorageLocation() + "/" + nombreArchivo,
                            contentType,
                            archivo.getSize()
                    );
                    
                    reporte.agregarImagen(imagen);
                    nombresArchivos.add(nombreArchivo);
                }
            }

            reporteRepository.save(reporte);

            return ResponseEntity.ok(new MessageResponse(
                    "Imágenes subidas exitosamente. Archivos: " + String.join(", ", nombresArchivos)));

        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error al subir las imágenes: " + ex.getMessage()));
        }
    }

    // Descargar imagen de reporte
    @GetMapping("/imagen/{imagenId}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Resource> descargarImagen(@PathVariable Long imagenId) {
        try {
            Optional<ImagenReporte> imagenOpt = imagenReporteRepository.findById(imagenId);
            if (!imagenOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            ImagenReporte imagen = imagenOpt.get();
            String nombreArchivo = imagen.getRutaArchivo().substring(imagen.getRutaArchivo().lastIndexOf("/") + 1);
            
            Path path = fileStorageService.loadFileAsResource(nombreArchivo);
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imagen.getTipoArchivo()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + imagen.getNombreArchivo() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // VIGILANTE puede ver sus propios reportes
    @GetMapping("/mis-reportes")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerMisReportes(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> vigilanteOpt = userRepository.findByUsername(username);
        
        if (!vigilanteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vigilante no encontrado"));
        }

        User vigilante = vigilanteOpt.get();
        List<ReporteIncidencia> reportes = reporteRepository.findByVigilante(vigilante);
        
        List<ReporteIncidenciaResponse> reportesResponse = reportes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reportesResponse);
    }

    // VIGILANTE puede ver reportes por estado
    @GetMapping("/mis-reportes/{estado}")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerMisReportesPorEstado(@PathVariable String estado,
                                                        Authentication authentication) {
        String username = authentication.getName();
        Optional<User> vigilanteOpt = userRepository.findByUsername(username);
        
        if (!vigilanteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Vigilante no encontrado"));
        }

        try {
            EstadoReporte estadoEnum = EstadoReporte.valueOf(estado.toUpperCase());
            User vigilante = vigilanteOpt.get();
            
            List<ReporteIncidencia> reportes = reporteRepository.findByVigilanteAndEstadoOrderByFechaCreacionDesc(
                    vigilante, estadoEnum);
            
            List<ReporteIncidenciaResponse> reportesResponse = reportes.stream()
                    .map(this::convertirAResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(reportesResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Estado inválido. Use: PENDIENTE, APROBADO o CANCELADO"));
        }
    }

    // ADMIN puede ver todos los reportes
    @GetMapping("/todos")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerTodosLosReportes() {
        List<ReporteIncidencia> reportes = reporteRepository.findAllByOrderByFechaCreacionDesc();
        
        List<ReporteIncidenciaResponse> reportesResponse = reportes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reportesResponse);
    }

    // ADMIN puede ver reportes por estado
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerReportesPorEstado(@PathVariable String estado) {
        try {
            EstadoReporte estadoEnum = EstadoReporte.valueOf(estado.toUpperCase());
            List<ReporteIncidencia> reportes = reporteRepository.findByEstado(estadoEnum);
            
            List<ReporteIncidenciaResponse> reportesResponse = reportes.stream()
                    .map(this::convertirAResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(reportesResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Estado inválido. Use: PENDIENTE, APROBADO o CANCELADO"));
        }
    }

    // ADMIN puede buscar reportes por nombre de cliente
    @GetMapping("/buscar-cliente")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> buscarReportesPorCliente(@RequestParam String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El parámetro 'nombre' es requerido"));
        }

        List<ReporteIncidencia> reportes = reporteRepository.findByClienteNombreContaining(nombre.trim());
        
        List<ReporteIncidenciaResponse> reportesResponse = reportes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reportesResponse);
    }

    // ADMIN puede buscar reportes por placa de vehículo
    @GetMapping("/buscar-vehiculo")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> buscarReportesPorVehiculo(@RequestParam String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El parámetro 'placa' es requerido"));
        }

        List<ReporteIncidencia> reportes = reporteRepository.findByVehiculoPlacaContaining(placa.trim());
        
        List<ReporteIncidenciaResponse> reportesResponse = reportes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reportesResponse);
    }

    // ADMIN puede ver un reporte específico
    @GetMapping("/{reporteId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerReporte(@PathVariable Long reporteId) {
        Optional<ReporteIncidencia> reporteOpt = reporteRepository.findById(reporteId);
        
        if (!reporteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Reporte no encontrado"));
        }

        ReporteIncidencia reporte = reporteOpt.get();
        ReporteIncidenciaResponse reporteResponse = convertirAResponse(reporte);

        return ResponseEntity.ok(reporteResponse);
    }

    // ADMIN puede actualizar el estado de un reporte
    @PutMapping("/{reporteId}/estado")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> actualizarEstadoReporte(@PathVariable Long reporteId,
                                                    @Valid @RequestBody ActualizarEstadoReporteRequest estadoRequest,
                                                    Authentication authentication) {
        // Verificar que el reporte existe
        Optional<ReporteIncidencia> reporteOpt = reporteRepository.findById(reporteId);
        if (!reporteOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Reporte no encontrado"));
        }

        // Obtener admin actual
        String username = authentication.getName();
        Optional<User> adminOpt = userRepository.findByUsername(username);
        if (!adminOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Administrador no encontrado"));
        }

        ReporteIncidencia reporte = reporteOpt.get();
        User admin = adminOpt.get();

        // Verificar que no se esté cambiando al mismo estado
        if (reporte.getEstado() == estadoRequest.getNuevoEstado()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El reporte ya se encuentra en ese estado"));
        }

        // Actualizar el estado del reporte
        reporte.actualizarEstado(
                estadoRequest.getNuevoEstado(),
                admin,
                estadoRequest.getComentarioAdmin()
        );

        reporteRepository.save(reporte);

        String mensajeEstado = "";
        switch (estadoRequest.getNuevoEstado()) {
            case APROBADO:
                mensajeEstado = "aprobado";
                break;
            case CANCELADO:
                mensajeEstado = "cancelado";
                break;
            case PENDIENTE:
                mensajeEstado = "marcado como pendiente";
                break;
        }

        return ResponseEntity.ok(new MessageResponse("Reporte " + mensajeEstado + " exitosamente"));
    }

    // Convertir entidad a response
    private ReporteIncidenciaResponse convertirAResponse(ReporteIncidencia reporte) {
        // Convertir imágenes
        List<ImagenReporteResponse> imagenesResponse = reporte.getImagenes().stream()
                .map(imagen -> new ImagenReporteResponse(
                        imagen.getId(),
                        imagen.getNombreArchivo(),
                        imagen.getRutaArchivo(),
                        imagen.getTipoArchivo(),
                        imagen.getTamaño(),
                        "/api/reportes/imagen/" + imagen.getId()
                ))
                .collect(Collectors.toList());

        return new ReporteIncidenciaResponse(
                reporte.getId(),
                reporte.getDescripcion(),
                reporte.getEstado(),
                reporte.getFechaCreacion(),
                reporte.getFechaActualizacion(),
                reporte.getComentarioAdmin(),
                reporte.getVigilante().getId(),
                reporte.getVigilante().getPersona().getNombreCompleto(),
                reporte.getVigilante().getUsername(),
                reporte.getCliente().getId(),
                reporte.getCliente().getPersona().getNombreCompleto(),
                reporte.getCliente().getPersona().getApellidos(),
                reporte.getCliente().getPersona().getDni(),
                reporte.getVehiculo().getId(),
                reporte.getVehiculo().getPlaca(),
                reporte.getVehiculo().getMarca(),
                reporte.getVehiculo().getModelo(),
                reporte.getAdminActualizador() != null ? reporte.getAdminActualizador().getId() : null,
                reporte.getAdminActualizador() != null ? reporte.getAdminActualizador().getPersona().getNombreCompleto() : null,
                imagenesResponse
        );
    }
}
