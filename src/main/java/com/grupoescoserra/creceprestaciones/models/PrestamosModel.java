package com.grupoescoserra.creceprestaciones.models;

import lombok.Data;

@Data
public class PrestamosModel {
    private ClienteModel cliente;
    private PrestamoModel[] prestamos;
}
