package com.parking.parkspot.payload.response;

import com.parking.parkspot.model.enums.TipoVehiculo;

public class VehiculoBasicoResponse {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private TipoVehiculo tipo;

    // Constructor
    public VehiculoBasicoResponse(Long id, String placa, String marca, String modelo, TipoVehiculo tipo) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.tipo = tipo;
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
}
