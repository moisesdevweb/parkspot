package com.parking.parkspot.payload.response;

import java.util.List;

public class PerfilCompletoResponse {
    private Long id;
    private String username;
    private String email;
    private String nombreCompleto;
    private String apellidos;
    private String dni;
    private String direccion;
    private String telefono;
    private Integer estado;
    private List<String> roles; // Puede ser null
    private List<VehiculoBasicoResponse> vehiculos; // ¡AGREGAR ESTE CAMPO!

    // Constructor SIN roles (para perfil propio)
    public PerfilCompletoResponse(Long id, String username, String email, String nombreCompleto, 
                                 String apellidos, String dni, String direccion, String telefono, Integer estado) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.apellidos = apellidos;
        this.dni = dni;
        this.direccion = direccion;
        this.telefono = telefono;
        this.estado = estado;
        this.roles = null; // Sin roles
    }

    // Constructor CON roles (para listas de admin/vigilante)
    public PerfilCompletoResponse(Long id, String username, String email, String nombreCompleto, 
                                 String apellidos, String dni, String direccion, String telefono, 
                                 Integer estado, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.apellidos = apellidos;
        this.dni = dni;
        this.direccion = direccion;
        this.telefono = telefono;
        this.estado = estado;
        this.roles = roles;
    }

    // AGREGAR NUEVO CONSTRUCTOR CON VEHÍCULOS:
    public PerfilCompletoResponse(Long id, String username, String email, String nombreCompleto, 
                                 String apellidos, String dni, String direccion, String telefono, 
                                 Integer estado, List<String> roles, List<VehiculoBasicoResponse> vehiculos) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.apellidos = apellidos;
        this.dni = dni;
        this.direccion = direccion;
        this.telefono = telefono;
        this.estado = estado;
        this.roles = roles;
        this.vehiculos = vehiculos; // ¡NUEVO CAMPO!
    }

    // Getters y setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
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
    
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    // AGREGAR GETTER Y SETTER PARA VEHÍCULOS:
    public List<VehiculoBasicoResponse> getVehiculos() { return vehiculos; }
    public void setVehiculos(List<VehiculoBasicoResponse> vehiculos) { this.vehiculos = vehiculos; }
}
