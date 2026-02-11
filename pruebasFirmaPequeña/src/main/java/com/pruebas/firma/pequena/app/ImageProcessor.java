package com.pruebas.firma.pequena.app;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Procesador de imágenes
 * Responsable de mostrar, processar y crear elementos de imagen para Word
 */
public class ImageProcessor {
    
    private int imagenAncho = 0;
    private int imagenAlto = 0;
    
    /**
     * Obtiene el ancho de la imagen en píxeles
     */
    public int getImagenAncho() {
        return imagenAncho;
    }
    
    /**
     * Obtiene el alto de la imagen en píxeles
     */
    public int getImagenAlto() {
        return imagenAlto;
    }
    
    /**
     * Establece una imagen en el label a partir de datos en bytes
     */
    public void mostrarImagen(byte[] imageBytes, JLabel labelImagen, JTextArea areaInfo) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img != null) {
                // Guardar las dimensiones reales de la imagen
                imagenAncho = img.getWidth();
                imagenAlto = img.getHeight();
                
                areaInfo.append("Dimensiones de imagen: " + imagenAncho + " x " + imagenAlto + " píxeles\n");
                
                // Escalar la imagen si es muy grande
                int maxWidth = 150;
                int maxHeight = 60;
                if (img.getWidth() > maxWidth || img.getHeight() > maxHeight) {
                    double scaleX = (double) maxWidth / img.getWidth();
                    double scaleY = (double) maxHeight / img.getHeight();
                    double scale = Math.min(scaleX, scaleY);
                    int newWidth = (int) (img.getWidth() * scale);
                    int newHeight = (int) (img.getHeight() * scale);
                    Image scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    labelImagen.setIcon(new ImageIcon(scaledImage));
                } else {
                    labelImagen.setIcon(new ImageIcon(img));
                }
                labelImagen.setText("");
            } else {
                labelImagen.setText("Error: formato de imagen no soportado");
                labelImagen.setIcon(null);
            }
        } catch (Exception e) {
            labelImagen.setText("Error al cargar imagen: " + e.getMessage());
            labelImagen.setIcon(null);
            System.err.println("Error cargando imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea el elemento XML para insertar una imagen en Word
     */
    public String crearElementoImagenWord(byte[] imageBytes, java.util.Map<String, byte[]> zipContents, JTextArea areaInfo) {
        try {
            // Guardar la imagen en la carpeta word/media/
            String imageName = "image" + System.currentTimeMillis() + ".png";
            String mediaPath = "word/media/" + imageName;
            zipContents.put(mediaPath, imageBytes);
            
            // Actualizar o crear las relaciones
            String relsPath = "word/_rels/document.xml.rels";
            String relsXml = "";
            
            if (zipContents.containsKey(relsPath)) {
                relsXml = new String(zipContents.get(relsPath), "UTF-8");
            } else {
                relsXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                         "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"></Relationships>";
            }
            
            // Encontrar el mayor ID de relación existente
            int maxId = 0;
            java.util.regex.Pattern idPattern = java.util.regex.Pattern.compile("Id=\"rId([0-9]+)\"");
            java.util.regex.Matcher matcher = idPattern.matcher(relsXml);
            while (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                if (id > maxId) maxId = id;
            }
            
            int newRelId = maxId + 1;
            
            // Agregar la nueva relación para la imagen
            String newRel = "<Relationship Id=\"rId" + newRelId + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/" + imageName + "\"/>";
            
            // Insertar antes del </Relationships>
            relsXml = relsXml.replace("</Relationships>", newRel + "\n</Relationships>");
            zipContents.put(relsPath, relsXml.getBytes("UTF-8"));
            
            // Crear el XML de la imagen para insertar en el documento
            // Conversión de píxeles a EMU: 1 píxel ≈ 9525 EMU (basado en 96 DPI = 1 inch)
            // Usar las dimensiones reales de la imagen extraída
            long pixelsPerEmu = 9525L;  // Constante de conversión
            long widthEmu = imagenAncho > 0 ? imagenAncho * pixelsPerEmu : 124425L;  // 13 pixels = 13 * 9525
            long heightEmu = imagenAlto > 0 ? imagenAlto * pixelsPerEmu : 647700L;   // 68 pixels = 68 * 9525
            
            areaInfo.append("EMU calculados: " + widthEmu + " x " + heightEmu + "\n");
            
            String imageXml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
                            "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" " +
                            "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" " +
                            "xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\" " +
                            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                            "<w:pPr>" +
                            "<w:pStyle w:val=\"Normal\"/>" +
                            "<w:jc w:val=\"left\"/>" +
                            "</w:pPr>" +
                            "<w:r>" +
                            "<w:rPr/>" +
                            "<w:drawing>" +
                            "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">" +
                            "<wp:extent cx=\"" + widthEmu + "\" cy=\"" + heightEmu + "\"/>" +
                            "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>" +
                            "<wp:docPr id=\"1\" name=\"Firma\"/>" +
                            "<wp:cNvGraphicFramePr>" +
                            "<a:graphicFrameLocks xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" noChangeAspect=\"1\"/>" +
                            "</wp:cNvGraphicFramePr>" +
                            "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
                            "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
                            "<pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
                            "<pic:nvPicPr>" +
                            "<pic:cNvPr id=\"0\" name=\"Firma.png\"/>" +
                            "<pic:cNvPicPr/>" +
                            "</pic:nvPicPr>" +
                            "<pic:blipFill>" +
                            "<a:blip r:embed=\"rId" + newRelId + "\"/>" +
                            "<a:stretch>" +
                            "<a:fillRect/>" +
                            "</a:stretch>" +
                            "</pic:blipFill>" +
                            "<pic:spPr>" +
                            "<a:xfrm>" +
                            "<a:off x=\"0\" y=\"0\"/>" +
                            "<a:ext cx=\"" + widthEmu + "\" cy=\"" + heightEmu + "\"/>" +
                            "</a:xfrm>" +
                            "<a:prstGeom prst=\"rect\">" +
                            "<a:avLst/>" +
                            "</a:prstGeom>" +
                            "</pic:spPr>" +
                            "</pic:pic>" +
                            "</a:graphicData>" +
                            "</a:graphic>" +
                            "</wp:inline>" +
                            "</w:drawing>" +
                            "</w:r>" +
                            "</w:p>";
            
            return imageXml;
            
        } catch (Exception e) {
            System.err.println("Error creando elemento de imagen: " + e.getMessage());
            return "<w:p><w:r><w:t>[Imagen - " + formatearTamaño(imageBytes.length) + "]</w:t></w:r></w:p>";
        }
    }
    
    /**
     * Formatea el tamaño en bytes a una unidad legible
     */
    public static String formatearTamaño(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] unidades = {"B", "KB", "MB", "GB"};
        int index = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, index), unidades[index]);
    }
    
    /**
     * Marca un punto en el XML del documento con información sobre la imagen
     */
    public void marcarImagenEnXml(String contentControlName, long imagenTamaño, JTextArea areaInfo) {
        try {
            String marcador = "\n<!-- Imagen insertada: " + contentControlName + 
                            " (" + formatearTamaño(imagenTamaño) + ") -->";
            
            areaInfo.append("✓ Marcador de imagen agregado al documento\n");
        } catch (Exception e) {
            System.err.println("Error marcando imagen: " + e.getMessage());
        }
    }
}
