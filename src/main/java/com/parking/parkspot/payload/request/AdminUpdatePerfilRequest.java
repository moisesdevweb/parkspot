package com.parking.parkspot.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminUpdatePerfilRequest {
    // Permite a admin/vigilante cambiar el estado (1=activo, 0=inactivo)
    private Integer estado;
    @NotBlank
    @Size(max = 120)
    @Email
    private String email;

    @NotBlank
    @Size(max = 100)
    private String nombreCompleto;

    @NotBlank
    @Size(max = 100)
    private String apellidos;

    @NotBlank
    @Size(max = 8, min = 8)
    private String dni;

    @Size(max = 200)
    private String direccion;

    @Size(max = 15)
    private String telefono;

    // Constructors
    public AdminUpdatePerfilRequest() {}

    public AdminUpdatePerfilRequest(String email, String nombreCompleto, String apellidos, 
                                   String dni, String direccion, String telefono, Integer estado) {
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.apellidos = apellidos;
        this.dni = dni;
        this.direccion = direccion;
        this.telefono = telefono;
        this.estado = estado;
    }

    // Getters y setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getEstado() { return estado; }
    public void setEstado(Integer estado) { this.estado = estado; }
}
