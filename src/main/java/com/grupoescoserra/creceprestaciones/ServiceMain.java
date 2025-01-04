package com.grupoescoserra.creceprestaciones;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class ServiceMain {
    

    public String pdf2text(File pdFile) throws IOException{
        
        PDDocument pdDoc = Loader.loadPDF(pdFile);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(pdDoc); 
        
        int numeroPrestamos = 0;  
        Matcher matcher = Pattern.compile("FINANCIERA MARJO", Pattern.CASE_INSENSITIVE).matcher(text);
        while (matcher.find()) numeroPrestamos++;
    
        for(int i = 0; i < numeroPrestamos; i++){
            int startIndex = text.indexOf("FINANCIERA MARJO");
            int endIndex = text.indexOf("FINANCIERA MARJO", text.indexOf("FINANCIERA MARJO") + 1);
            System.out.println(startIndex);
            System.out.println(endIndex);

            text.replaceAll("", "");
        }

        // do {

        // } while (text.indexOf("FINANCIERA MARJO", text.indexOf("FINANCIERA MARJO") + 1) != text.lastIndexOf("FINANCIERA MARJO"));

        pdDoc.close();  
        return text;
    }
}
