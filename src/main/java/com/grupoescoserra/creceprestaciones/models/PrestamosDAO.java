package com.grupoescoserra.creceprestaciones.models;

import lombok.Data;

@Data
public class PrestamosDAO {
    private ClienteModel cliente;
    private PrestamoModel[] prestamos;
}
