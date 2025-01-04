package com.grupoescoserra.creceprestaciones;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupoescoserra.creceprestaciones.models.PrestamosModel;

@RestController
@RequestMapping(value = "test")
public class ControllerMain {
    private ServiceMain service;

    public ControllerMain(ServiceMain service){
        this.service = service;
    }

    @GetMapping
    public PrestamosModel function() throws IOException{
        File file = new ClassPathResource("files/report.pdf").getFile();
        return service.pdf2text(file);
    }
}
