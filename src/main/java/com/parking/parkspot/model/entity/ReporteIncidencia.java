package com.parking.parkspot.model.entity;

import com.parking.parkspot.model.enums.EstadoReporte;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reporte_incidencia")
public class ReporteIncidencia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000)
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    private LocalDateTime fechaActualizacion;
    
    // Vigilante que crea el reporte
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vigilante_id", nullable = false)
    private User vigilante;
    
    // Cliente involucrado en la incidencia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;
    
    // Vehículo involucrado en la incidencia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;
    
    // Admin que actualiza el estado (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_actualizador_id")
    private User adminActualizador;
    
    // Comentario del admin al cambiar estado (opcional)
    @Column(length = 500)
    private String comentarioAdmin;
    
    // Imágenes del reporte
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImagenReporte> imagenes = new ArrayList<>();
    
    // Constructores
    public ReporteIncidencia() {}
    
    public ReporteIncidencia(String descripcion, User vigilante, User cliente, Vehiculo vehiculo) {
        this.descripcion = descripcion;
        this.vigilante = vigilante;
        this.cliente = cliente;
        this.vehiculo = vehiculo;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoReporte.PENDIENTE;
    }
    
    // Métodos de utilidad
    public void agregarImagen(ImagenReporte imagen) {
        imagenes.add(imagen);
        imagen.setReporte(this);
    }
    
    public void actualizarEstado(EstadoReporte nuevoEstado, User admin, String comentario) {
        this.estado = nuevoEstado;
        this.adminActualizador = admin;
        this.comentarioAdmin = comentario;
        this.fechaActualizacion = LocalDateTime.now();
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
    
    public User getVigilante() { return vigilante; }
    public void setVigilante(User vigilante) { this.vigilante = vigilante; }
    
    public User getCliente() { return cliente; }
    public void setCliente(User cliente) { this.cliente = cliente; }
    
    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }
    
    public User getAdminActualizador() { return adminActualizador; }
    public void setAdminActualizador(User adminActualizador) { this.adminActualizador = adminActualizador; }
    
    public String getComentarioAdmin() { return comentarioAdmin; }
    public void setComentarioAdmin(String comentarioAdmin) { this.comentarioAdmin = comentarioAdmin; }
    
    public List<ImagenReporte> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenReporte> imagenes) { this.imagenes = imagenes; }
}
