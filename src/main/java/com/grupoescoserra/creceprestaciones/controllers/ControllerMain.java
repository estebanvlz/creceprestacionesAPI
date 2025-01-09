package com.grupoescoserra.creceprestaciones.controllers;

import java.util.HashMap;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupoescoserra.creceprestaciones.models.PrestamosModel;
import com.grupoescoserra.creceprestaciones.services.ServiceMain;

@RestController
@RequestMapping(value = "crece")
public class ControllerMain {

    private ServiceMain service;

    public ControllerMain(ServiceMain service){
        this.service = service;
    }

    @GetMapping("/prestamos")
    public List<PrestamosModel> function(){
        return service.fetchAllLoansInfo();
    }

    @GetMapping("/movimientos")
    public List<PrestamosModel> function2(@RequestParam("month") String month, @RequestParam("year") String year){
        return service.fetchAllLoanInfoByMovementMonthAndYear(month, year);
    }

}
