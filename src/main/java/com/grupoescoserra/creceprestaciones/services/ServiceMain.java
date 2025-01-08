package com.grupoescoserra.creceprestaciones.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupoescoserra.creceprestaciones.models.ClienteModel;
import com.grupoescoserra.creceprestaciones.models.MovimientosModel;
import com.grupoescoserra.creceprestaciones.models.PrestamoModel;
import com.grupoescoserra.creceprestaciones.models.PrestamosModel;
import com.grupoescoserra.creceprestaciones.models.SaldoModel;

@Service
public class ServiceMain {

    private final String cookie = "_workspace_session=TFAwL1h5VlI1RHR3bS9GcGhQb3RWZmJWNS9FSldMNzVGdHhyQUdlWk85TjY3YUx4ZnVMamtSUWNyb1RoZVlsRnR4MHNESDhKd2tnOVdKY3p0c3V6eGxxejRXVFVkbEZTbXpwUXU2T0V4S0FWc0RCUVhBcFlKMnNqK2NzMFRQMXJuejRwYkdxRDFkSklxRlR4b0w4RjhEMVpuQkIwVm5DYUxCRU1jWFc0YTRhcFplanJsNEllc3ZlVnQxU2tqbDdWaUQydCs1YVRBUEZ4QkZKbThRUkpycm5sdjd0UVFCcTRKUHF0TGloenBYND0tLTY3TnFEbXRGcjhETTNjenMvWGNhMEE9PQ%3D%3D--b7273668d44d678b7738cb71f7ad08a3c024f86b";

    public List<Map<String, String>> fetchIds() throws IOException {
        try {
            URL url = new URL("http://159.65.79.139/customers.json?");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", cookie);

            if (connection.getResponseCode() != 200) {
                throw new IOException("Failed to fetch data. HTTP code: " + connection.getResponseCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());

            List<Map<String, String>> customerList = new ArrayList<>();

            for (JsonNode customerNode : rootNode) {
                String id = customerNode.path("id").asText();
                String empresa = customerNode.path("empresa_donde_labora").asText();

                // Create a map for each customer
                Map<String, String> customerMap = new HashMap<>();
                customerMap.put("id", id);
                customerMap.put("empresa", empresa);

                customerList.add(customerMap);
            }

            return customerList;

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error occurred while fetching data: " + e.getMessage());
        }
    }


    public List<PrestamosModel> fetchAllPrestamos() throws IOException {
        List<Map<String, String>> ids = fetchIds();
        List<PrestamosModel> prestamosList = new ArrayList<>();
    
        for (Map<String, String> map : ids) {
            String id = map.get("id");
            String empresa = map.get("empresa");
            if (id != null) {
                PrestamosModel prestamo = pdf2text(id, empresa);
                prestamosList.add(prestamo);
            }
        }
    
        return prestamosList;
    }




    private File fetchPdf(String id) throws IOException {
        URL url = new URL("http://159.65.79.139/reports/estado_de_cuenta.pdf?c_id=" + id + "&");        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", cookie);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            File tempPdf = File.createTempFile("tempPdf", ".pdf");
            try (
                InputStream inputStream = connection.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(tempPdf)
            ) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempPdf;
        } else {
            System.err.println("No se pudo encontrar el pdf: " + responseCode);
            return null;
        }
    }

    public PrestamosModel pdf2text(String id, String empresa) throws IOException {

        PrestamosModel prestamos = new PrestamosModel();
        ClienteModel cliente = new ClienteModel();
        List<PrestamoModel> arrPrestamos = new ArrayList<>();
        List<MovimientosModel> arrMoviemientos = new ArrayList<>();
        File pdfFile = fetchPdf(id);
        if (pdfFile == null) {
            System.err.println("Failed to fetch PDF for id: " + id);
            return null;  
        }
        PDDocument pdDoc = Loader.loadPDF(fetchPdf(id));

        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(pdDoc);

        // Se extraen los datos del cliente.
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

        cliente.setEmpresa(empresa);
        // se mapea la info del cliente
        prestamos.setCliente(cliente);

        text = text.replaceAll("\\*CAT \\(COSTO ANUAL TOTAL\\) 115\\.82% SIN IVA\\n?|\\*CONDUSEF\\n?COMISION PARA LA PROTECCION Y LA DEFENSA DE USUARIOS DE SERVICIOS FINANCIEROS\\. TEL \\(55\\) 5340 0999 8080 Y 01 800 999 8080 www\\.condusef\\.gob\\.mx\\n?","");

        do {

            PrestamoModel prestamo = new PrestamoModel();
            SaldoModel saldo = new SaldoModel();

            int startIndex = text.indexOf("FINANCIERA MARJO");
            if (startIndex == -1)
                break;
            int endIndex = text.indexOf("FINANCIERA MARJO", startIndex + 1);
            if (endIndex == -1) {
                endIndex = text.length();
            }

            String prestamoTemp = text.substring(startIndex, endIndex);

            doubleMapper("Capital pagado:\\s?\\$([\\d,\\.]+) Capital Vencido", "No se encontro el capital pagado.",
                    saldo::setCapitalPagado, prestamoTemp);
            doubleMapper("Interes pagado:\\s?\\$([\\d,\\.]+)", "No se encontro el interes pagado.",
                    saldo::setInteresPagado, prestamoTemp);
            doubleMapper("Comisiones pagado: \\s?\\$([\\d,\\.]+)", "No se encontraron las comiciones pagadas",
                    saldo::setComisionesPagado, prestamoTemp);
            doubleMapper("Capital Vencido:\\s?\\$([\\d,\\.]+) Saldo", "No se encontro el capital vencido",
                    saldo::setCapitalVencido, prestamoTemp);
            doubleMapper("Interes Vencido: \\s?\\$([\\d,\\.]+)", "No se encontro el interes vencido",
                    saldo::setInteresVencido, prestamoTemp);
            doubleMapper("Interes Moratorio: \\s?\\$([\\d,\\.]+)", "No se encontro el interes mortatorio",
                    saldo::setInteresMoratorio, prestamoTemp);
            doubleMapper("Capital a pagar: \\s?\\$([\\d,\\.]+)", "No se encontro el capital a pagar",
                    saldo::setCapitalAPagar, prestamoTemp);
            doubleMapper("Interes a pagar:\\s?\\$([\\d,\\.]+)", "No se encontro el interes a pagar",
                    saldo::setInteresAPagar, prestamoTemp);
            doubleMapper(" Saldo Actual:\\s?\\$([\\d,\\.]+)", "No se encontro el saldo actual", saldo::setSaldoActual,
                    prestamoTemp);

            Matcher cuentaMatcher = Pattern.compile("Cuenta : (\\d+)").matcher(prestamoTemp);
            if (cuentaMatcher.find()) {
                // System.out.println(cuentaMatcher.group(1));
                prestamo.setCuenta(Integer.parseInt(
                        cuentaMatcher.group(1)));
            } else {

            }

            doubleMapper("Capital Concedido: \\s?\\$([\\d,\\.]+)", "No se encontro el capital concedido",
                    prestamo::setCapitalConcedido, prestamoTemp);

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

            Matcher fechaAperturaMatcher = Pattern.compile("Fecha de apertura:\\s*(.+)").matcher(prestamoTemp);
            if (fechaAperturaMatcher.find()) {
                // System.out.println(fechaAperturaMatcher.group(1));
                prestamo.setFechaApertura(fechaAperturaMatcher.group(1));
            } else {
                System.out.println("No se encontro la fecha de apertura.");
            }

            Matcher fechaVencimientoMatcher = Pattern.compile("Fecha de vencimiento:\\s*(.+)").matcher(prestamoTemp);
            if (fechaVencimientoMatcher.find()) {
                // System.out.println(fechaVencimientoMatcher.group(1));
                prestamo.setFechaVencimiento(fechaVencimientoMatcher.group(1));
            } else {
                System.out.println("No se encontro la fecha de apertura.");
            }

            prestamo.setSaldo(saldo);

            // Movimientos
            prestamoTemp = prestamoTemp.substring(prestamoTemp.indexOf("F. OPERACION"), prestamoTemp.length());

            List<String> lines = Arrays.stream(prestamoTemp
                    .split("\n"))
                    .skip(1)
                    .collect(Collectors.toList());

            arrMoviemientos = lines.stream()
                    .map(line -> {
                        Matcher matcher = Pattern.compile(
                                "(\\d{2}/\\d{2}/\\d{4})\\s+(pago\\s+\\w+)\\s+(\\d{2}/\\d{2}/\\d{4})\\s+\\$([\\d,]+\\.\\d{2})")
                                .matcher(line);
                        if (matcher.matches()) {
                            MovimientosModel model = new MovimientosModel();
                            model.setFechaOperacion(matcher.group(1));
                            model.setMovimiento(matcher.group(2));
                            model.setFechaRecibo(matcher.group(3));
                            model.setImporte(Double.parseDouble(matcher.group(4).replace(",", "")));
                            return model;
                        } else {
                            return null;
                        }
                    })
                    .collect(Collectors.toList());

            prestamo.setMovimientos(arrMoviemientos.toArray(new MovimientosModel[0]));

            text = text.replace(text.substring(startIndex, endIndex), "");

            arrPrestamos.add(prestamo);

        } while (text.indexOf("FINANCIERA MARJO") != -1);

        PrestamoModel[] prestamosArray = arrPrestamos.toArray(new PrestamoModel[0]);

        //
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

    public String text(){

        return "";
    }
}
