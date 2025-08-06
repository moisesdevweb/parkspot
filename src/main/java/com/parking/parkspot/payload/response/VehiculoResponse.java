package com.parking.parkspot.payload.response;

import com.parking.parkspot.model.enums.TipoVehiculo;

public class VehiculoResponse {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private String color;
    private Integer año;
    private TipoVehiculo tipo;
    private Integer estado;
    
    // Info básica del cliente (sin referencia circular)
    private Long clienteId;
    private String clienteNombre;

    // Constructor
    public VehiculoResponse(Long id, String placa, String marca, String modelo, 
                           String color, Integer año, TipoVehiculo tipo, Integer estado,
                           Long clienteId, String clienteNombre) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.color = color;
        this.año = año;
        this.tipo = tipo;
        this.estado = estado;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getAño() { return año; }
    public void setAño(Integer año) { this.año = año; }

    public TipoVehiculo getTipo() { return tipo; }
    public void setTipo(TipoVehiculo tipo) { this.tipo = tipo; }

    public Integer getEstado() { return estado; }
    public void setEstado(Integer estado) { this.estado = estado; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
}
