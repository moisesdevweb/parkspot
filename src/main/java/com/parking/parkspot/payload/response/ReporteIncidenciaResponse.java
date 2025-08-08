package com.parking.parkspot.payload.response;

import com.parking.parkspot.model.enums.EstadoReporte;
import java.time.LocalDateTime;
import java.util.List;

public class ReporteIncidenciaResponse {
    
    private Long id;
    private String descripcion;
    private EstadoReporte estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String comentarioAdmin;
    
    // Información del vigilante
    private Long vigilanteId;
    private String vigilanteNombre;
    private String vigilanteUsername;
    
    // Información del cliente
    private Long clienteId;
    private String clienteNombre;
    private String clienteApellidos;
    private String clienteDni;
    
    // Información del vehículo
    private Long vehiculoId;
    private String vehiculoPlaca;
    private String vehiculoMarca;
    private String vehiculoModelo;
    
    // Información del admin actualizador (opcional)
    private Long adminActualizadorId;
    private String adminActualizadorNombre;
    
    // Imágenes del reporte
    private List<ImagenReporteResponse> imagenes;
    
    // Constructor por defecto
    public ReporteIncidenciaResponse() {}
    
    // Constructor completo
    public ReporteIncidenciaResponse(Long id, String descripcion, EstadoReporte estado,
                                   LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion,
                                   String comentarioAdmin, Long vigilanteId, String vigilanteNombre,
                                   String vigilanteUsername, Long clienteId, String clienteNombre,
                                   String clienteApellidos, String clienteDni, Long vehiculoId,
                                   String vehiculoPlaca, String vehiculoMarca, String vehiculoModelo,
                                   Long adminActualizadorId, String adminActualizadorNombre,
                                   List<ImagenReporteResponse> imagenes) {
        this.id = id;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.comentarioAdmin = comentarioAdmin;
        this.vigilanteId = vigilanteId;
        this.vigilanteNombre = vigilanteNombre;
        this.vigilanteUsername = vigilanteUsername;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.clienteApellidos = clienteApellidos;
        this.clienteDni = clienteDni;
        this.vehiculoId = vehiculoId;
        this.vehiculoPlaca = vehiculoPlaca;
        this.vehiculoMarca = vehiculoMarca;
        this.vehiculoModelo = vehiculoModelo;
        this.adminActualizadorId = adminActualizadorId;
        this.adminActualizadorNombre = adminActualizadorNombre;
        this.imagenes = imagenes;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    public String getComentarioAdmin() { return comentarioAdmin; }
    public void setComentarioAdmin(String comentarioAdmin) { this.comentarioAdmin = comentarioAdmin; }
    
    public Long getVigilanteId() { return vigilanteId; }
    public void setVigilanteId(Long vigilanteId) { this.vigilanteId = vigilanteId; }
    
    public String getVigilanteNombre() { return vigilanteNombre; }
    public void setVigilanteNombre(String vigilanteNombre) { this.vigilanteNombre = vigilanteNombre; }
    
    public String getVigilanteUsername() { return vigilanteUsername; }
    public void setVigilanteUsername(String vigilanteUsername) { this.vigilanteUsername = vigilanteUsername; }
    
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    
    public String getClienteApellidos() { return clienteApellidos; }
    public void setClienteApellidos(String clienteApellidos) { this.clienteApellidos = clienteApellidos; }
    
    public String getClienteDni() { return clienteDni; }
    public void setClienteDni(String clienteDni) { this.clienteDni = clienteDni; }
    
    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }
    
    public String getVehiculoPlaca() { return vehiculoPlaca; }
    public void setVehiculoPlaca(String vehiculoPlaca) { this.vehiculoPlaca = vehiculoPlaca; }
    
    public String getVehiculoMarca() { return vehiculoMarca; }
    public void setVehiculoMarca(String vehiculoMarca) { this.vehiculoMarca = vehiculoMarca; }
    
    public String getVehiculoModelo() { return vehiculoModelo; }
    public void setVehiculoModelo(String vehiculoModelo) { this.vehiculoModelo = vehiculoModelo; }
    
    public Long getAdminActualizadorId() { return adminActualizadorId; }
    public void setAdminActualizadorId(Long adminActualizadorId) { this.adminActualizadorId = adminActualizadorId; }
    
    public String getAdminActualizadorNombre() { return adminActualizadorNombre; }
    public void setAdminActualizadorNombre(String adminActualizadorNombre) { this.adminActualizadorNombre = adminActualizadorNombre; }
    
    public List<ImagenReporteResponse> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenReporteResponse> imagenes) { this.imagenes = imagenes; }
}
