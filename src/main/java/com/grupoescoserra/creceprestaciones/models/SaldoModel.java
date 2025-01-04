package com.grupoescoserra.creceprestaciones.models;

import lombok.Data;

@Data
public class SaldoModel {
    private double capitalPagado;
    private double interesPagado;
    private double comisionesPagado;
    private double capitalVencido;
    private double interesVencido;
    private double interesMoratorio;
    private double capitalAPagar;
    private double interesAPagar;
    private double saldoActual;
}
