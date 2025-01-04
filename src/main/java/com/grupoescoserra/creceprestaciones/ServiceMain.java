package com.grupoescoserra.creceprestaciones;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.grupoescoserra.creceprestaciones.models.ClienteModel;
import com.grupoescoserra.creceprestaciones.models.MovimientosModel;
import com.grupoescoserra.creceprestaciones.models.PrestamoModel;
import com.grupoescoserra.creceprestaciones.models.PrestamosModel;
import com.grupoescoserra.creceprestaciones.models.SaldoModel;

@Service
public class ServiceMain {
    

    public PrestamosModel pdf2text(File pdFile) throws IOException{
        
        PrestamosModel prestamos = new PrestamosModel();
        ClienteModel cliente = new ClienteModel();
        PrestamoModel prestamo = new PrestamoModel();
        SaldoModel saldo = new SaldoModel();
        MovimientosModel movimiento = new MovimientosModel();
        List<PrestamoModel> arr = new ArrayList<>();

        PDDocument pdDoc = Loader.loadPDF(pdFile);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(pdDoc);
        
        //Se extraen los datos del cliente.
        Matcher clientNameMatcher = Pattern.compile("Cliente : (.+?) Fecha de vencimiento").matcher(text);
        if (clientNameMatcher.find()) {
            cliente.setNombreCliente((clientNameMatcher.group(1).trim()));
        } else {
            System.out.println("No se encontro el nombre del cliente.");
        }
        Matcher clientNumMatcher = Pattern.compile("No\\. de cliente : (\\d+)").matcher(text);
        if (clientNumMatcher.find()) {
            cliente.setNumeroCliente(Integer.parseInt(clientNumMatcher.group(1)));
        } else {
            System.out.println("No se encontro el numero del cliente.");
        }

        prestamos.setCliente(cliente);

        System.out.println(cliente.toString());  

        text = text.replaceAll("\\*CAT \\(COSTO ANUAL TOTAL\\) 115\\.82% SIN IVA\\n?|\\*CONDUSEF\\n?COMISION PARA LA PROTECCION Y LA DEFENSA DE USUARIOS DE SERVICIOS FINANCIEROS\\. TEL \\(55\\) 5340 0999 8080 Y 01 800 999 8080 www\\.condusef\\.gob\\.mx\\n?", "");

        int i = 0;
        do {
            int startIndex = text.indexOf("FINANCIERA MARJO");
            if (startIndex == -1) break; 
            int endIndex = text.indexOf("FINANCIERA MARJO", startIndex + 1);
            if (endIndex == -1) {
                endIndex = text.length(); 
            }

            String prestamoTemp = text.substring(startIndex, endIndex);
            // text = prestamoTemp;

            doubleMapper("Capital pagado:\\s?\\$([\\d,\\.]+) Capital Vencido", "No se encontro el capital pagado.", saldo::setCapitalPagado, prestamoTemp);
            doubleMapper("Interes pagado:\\s?\\$([\\d,\\.]+)", "No se encontro el interes pagado.", saldo::setInteresPagado, prestamoTemp);
            doubleMapper("Comisiones pagado: \\s?\\$([\\d,\\.]+)", "No se encontraron las comiciones pagadas", saldo::setComisionesPagado, prestamoTemp);
            doubleMapper("Capital Vencido:\\s?\\$([\\d,\\.]+) Saldo", "No se encontro el capital vencido", saldo::setCapitalVencido, prestamoTemp);
            doubleMapper("Interes Vencido: \\s?\\$([\\d,\\.]+)", "No se encontro el interes vencido", saldo::setInteresVencido, prestamoTemp);
            doubleMapper("Interes Moratorio: \\s?\\$([\\d,\\.]+)", "No se encontro el interes mortatorio", saldo::setInteresMoratorio, prestamoTemp);
            doubleMapper("Capital a pagar: \\s?\\$([\\d,\\.]+)", "No se encontro el capital a pagar", saldo::setCapitalAPagar, prestamoTemp);
            doubleMapper("Interes a pagar:\\s?\\$([\\d,\\.]+)", "No se encontro el interes a pagar", saldo::setInteresAPagar, prestamoTemp);
            doubleMapper(" Saldo Actual:\\s?\\$([\\d,\\.]+)", "No se encontro el saldo actual", saldo::setSaldoActual, prestamoTemp);

            System.out.println(saldo.toString());

            Matcher cuentaMatcher = Pattern.compile("Cuenta : (\\d+)").matcher(prestamoTemp);
            if (cuentaMatcher.find()) {
                // System.out.println(cuentaMatcher.group(1));
                prestamo.setCuenta(Integer.parseInt(
                    cuentaMatcher.group(1)
                ));
            } else {

            }

            doubleMapper("Capital Concedido: \\s?\\$([\\d,\\.]+)", "No se encontro el capital concedido", prestamo::setCapitalConcedido, prestamoTemp);
            
            Matcher periocidadMatcher = Pattern.compile("Periocidad: \\s*(\\w+)").matcher(prestamoTemp);
            if (periocidadMatcher.find()) {
                // System.out.println(periocidadMapper.group(1));
                prestamo.setPeriocidad(periocidadMatcher.group(1));
            } else {
                System.out.println("no se encontro la periocidad");
            }

            Matcher plazcoPactadoMatcher = Pattern.compile("Plazo pactado:\\s*(\\d+)").matcher(prestamoTemp);
            if (plazcoPactadoMatcher.find()) {
                // System.out.println(plazcoPactadoMatcher.group(1));
                prestamo.setPlazoPactado(Integer.parseInt(plazcoPactadoMatcher.group(1)));
            } else {
                System.out.println("no se encontro el plazo pactado");
            }

            doubleMapper("Tasa de Interes:\\s*(\\d+\\.\\d+)%", "valio berga", prestamo::setTasaInteres, prestamoTemp);


            System.out.println(prestamo.toString());
            prestamo.setSaldo(saldo);
            text = text.replace(text.substring(startIndex, endIndex),"");
            arr.add(prestamo);
            i++;
        } while (text.indexOf("FINANCIERA MARJO") != -1);
        // } while (i != 1);
        PrestamoModel[] prestamosArray = arr.toArray(new PrestamoModel[0]);

        // Set the prestamos array in PrestamosModel
        prestamos.setPrestamos(prestamosArray);
        pdDoc.close();  
        return prestamos;
    }

    private void doubleMapper(String pattern, String errorMessage, Consumer<Double> setter, String str) {
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        if (matcher.find()) {
            setter.accept(Double.parseDouble(matcher.group(1).replace(",", "").trim()));
        } else {
            System.out.println(errorMessage);
        }
    }
}
