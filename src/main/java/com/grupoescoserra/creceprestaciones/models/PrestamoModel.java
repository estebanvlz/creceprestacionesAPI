package com.grupoescoserra.creceprestaciones.models;

import lombok.Data;

@Data
public class PrestamoModel {
    private int cuenta;
    private double capitalConcedido;
    private String periocidad;
    private int plazoPactado;
    private double tasaInteres;
    private String fechaApertura;
    private String fechaVencimiento;
    private SaldoModel saldo;
    private String numeroPagoActual;
    private boolean terminado;
    private MovimientosModel[] movimientos;
}
