package com.parking.parkspot.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "imagen_reporte")
public class ImagenReporte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombreArchivo;
    
    @Column(nullable = false)
    private String rutaArchivo;
    
    @Column(nullable = false)
    private String tipoArchivo;
    
    @Column(nullable = false)
    private Long tamaño;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id")
    private ReporteIncidencia reporte;
    
    // Constructores
    public ImagenReporte() {}
    
    public ImagenReporte(String nombreArchivo, String rutaArchivo, String tipoArchivo, Long tamaño) {
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
        this.tipoArchivo = tipoArchivo;
        this.tamaño = tamaño;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    
    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    
    public Long getTamaño() { return tamaño; }
    public void setTamaño(Long tamaño) { this.tamaño = tamaño; }
    
    public ReporteIncidencia getReporte() { return reporte; }
    public void setReporte(ReporteIncidencia reporte) { this.reporte = reporte; }
}
