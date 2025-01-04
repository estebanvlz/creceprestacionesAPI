package com.grupoescoserra.creceprestaciones.models;

import lombok.Data;

@Data
public class SaldoModel {
    private double capitalPagado;
    private double interesPagado;
    private double comicionesPagado;
    private double capitalVencido;
    private double interesVencido;
    private double interesMortatorio;
    private double capitalAPagar;
    private double interesAPagar;
    private double saldoActual;
}
