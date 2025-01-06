package com.grupoescoserra.creceprestaciones.models;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grupoescoserra.creceprestaciones.services.ServiceMain;
import com.grupoescoserra.creceprestaciones.services.ServiceMainCopy;

@RestController
@RequestMapping(value = "crece")
public class ControllerMain {
    private ServiceMain service;
    private ServiceMainCopy service2;

    public ControllerMain(ServiceMain service, ServiceMainCopy service2){
        this.service = service;
        this.service2 = service2;
    }

    // @GetMapping("/prestamos")
    // public PrestamosModel function(@RequestParam("id") String id) throws IOException{
    //     return service.pdf2text(id);
    // }

    @GetMapping("/test")
    public List<PrestamosModel> function2() throws IOException{
        return service.fetchAllPrestamos();
    }

    @GetMapping("/test1")
    public List<Map<String, String>> function3() throws IOException{
        return service.fetchIds();
    }


    @GetMapping("/prueba")
    public ResponseEntity<List<HashMap<String, String>>> prueba(){
        try {
            return new ResponseEntity<>(service2.fetchAllIdsAndCompanyName(), HttpStatus.OK);
        } catch (Exception exception) {
            System.out.println("Error");
        }
        return null;

    }

    @GetMapping("/prueba2")
    public File prueba2(@RequestParam("id") String id){
        return service2.fetchPDF(id);
    }
}
