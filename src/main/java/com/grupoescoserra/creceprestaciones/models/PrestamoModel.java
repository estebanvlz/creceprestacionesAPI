package com.grupoescoserra.creceprestaciones.models;

import lombok.Data;

@Data
public class PrestamoModel {
    private int cuenta;
    private double capitalConcedido;
    private String periocidad;
    private int plazoPactado;
    private int tasaInteres;
    private String fechaApertura;
    private String fechaVencimiento;
    private boolean terminado;
    private SaldoModel saldo;
    private MovimientosModel movimientos;
}
