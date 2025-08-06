package com.parking.parkspot.payload.request;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

public class AdminUpdateClienteCompletoRequest {
    
    // DATOS DE USUARIO
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    // DATOS PERSONALES
    @NotBlank
    @Size(max = 100)
    private String nombreCompleto;

    @NotBlank
    @Size(max = 100)
    private String apellidos;

    @NotBlank
    @Size(min = 8, max = 8)
    private String dni;

    @Size(max = 200)
    private String direccion;

    @Size(max = 15)
    private String telefono;

    // LISTA DE VEHÍCULOS
    @Valid
    private List<VehiculoUpdateRequest> vehiculos;

    // Constructor por defecto
    public AdminUpdateClienteCompletoRequest() {}

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

    public List<VehiculoUpdateRequest> getVehiculos() { return vehiculos; }
    public void setVehiculos(List<VehiculoUpdateRequest> vehiculos) { this.vehiculos = vehiculos; }
}
