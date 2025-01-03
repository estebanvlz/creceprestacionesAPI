package com.grupoescoserra.creceprestaciones;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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
        System.out.println(numeroPrestamos);
        
        pdDoc.close();  
        return text;
    }
}
