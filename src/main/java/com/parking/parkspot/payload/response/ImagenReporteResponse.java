package com.parking.parkspot.payload.response;

public class ImagenReporteResponse {
    
    private Long id;
    private String nombreArchivo;
    private String rutaArchivo;
    private String tipoArchivo;
    private Long tamaño;
    private String urlDescarga;
    
    // Constructor por defecto
    public ImagenReporteResponse() {}
    
    // Constructor con parámetros
    public ImagenReporteResponse(Long id, String nombreArchivo, String rutaArchivo, 
                               String tipoArchivo, Long tamaño, String urlDescarga) {
        this.id = id;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
        this.tipoArchivo = tipoArchivo;
        this.tamaño = tamaño;
        this.urlDescarga = urlDescarga;
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
    
    public String getUrlDescarga() { return urlDescarga; }
    public void setUrlDescarga(String urlDescarga) { this.urlDescarga = urlDescarga; }
}
