package com.grupoescoserra.creceprestaciones.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ServiceMainCopy{

    private final String cookie = "_workspace_session=TFAwL1h5VlI1RHR3bS9GcGhQb3RWZmJWNS9FSldMNzVGdHhyQUdlWk85TjY3YUx4ZnVMamtSUWNyb1RoZVlsRnR4MHNESDhKd2tnOVdKY3p0c3V6eGxxejRXVFVkbEZTbXpwUXU2T0V4S0FWc0RCUVhBcFlKMnNqK2NzMFRQMXJuejRwYkdxRDFkSklxRlR4b0w4RjhEMVpuQkIwVm5DYUxCRU1jWFc0YTRhcFplanJsNEllc3ZlVnQxU2tqbDdWaUQydCs1YVRBUEZ4QkZKbThRUkpycm5sdjd0UVFCcTRKUHF0TGloenBYND0tLTY3TnFEbXRGcjhETTNjenMvWGNhMEE9PQ%3D%3D--b7273668d44d678b7738cb71f7ad08a3c024f86b";


    public List<HashMap<String, String>> fetchAllIdsAndCompanyName() throws IOException{
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

    public File fetchPDF(String id){
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




}
