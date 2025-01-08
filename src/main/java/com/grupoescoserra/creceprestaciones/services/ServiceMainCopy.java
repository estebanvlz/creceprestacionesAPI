package com.grupoescoserra.creceprestaciones.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

import lombok.val;

@Service
public class ServiceMainCopy{

    private final String cookie = "_workspace_session=TFAwL1h5VlI1RHR3bS9GcGhQb3RWZmJWNS9FSldMNzVGdHhyQUdlWk85TjY3YUx4ZnVMamtSUWNyb1RoZVlsRnR4MHNESDhKd2tnOVdKY3p0c3V6eGxxejRXVFVkbEZTbXpwUXU2T0V4S0FWc0RCUVhBcFlKMnNqK2NzMFRQMXJuejRwYkdxRDFkSklxRlR4b0w4RjhEMVpuQkIwVm5DYUxCRU1jWFc0YTRhcFplanJsNEllc3ZlVnQxU2tqbDdWaUQydCs1YVRBUEZ4QkZKbThRUkpycm5sdjd0UVFCcTRKUHF0TGloenBYND0tLTY3TnFEbXRGcjhETTNjenMvWGNhMEE9PQ%3D%3D--b7273668d44d678b7738cb71f7ad08a3c024f86b";

    public List<PrestamosModel> fetchAllPrestamosInfo(){

        List<PrestamosModel> prestamosList = new ArrayList<>();
        List<HashMap<String, String>> info;
        try {
            info = fetchAllIdsAndCompanyName();
            
            for (HashMap<String, String> map : info) {
                if (map.get("id") != null) {
                    PrestamosModel prestamo = extractAllTheLoanInfo(map);
                    prestamosList.add(prestamo);
                }
            }
            
            return prestamosList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        
    }

    private List<HashMap<String, String>> fetchAllIdsAndCompanyName() throws IOException{
        try {
            // Url de la peticion.
            URL url = new URL("http://159.65.79.139/customers.json?");
            // Realiza la conexion al URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Establece los parametros de la conexion.
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", cookie);

            // Si mi peticion falla arrojo un error.
            if (connection.getResponseCode() != 200) {
                throw new IOException("Error al consultar los datoss. HTTP: " + connection.getResponseCode());
            }
                
            // Lectura de los datos extraidos por mi peticion Get HTTP.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            // Crea un StringBuilder para trabajar con la respuesta recibida por la peticion.
            StringBuilder httpResponse = new StringBuilder();
            // String temporal que almacenara las lineas de la respuesta
            String line;

            // Ciclo para leer linea por linea de la respuesta y guardarla en mi StringBuilder
            while ((line = reader.readLine()) != null) {
                httpResponse.append(line);
            }

            // Se cierra el objeto reader.
            reader.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(httpResponse.toString());

            // Creacion del mapa donde se almacenaran el id y el nombre de la compañia.
            List<HashMap<String, String>> datosClientes = StreamSupport.stream(rootNode.spliterator(), false)
                .map(clienteNode -> {
                    HashMap<String, String> customerMap = new HashMap<>();
                    customerMap.put("id", clienteNode.path("id").asText());
                    customerMap.put("empresa", clienteNode.path("empresa_donde_labora").asText());
                    return customerMap;
                })
                .collect(Collectors.toList()
            );

            // Devuelve mi mapa de clientes.
            return datosClientes;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error occurred while fetching data: " + e.getMessage());
        }
    }

    private File fetchPDF(String id){
        try {
            // Url de la peticion.
            URL url = new URL("http://159.65.79.139/reports/estado_de_cuenta.pdf?c_id=" + id + "&");   
            // Realiza la conexion al URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Establece los parametros de la conexion.
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", cookie);

            if ((connection.getResponseCode()) == 200) {
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
                System.err.println("No se pudo encontrar el pdf: " + connection.getResponseCode());
                return null;
            }

        } catch (Exception e) {
            System.out.println("Error");
        }
        return null;
    }

    // private HashMap<String, String> fetchASingleId(String id, List<HashMap<String, String>> allIds){
    //     try {
    //         return allIds.stream()
    //             .filter(map -> id.equals(map.get("id")))
    //             .findFirst()
    //             .orElse(null);

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }

    public PrestamosModel extractAllTheLoanInfo(HashMap<String, String> info){
        
        PrestamosModel prestamos = new PrestamosModel();
        ClienteModel cliente = new ClienteModel();
        List<PrestamoModel> arrPrestamos = new ArrayList<>();
        List<MovimientosModel> arrMoviemientos = new ArrayList<>();


        // HashMap<String, String> info = fetchASingleId(id);
        File pdfFIle = fetchPDF(info.get("id"));

        if (pdfFIle == null || info.isEmpty()) {
            return null;
        }

        try (PDDocument pdDoc = Loader.loadPDF(fetchPDF(info.get("id")))) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdDoc);
            text = text.replaceAll("\\*CAT \\(COSTO ANUAL TOTAL\\) 115\\.82% SIN IVA\\n?|\\*CONDUSEF\\n?COMISION PARA LA PROTECCION Y LA DEFENSA DE USUARIOS DE SERVICIOS FINANCIEROS\\. TEL \\(55\\) 5340 0999 8080 Y 01 800 999 8080 www\\.condusef\\.gob\\.mx\\n?","");

            // Extraer y setear la info del cliente.
            cliente.setEmpresa(info.get("empresa"));
            cliente.setNombreCliente(extractText("Cliente : (.+?) Fecha", "No se encontro el nombre de cliente", text));
            cliente.setNumeroCliente(extractInteger("cliente : (\\d+) Fecha", "No se encontro el nombre de cliente" , text));

            prestamos.setCliente(cliente);

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

                saldo.setCapitalPagado(
                    extractDouble("ado: \\$(\\d.+) C", "No se encontro el capital pagado", prestamoTemp)
                );

                saldo.setInteresPagado(
                    extractDouble("Interes pagado: \\$(\\d.+)", "No se encontro el interes pagado.", prestamoTemp)
                );

                saldo.setComisionesPagado(
                    extractDouble(": \\$(\\d.+) I", "No se encontraron las comiciones pagadas.", prestamoTemp)
                );

                saldo.setCapitalVencido(
                    extractDouble("ido: \\$(\\d.+) S", "No se encontro el capital vencido.", prestamoTemp)
                );

                saldo.setInteresVencido(
                    extractDouble("s Vencido: \\$(\\d.+)", "No se encontro el interes vencido.", prestamoTemp)
                );
                
                saldo.setInteresMoratorio(
                    extractDouble("\\$(\\d.+)F", "No se encontro el interes mortatorio", prestamoTemp)
                );

                saldo.setCapitalAPagar(
                    extractDouble("Capital a pagar: \\$(\\d.+)", "No se encontro el capital a pagar.", prestamoTemp)
                );

                saldo.setInteresAPagar(
                    extractDouble(":\\$(\\d.+)", "No se encontro el interes a pagar", prestamoTemp)
                );

                saldo.setSaldoActual(
                    extractDouble("Actual: \\$(\\d.+)", "No se encontro el saldo actual.", prestamoTemp)
                );
                

                prestamoTemp = prestamoTemp.substring(prestamoTemp.indexOf("F. OPERACION"), prestamoTemp.length());

                List<String> lines = Arrays.stream(prestamoTemp
                    .split("\n"))
                    .skip(1)
                    .collect(Collectors.toList()
                );

                arrMoviemientos = lines.stream()
                    .map(line -> {
                        Matcher match = Pattern.compile(
                            "(\\d.+) pago (\\w+) (\\d.+) \\$(\\d.+)")
                            .matcher(line);
                        if (match.matches()) {
                            MovimientosModel model = new MovimientosModel();
                            model.setFechaOperacion(match.group(1));
                            model.setMovimiento(match.group(2));
                            model.setFechaRecibo(match.group(3));
                            model.setImporte(Double.parseDouble(match.group(4)));
                            return model;
                        } else {
                            return null;
                        }
    
                    }).collect(Collectors.toList());
                
                prestamo.setMovimientos(arrMoviemientos.toArray(new MovimientosModel[0]));

                text = text.replace(text.substring(startIndex, endIndex), "");

                arrPrestamos.add(prestamo);

            } while (text.indexOf("FINANCIERA MARJO") != -1);

            PrestamoModel[] prestamosArray = arrPrestamos.toArray(new PrestamoModel[0]);

            prestamos.setPrestamos(prestamosArray);
            pdDoc.close();
            return prestamos;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractText(String pattern, String errorMessage, String str){
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return null;
        }
    }

    private Integer extractInteger(String pattern, String errorMessage, String str){
        String value = extractText(pattern, errorMessage, str);
        return (value != null) ? Integer.parseInt(value) : null; 
    }

    private Double extractDouble(String pattern, String errorMessage, String str){
        String value = extractText(pattern, errorMessage, str);
        return (value != null) ? Double.parseDouble(value.replace(",", "")) : null; 
    }
}
