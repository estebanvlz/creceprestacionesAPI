package com.grupoescoserra.creceprestaciones.models;

import lombok.Data;

@Data
public class MovimientosModel {
    private String fechaOperacion;
    private String movimiento;
    private String fechaRecibo;
    private double importe;
}
