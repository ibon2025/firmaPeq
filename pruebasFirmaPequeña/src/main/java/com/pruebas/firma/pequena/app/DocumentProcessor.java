package com.pruebas.firma.pequena.app;

import javax.swing.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Procesador de documentos Word
 * Responsable de crear, modificar e insertar imágenes en documentos .docx
 */
public class DocumentProcessor {
    
    /**
     * Crea un nuevo documento Word con la imagen insertada en el Content Control especificado.
     * Utiliza manipulación directa de ZIP para evitar dependencias pesadas.
     */
    public static byte[] crearDocumentoConImagen(byte[] documentBytes, byte[] imageBytes, 
                                                  String contentControlName, ImageProcessor imageProcessor,
                                                  JTextArea areaInfo) {
        try {
            // Abrir el documento .docx (que es un ZIP)
            ByteArrayInputStream bais = new ByteArrayInputStream(documentBytes);
            ZipInputStream zis = new ZipInputStream(bais);
            ZipEntry entry;
            Map<String, byte[]> zipContents = new java.util.HashMap<>();
            
            // Leer todos los archivos del ZIP
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] buffer = new byte[4096];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    zipContents.put(entry.getName(), baos.toByteArray());
                }
            }
            zis.close();
            
            // Obtener el document.xml del mapa
            String documentXmlPath = "word/document.xml";
            if (!zipContents.containsKey(documentXmlPath)) {
                areaInfo.append("✗ No se encontró word/document.xml en el documento\n");
                return null;
            }
            
            byte[] docXmlBytes = zipContents.get(documentXmlPath);
            String docXml = new String(docXmlBytes, "UTF-8");
            
            // Buscar el Content Control por su nombre y reemplazar su contenido
            String controlPattern = "<w:sdt>.*?<w:sdtPr>.*?<w:tag w:val=\"" + escapeRegex(contentControlName) + "\".*?</w:sdt>";
            
            if (!docXml.matches("(?s).*" + controlPattern + ".*")) {
                areaInfo.append("⚠ No se encontró el Content Control '" + contentControlName + "' en el documento\n");
                // Agregar la imagen al final del documento de todas formas
                docXml = agregarImagenAlFinal(docXml, imageBytes, zipContents, imageProcessor, areaInfo);
            } else {
                // Reemplazar el Content Control con uno que contenga la imagen
                docXml = reemplazarContentControlConImagen(docXml, contentControlName, imageBytes, zipContents, imageProcessor, areaInfo);
                areaInfo.append("✓ Imagen insertada en el Content Control\n");
            }
            
            // Actualizar el document.xml en el mapa
            zipContents.put(documentXmlPath, docXml.getBytes("UTF-8"));
            
            // Recrear el ZIP con los archivos modificados
            ByteArrayOutputStream baosZip = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baosZip);
            
            for (Map.Entry<String, byte[]> fileEntry : zipContents.entrySet()) {
                ZipEntry newEntry = new ZipEntry(fileEntry.getKey());
                zos.putNextEntry(newEntry);
                zos.write(fileEntry.getValue());
                zos.closeEntry();
            }
            
            zos.close();
            baosZip.close();
            
            return baosZip.toByteArray();
            
        } catch (Exception e) {
            System.err.println("Error creando documento con imagen: " + e.getMessage());
            e.printStackTrace();
            areaInfo.append("✗ Error: " + e.getMessage() + "\n");
            return null;
        }
    }
    
    /**
     * Reemplaza el contenido de un Content Control con la imagen
     */
    private static String reemplazarContentControlConImagen(String docXml, String contentControlName, 
                                                            byte[] imageBytes, Map<String, byte[]> zipContents,
                                                            ImageProcessor imageProcessor, JTextArea areaInfo) {
        try {
            // Encontrar el SDT que contiene este tag
            String tagSearch = "<w:tag w:val=\"" + contentControlName + "\"";
            int tagIndex = docXml.indexOf(tagSearch);
            if (tagIndex == -1) return docXml;
            
            // Retroceder para encontrar el <w:sdt> más cercano
            int sdtStartIndex = docXml.lastIndexOf("<w:sdt>", tagIndex);
            int sdtEndIndex = docXml.indexOf("</w:sdt>", tagIndex) + "</w:sdt>".length();
            
            if (sdtStartIndex == -1 || sdtEndIndex <= tagIndex) {
                return docXml;
            }
            
            // Extraer el SDT completo
            String originalSdt = docXml.substring(sdtStartIndex, sdtEndIndex);
            
            // Buscar el <w:sdtContent> dentro del SDT
            int contentStartIndex = originalSdt.indexOf("<w:sdtContent>");
            int contentEndIndex = originalSdt.indexOf("</w:sdtContent>");
            
            if (contentStartIndex == -1 || contentEndIndex == -1) {
                return docXml;
            }
            
            // Crear el nuevo contenido con la imagen
            String newImageContent = imageProcessor.crearElementoImagenWord(imageBytes, zipContents, areaInfo);
            
            // Construir el nuevo SDT reemplazando el contenido
            // Limpiador: eliminar cualquier formato anterior y mantener solo la imagen
            String nuevoSdt = originalSdt.substring(0, contentStartIndex + "<w:sdtContent>".length()) +
                            "\n" + newImageContent + "\n" +
                            originalSdt.substring(contentEndIndex);
            
            // Reemplazar en el documento
            return docXml.substring(0, sdtStartIndex) + nuevoSdt + docXml.substring(sdtEndIndex);
            
        } catch (Exception e) {
            System.err.println("Error reemplazando Content Control: " + e.getMessage());
            return docXml;
        }
    }
    
    /**
     * Agrega la imagen al final del documento si no se encontró el Content Control
     */
    private static String agregarImagenAlFinal(String docXml, byte[] imageBytes, 
                                                Map<String, byte[]> zipContents,
                                                ImageProcessor imageProcessor, JTextArea areaInfo) {
        try {
            // Encontrar </w:body>
            int bodyCloseIndex = docXml.lastIndexOf("</w:body>");
            if (bodyCloseIndex == -1) return docXml;
            
            // Crear el elemento de imagen
            String imageXml = imageProcessor.crearElementoImagenWord(imageBytes, zipContents, areaInfo);
            
            // Insertar antes de </w:body>
            return docXml.substring(0, bodyCloseIndex) + imageXml + docXml.substring(bodyCloseIndex);
            
        } catch (Exception e) {
            System.err.println("Error agregando imagen al final: " + e.getMessage());
            return docXml;
        }
    }
    
    /**
     * Escapa caracteres especiales en regex
     */
    private static String escapeRegex(String s) {
        return s.replaceAll("[.+*?^${}()|\\\\\\[\\]]", "\\\\$0");
    }
}
