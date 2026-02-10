package com.pruebas.firma.pequena.app;

// Descomenta estas importaciones si añades la dependencia de docx4j al pom.xml
/*
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingMLPart;
import org.docx4j.wml.BinaryPartAbstractImage;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Inline;
*/

import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Consumer;

/**
 * Clase principal de la aplicación de pruebas de firma pequeña
 */
public class App {
    
    public static void main(String[] args) {
        System.out.println("Bienvenido a Pruebas Firma Pequeña");
    }
    
    public String saludo(String nombre) {
        return "Hola, " + nombre;
    }

    /**
     * Extrae una imagen en formato base64 de un contenido XML dado un nombre de tag
     *
     * @param xmlContent Contenido XML donde buscar
     * @param tagName Nombre del tag que contiene la imagen base64
     * @return Bytes de la imagen decodificada, o null si no se encuentra
     */
    public byte[] extraerImagenBase64DelXml(String xmlContent, String tagName) {
        return extraerImagenBase64DelXml(xmlContent, tagName, null);
    }

    /**
     * Extrae una imagen en formato base64 de un contenido XML dado un nombre de tag
     *
     * @param xmlContent Contenido XML donde buscar
     * @param tagName Nombre del tag que contiene la imagen base64
     * @param debugLogger Función para registrar mensajes de debug (puede ser null)
     * @return Bytes de la imagen decodificada, o null si no se encuentra
     */
    public byte[] extraerImagenBase64DelXml(String xmlContent, String tagName, Consumer<String> debugLogger) {
        if (xmlContent == null || xmlContent.isEmpty() || tagName == null) {
            if (debugLogger != null) debugLogger.accept("DEBUG: xmlContent es null/vacío o tagName es null");
            return null;
        }

        try {
            // Buscar todas las variaciones posibles del tag
            String[] patterns = {
                // Patrón directo y simple para imagenFirmaPeq
                "<" + Pattern.quote(tagName) + ">([^<]+)</" + Pattern.quote(tagName) + ">",
                // Patrón original
                "<" + Pattern.quote(tagName) + ">([^<]*)</" + Pattern.quote(tagName) + ">",
                // Con namespaces
                "<[^>]*:" + Pattern.quote(tagName) + ">([^<]*)</[^>]*:" + Pattern.quote(tagName) + ">",
                // Codificado como entidades HTML
                "&lt;" + Pattern.quote(tagName) + "&gt;([^&]*)&lt;/" + Pattern.quote(tagName) + "&gt;",
                // Búsqueda más amplia - cualquier contenido entre tags firmaPeq
                "<" + Pattern.quote(tagName) + "[^>]*>(.*?)</" + Pattern.quote(tagName) + ">",
                // Con namespaces y atributos
                "<[^>]*" + Pattern.quote(tagName) + "[^>]*>(.*?)</[^>]*" + Pattern.quote(tagName) + ">",
                // FORMATO WORD: Content Control con tag w:val="firmaPeq" - buscar base64 en elementos de imagen
                "<w:tag\\s+w:val=\"" + Pattern.quote(tagName) + "\"[^>]*>.*?<w:sdtContent[^>]*>.*?<w:drawing[^>]*>.*?<wp:docPr[^>]*>.*?<a:blip[^>]*r:embed=\"[^\"]*\"[^>]*>.*?([A-Za-z0-9+/=]{100,})",
                // FORMATO WORD: Buscar base64 en cualquier elemento de imagen dentro del content control
                "<w:tag\\s+w:val=\"" + Pattern.quote(tagName) + "\"[^>]*>.*?<w:sdtContent[^>]*>.*?<w:pict[^>]*>.*?([A-Za-z0-9+/=]{100,})",
                // FORMATO WORD: Buscar base64 en cualquier pkg:binaryData que aparezca después del tag
                "(?s)" + Pattern.quote(tagName) + ".*?<pkg:binaryData[^>]*>([A-Za-z0-9+/=]{50,})</pkg:binaryData>",
                // Patron adicional: buscar cualquier cadena base64 larga (mas de 100 caracteres) que pueda estar en cualquier tag
                // Este patron es util cuando no se conoce exactamente el nombre del tag
                "(?:" + Pattern.quote(tagName) + "|[A-Za-z0-9_-]+)>([A-Za-z0-9+/=]{100,})</(?:\\1|[A-Za-z0-9_-]+)"
            };
            
            for (int i = 0; i < patterns.length; i++) {
                String pattern = patterns[i];
                if (debugLogger != null) debugLogger.accept("DEBUG: Probando patrón " + (i+1) + ": " + pattern);

                Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher m = p.matcher(xmlContent);

                if (m.find()) {
                    String base64Data = m.group(1).trim();
                    if (debugLogger != null) debugLogger.accept("DEBUG: ¡ÉXITO! Patrón " + (i+1) + " encontró contenido: " +
                        (base64Data.length() > 50 ? base64Data.substring(0, 50) + "..." : base64Data));

                    if (!base64Data.isEmpty()) {
                        // Limpiar posibles entidades HTML si existen
                        base64Data = base64Data.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
                        if (debugLogger != null) debugLogger.accept("DEBUG: Decodificando base64 de longitud: " + base64Data.length());
                        return Base64.getDecoder().decode(base64Data);
                    } else {
                        if (debugLogger != null) debugLogger.accept("DEBUG: El contenido encontrado está vacío");
                    }
                } else {
                    if (debugLogger != null) debugLogger.accept("DEBUG: Patrón " + (i+1) + " no coincidió");
                }
            }

            // BUSQUEDA ESPECIFICA PARA WORD: Si encontramos el tag pero no los datos, buscar base64 en el contexto
            if (xmlContent.contains("<w:tag w:val=\"" + tagName + "\"")) {
                if (debugLogger != null) debugLogger.accept("DEBUG: ¡Encontrado tag Word! <w:tag w:val=\"" + tagName + "\"> - buscando datos base64 en el contexto...");

                // Buscar el content control específico que contiene EXACTAMENTE el tag firmaPeq
                Pattern contentControlPattern = Pattern.compile(
                    "<w:sdt[^>]*>.*?<w:tag\\s+w:val=\"" + Pattern.quote(tagName) + "\"[^>]*>.*?</w:sdt>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
                );
                Matcher contentMatcher = contentControlPattern.matcher(xmlContent);

                if (contentMatcher.find()) {
                    String contentControl = contentMatcher.group();
                    if (debugLogger != null) debugLogger.accept("DEBUG: Content control específico de '" + tagName + "' encontrado, analizando...");

                    // Verificar si es un picture content control
                    if (contentControl.contains("<w:picture/>") || contentControl.contains("w:picture")) {
                        if (debugLogger != null) debugLogger.accept("DEBUG: Es un Picture Content Control - la imagen podría estar en archivos separados");

                        // Buscar referencias a imágenes embebidas en el content control
                        Pattern embedPattern = Pattern.compile("r:embed=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
                        Matcher embedMatcher = embedPattern.matcher(contentControl);

                        if (embedMatcher.find()) {
                            String embedId = embedMatcher.group(1);
                            if (debugLogger != null) debugLogger.accept("DEBUG: Encontrada referencia a imagen embebida: " + embedId);
                            if (debugLogger != null) debugLogger.accept("DEBUG: NOTA: Para extraer la imagen real, necesitamos buscar en media/ o word/embeddings/ del archivo .docx");
                        } else {
                            if (debugLogger != null) debugLogger.accept("DEBUG: Picture Content Control pero no hay referencia embebida directa");
                        }
                    }

                    // Buscar específicamente en elementos de imagen embebidos
                    String[] imagePatterns = {
                        // Datos embebidos en pkg:binaryData (formato OpenXML)
                        "<pkg:binaryData[^>]*>([A-Za-z0-9+/=]{100,})</pkg:binaryData>",
                        // Datos en elementos a:blip (imágenes embebidas)
                        "<a:blip[^>]*>.*?([A-Za-z0-9+/=]{100,})",
                        // Datos en elementos w:pict (imágenes antiguas)
                        "<w:pict[^>]*>.*?([A-Za-z0-9+/=]{100,})",
                        // Cualquier dato base64 largo en contexto de imagen
                        "<w:drawing[^>]*>.*?<wp:docPr[^>]*>.*?<a:blip[^>]*>.*?([A-Za-z0-9+/=]{100,})"
                    };

                    for (int j = 0; j < imagePatterns.length; j++) {
                        Pattern imgPattern = Pattern.compile(imagePatterns[j], Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                        Matcher imgMatcher = imgPattern.matcher(contentControl);

                        if (imgMatcher.find()) {
                            String base64Data = imgMatcher.group(1);
                            if (debugLogger != null) debugLogger.accept("DEBUG: ¡ÉXITO! Patrón de imagen " + (j+1) + " encontró base64: " +
                                (base64Data.length() > 50 ? base64Data.substring(0, 50) + "..." : base64Data));
                            return Base64.getDecoder().decode(base64Data);
                        }
                    }

                    if (debugLogger != null) debugLogger.accept("DEBUG: Content control encontrado pero no hay datos base64 directos");
                    // Mostrar parte del content control para debug
                    if (debugLogger != null) debugLogger.accept("DEBUG: Parte del content control '" + tagName + "':\n" +
                        (contentControl.length() > 500 ? contentControl.substring(0, 500) + "..." : contentControl));
                } else {
                    if (debugLogger != null) debugLogger.accept("DEBUG: Tag Word encontrado pero no pude extraer el content control específico");
                }
            }

            // Si ningún patrón funcionó, buscar al menos la presencia del tag
            if (xmlContent.toLowerCase().contains(tagName.toLowerCase())) {
                if (debugLogger != null) debugLogger.accept("DEBUG: El tag '" + tagName + "' SI esta presente en el XML, pero los patrones no coinciden");

                // Mostrar contexto alrededor del tag
                int index = xmlContent.toLowerCase().indexOf(tagName.toLowerCase());
                int start = Math.max(0, index - 100);
                int end = Math.min(xmlContent.length(), index + 200);
                String context = xmlContent.substring(start, end);
                if (debugLogger != null) debugLogger.accept("DEBUG: Contexto alrededor del tag:\n" + context);
            } else {
                if (debugLogger != null) debugLogger.accept("DEBUG: El tag '" + tagName + "' NO está presente en el XML");
            }

        } catch (Exception e) {
            if (debugLogger != null) debugLogger.accept("Error al extraer imagen base64 del XML: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Inserta una imagen en un content control específico de un documento Word
     * 
     * NOTA: Para usar este método, necesitas:
     * 1. Añadir la dependencia de docx4j al pom.xml (descomentada arriba)
     * 2. Descomentar las importaciones de docx4j
     * 3. Descomentar el código del método
     * 
     * @param wordMLPackage Paquete del documento Word
     * @param controlName Nombre del content control donde insertar la imagen
     * @param imageBytes Bytes de la imagen a insertar
     * @throws Exception Si hay error al procesar la imagen o documento
     */
    /*
    public void insertarImagenEnContentControl(WordprocessingMLPackage wordMLPackage, 
        String controlName, byte[] imageBytes) throws Exception {

        var mainDocPart = wordMLPackage.getMainDocumentPart();
        List<Object> allElements = mainDocPart.getContent();
        List<Object> contentControls = getAllElementsOfType(allElements, SdtElement.class);

        for (Object obj : contentControls) {
            SdtElement sdt = (SdtElement) obj;

            if (sdt.getSdtPr() != null && 
                sdt.getSdtPr().getTag() != null &&
                sdt.getSdtPr().getTag().getVal().equals(controlName)) {

                try {
                    BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(
                        wordMLPackage,
                        imageBytes);

                    Inline inline = imagePart.createImageInline("", "", 0, 100, 100, false);

                    // Limpiar el contenido del content control
                    sdt.getSdtContent().getContent().clear();

                    // Crear párrafo con la imagen
                    P paragraph = new P();
                    R run = new R();
                    
                    Drawing drawing = new Drawing();
                    drawing.getAnchorOrInline().add(inline);
                    
                    run.getContent().add(drawing);
                    paragraph.getContent().add(run);
                    sdt.getSdtContent().getContent().add(paragraph);

                    System.out.println("Imagen insertada en Picture Content Control: " + controlName);
                    return;

                } catch (Exception e) {
                    System.err.println("Error al insertar imagen: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Picture Content Control no encontrado: " + controlName);
    }
    */

    /**
     * Obtiene todos los elementos de un tipo específico de una lista de objetos
     * 
     * @param allElements Lista de todos los elementos
     * @param elementType Tipo de elemento a buscar
     * @return Lista de elementos del tipo especificado
     */
    private <T> List<T> getAllElementsOfType(List<Object> allElements, Class<T> elementType) {
        List<T> elements = new ArrayList<>();
        
        for (Object obj : allElements) {
            if (elementType.isInstance(obj)) {
                elements.add(elementType.cast(obj));
            }
        }
        
        return elements;
    }
}
