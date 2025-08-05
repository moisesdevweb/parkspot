package com.parking.parkspot.payload.request;

import jakarta.validation.constraints.NotBlank;

public class UpdatePerfilRequest {
    @NotBlank
    private String nombreCompleto;
    
    @NotBlank
    private String apellidos;
    
    private String direccion;
    private String telefono;

    // Getters y setters
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
