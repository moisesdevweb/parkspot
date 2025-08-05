package com.parking.parkspot.payload.response;

public class PerfilCompletoResponse {
    private Long id;
    private String username;        // Solo mostrar, NO editable
    private String email;           // Solo mostrar, NO editable
    private String nombreCompleto;  // EDITABLE
    private String apellidos;       // EDITABLE
    private String dni;             // Solo mostrar, NO editable
    private String direccion;       // EDITABLE
    private String telefono;        // EDITABLE
    private Integer estado;

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
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
}
