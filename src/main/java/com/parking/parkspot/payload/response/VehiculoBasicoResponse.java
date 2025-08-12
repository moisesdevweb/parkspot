package com.parking.parkspot.payload.response;

import com.parking.parkspot.model.enums.TipoVehiculo;

public class VehiculoBasicoResponse {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private TipoVehiculo tipo;
    private Integer año;
    private String color;

    // Constructor
    public VehiculoBasicoResponse(Long id, String placa, String marca, String modelo, TipoVehiculo tipo, Integer año, String color) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.tipo = tipo;
        this.año = año;
        this.color = color;
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

    public TipoVehiculo getTipo() { return tipo; }
    public void setTipo(TipoVehiculo tipo) { this.tipo = tipo; }

    public Integer getAño() { return año; }
    public void setAño(Integer año) { this.año = año; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
