package com.parking.parkspot.payload.request;

import com.parking.parkspot.model.enums.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VehiculoUpdateRequest {
    private Long id; // Puede ser null para vehículos nuevos
    
    @NotBlank
    @Size(max = 10)
    private String placa;

    @NotBlank
    @Size(max = 50)
    private String marca;

    @NotBlank
    @Size(max = 50)
    private String modelo;

    @Size(max = 20)
    private String color;

    private Integer año;

    private TipoVehiculo tipo;

    // Constructor por defecto
    public VehiculoUpdateRequest() {}

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
}
