package com.pruebas.firma.pequena.app;

import javax.swing.*;
import java.io.File;
import java.util.function.Consumer;

/**
 * Manejadores de eventos de la interfaz
 * Responsable de procesar las acciones del usuario
 */
public class EventHandlers {
    
    private DocumentSelector documentSelector;
    private App app;
    private ImageProcessor imageProcessor;
    private ConfigurationManager configManager;
    private UIComponentBuilder.Components components;
    private byte[] ultimaImagenExtraida;
    private String ultimoContentControlBuscado;
    
    public EventHandlers(DocumentSelector documentSelector, App app, ConfigurationManager configManager) {
        this.documentSelector = documentSelector;
        this.app = app;
        this.imageProcessor = new ImageProcessor();
        this.configManager = configManager;
    }
    
    public void setComponents(UIComponentBuilder.Components components) {
        this.components = components;
    }
    
    /**
     * Manejador del evento de seleccionar archivo
     */
    public void onSeleccionarArchivo() {
        if (documentSelector.seleccionar()) {
            actualizarInterfaz();
            mostrarInformacionArchivo();
        }
    }

    /**
     * Actualiza los elementos de la interfaz después de seleccionar un archivo
     */
    private void actualizarInterfaz() {
        components.labelArchivo.setText(documentSelector.getFileName());
        actualizarBotones();
    }

    /**
     * Actualiza el estado de los botones según la entrada
     */
    public void actualizarBotones() {
        boolean hayXml = !components.txtXmlInput.getText().trim().isEmpty();
        boolean hayTag = !components.txtTagName.getText().trim().isEmpty();
        boolean hayDocumento = documentSelector.hayArchivoSeleccionado();
        
        components.btnProcesar.setEnabled(hayXml && hayTag);
        components.btnLimpiar.setEnabled(hayXml || hayDocumento);
    }

    /**
     * Muestra la información del archivo seleccionado
     */
    private void mostrarInformacionArchivo() {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMACIÓN DEL DOCUMENTO ===\n\n");
        info.append("Documento: ").append(documentSelector.getFileName()).append("\n");
        info.append("Ruta: ").append(documentSelector.getFilePath()).append("\n");
        info.append("Tamaño: ").append(ImageProcessor.formatearTamaño(documentSelector.getFileContent().length)).append("\n\n");
        
        // Extraer y mostrar XML del documento si es un .docx
        if (documentSelector.getFileName().toLowerCase().endsWith(".docx")) {
            String xmlContent = XMLTreeBuilder.extraerXmlDelDocx(documentSelector.getFileContent());
            if (xmlContent != null && !xmlContent.trim().isEmpty()) {
                String xmlPreview = xmlContent.length() > 300 ? xmlContent.substring(0, 300) + "..." : xmlContent;
                
                // Auto-llenar el campo XML si está vacío (solo texto por defecto)
                String currentXml = components.txtXmlInput.getText().trim();
                boolean isDefaultText = currentXml.equals("Pega aquí el contenido XML del documento...") || currentXml.isEmpty();
                
                if (xmlContent != null && !xmlContent.trim().isEmpty() && isDefaultText) {
                    components.txtXmlInput.setText(xmlContent);
                    info.append("✓ XML cargado automáticamente en el campo de entrada.\n\n");
                } else {
                    info.append("ℹ Campo XML ya contiene contenido personalizado - no se sobrescribió.\n\n");
                }
                
                // Buscar content controls especificados
                String controlName = components.txtControlName.getText().trim();
                if (!controlName.isEmpty()) {
                    boolean encontrado = XMLTreeBuilder.buscarContentControl(xmlContent, controlName);
                    if (encontrado) {
                        info.append("✓ Se encontró el Content Control: <").append(controlName).append(">.\n\n");
                    } else {
                        info.append("✗ NO se encontró el Content Control: <").append(controlName).append(">\n");
                        info.append("  Disponibles en el documento.\n\n");
                    }
                }
            } else {
                info.append("⚠ No se pudo extraer XML del documento.\n\n");
            }
        }
        
        info.append("Estado: Documento cargado y listo para procesar.\n\n");
        
        components.areaInfo.setText(info.toString());
    }

    /**
     * Manejador del evento de procesar documento
     */
    public void onProcesarDocumento() {
        String xmlInput = components.txtXmlInput.getText().trim();
        String tagName = components.txtTagName.getText().trim();
        
        if (xmlInput.isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "Por favor, introduce el contenido XML a procesar.",
                "XML requerido", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (tagName.isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "Por favor, especifica el nombre de la etiqueta a buscar.",
                "Etiqueta requerida", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        components.areaInfo.setText("Procesando XML...\n\n");
        procesarImagenDelDocumento(xmlInput, tagName);
        
        JOptionPane.showMessageDialog(null, 
            "XML procesado exitosamente.\nEtiqueta buscada: " + tagName,
            "Éxito", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Procesa la imagen contenida en el XML proporcionado
     */
    private void procesarImagenDelDocumento(String xmlContent, String tagName) {
        try {
            String templateData = xmlContent;
            
            components.areaInfo.append("=== DEBUG: XML Analizado ===\n");
            
            boolean contieneTag = templateData.toLowerCase().contains("<" + tagName.toLowerCase() + ">");
            components.areaInfo.append("¿Contiene <" + tagName + ">? " + contieneTag + "\n");
            
            String tagToUse = tagName;
            
            components.areaInfo.append("=== INICIANDO EXTRACCIÓN ===\n");
            Consumer<String> debugLogger = msg -> {
                components.areaInfo.append(msg + "\n");
                components.areaInfo.revalidate();
                components.areaInfo.repaint();
            };
            byte[] imageBytes = app.extraerImagenBase64DelXml(templateData, tagToUse, debugLogger);
            components.areaInfo.append("=== FIN EXTRACCIÓN ===\n");
            
            if (imageBytes != null && imageBytes.length > 0) {
                ultimaImagenExtraida = imageBytes;
                components.btnDescargarImagen.setEnabled(true);
                
                components.areaInfo.append("✓ Imagen extraída del XML (" + ImageProcessor.formatearTamaño(imageBytes.length) + ")\n");
                imageProcessor.mostrarImagen(imageBytes, components.labelImagen, components.areaInfo);
                
                if (documentSelector.hayArchivoSeleccionado()) {
                    String wordXmlContent = XMLTreeBuilder.extraerXmlDelDocx(documentSelector.getFileContent());
                    
                    if (wordXmlContent != null) {
                        XMLTreeBuilder.cargarArbolComponentes(wordXmlContent, components.arbolComponentes);
                    }
                    
                    String controlName = components.txtControlName.getText().trim();
                    if (!controlName.isEmpty()) {
                        if (wordXmlContent != null) {
                            boolean encontrado = XMLTreeBuilder.buscarContentControl(wordXmlContent, controlName);
                            if (encontrado) {
                                components.areaInfo.append("✓ Se encontró el Content Control <" + controlName + "> en el documento.\n");
                                components.areaInfo.append("  ✓ Imagen lista para insertar en Word.\n");
                                ultimoContentControlBuscado = controlName;
                                components.btnDescargar.setEnabled(true);
                            } else {
                                components.areaInfo.append("✗ NO se encontró el Content Control <" + controlName + "> en el documento.\n");
                                components.areaInfo.append("  ✗ Especifica un Content Control válido.\n");
                                components.btnDescargar.setEnabled(false);
                            }
                        } else {
                            components.areaInfo.append("✗ No se pudo leer el XML del documento Word.\n");
                            components.btnDescargar.setEnabled(false);
                        }
                    } else {
                        components.areaInfo.append("ℹ No se especificó Content Control - especifica uno para insertar en Word.\n");
                        components.btnDescargar.setEnabled(false);
                    }
                } else {
                    components.areaInfo.append("ℹ Archivo Word no seleccionado - solo se extrajo la imagen del XML\n");
                }
            } else {
                components.areaInfo.append("ℹ No se encontró imagen en el campo <" + tagName + "> del XML\n");
                components.areaInfo.append("=== FIN DEBUG ===\n\n");
                components.labelImagen.setIcon(null);
                components.labelImagen.setText("No se encontró imagen");
                components.btnDescargar.setEnabled(false);
                components.btnDescargarImagen.setEnabled(false);
            }
            
        } catch (Exception e) {
            components.areaInfo.append("✗ Error al procesar la imagen: " + e.getMessage() + "\n");
            System.err.println("Error procesando imagen del XML: " + e.getMessage());
            components.labelImagen.setText("Error al procesar imagen");
            components.labelImagen.setIcon(null);
            components.btnDescargar.setEnabled(false);
            components.btnDescargarImagen.setEnabled(false);
        }
    }

    /**
     * Manejador del evento de limpiar
     */
    public void onLimpiar() {
        documentSelector.limpiar();
        components.labelArchivo.setText("Ninguno");
        components.txtXmlInput.setText("Pega aquí el contenido XML del documento...");
        components.txtTagName.setText("firmaPeq");
        components.txtControlName.setText("imagenFirmaPeq");
        components.areaInfo.setText("Introduce XML y especifica la etiqueta a buscar.");
        components.labelImagen.setIcon(null);
        components.labelImagen.setText("Imagen extraída aparecerá aquí");
        actualizarBotones();
    }

    /**
     * Manejador del evento de descargar documento con imagen insertada
     */
    public void onDescargarDocumento() {
        if (ultimaImagenExtraida == null || ultimaImagenExtraida.length == 0) {
            JOptionPane.showMessageDialog(null, 
                "Error: No hay imagen para insertar.",
                "Imagen no disponible", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (ultimoContentControlBuscado == null || ultimoContentControlBuscado.isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "Error: No hay Content Control seleccionado.",
                "Content Control no disponible", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!documentSelector.hayArchivoSeleccionado()) {
            JOptionPane.showMessageDialog(null, 
                "Error: No hay documento Word seleccionado.",
                "Documento no disponible", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            byte[] documentoModificado = DocumentProcessor.crearDocumentoConImagen(
                documentSelector.getFileContent(), 
                ultimaImagenExtraida, 
                ultimoContentControlBuscado,
                imageProcessor,
                components.areaInfo
            );
            
            if (documentoModificado == null || documentoModificado.length == 0) {
                JOptionPane.showMessageDialog(null, 
                    "Error al crear el documento modificado.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar documento modificado");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Documentos Word", "docx"));
            
            String originalName = documentSelector.getFileName();
            if (originalName != null) {
                String suggestedName = originalName.replace(".docx", "_con_imagen.docx");
                fileChooser.setSelectedFile(new File(suggestedName));
            } else {
                fileChooser.setSelectedFile(new File("documento_con_imagen.docx"));
            }
            
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    fos.write(documentoModificado);
                    fos.flush();
                    
                    JOptionPane.showMessageDialog(null, 
                        "Documento guardado exitosamente en:\n" + file.getAbsolutePath(),
                        "Éxito", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    components.areaInfo.append("✓ Documento con imagen guardado en: " + file.getAbsolutePath() + "\n");
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error al descargar documento: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            components.areaInfo.append("✗ Error al descargar documento: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    /**
     * Manejador del evento de descargar imagen
     */
    public void onDescargarImagen() {
        if (ultimaImagenExtraida == null || ultimaImagenExtraida.length == 0) {
            JOptionPane.showMessageDialog(null, 
                "Error: No hay imagen para descargar.",
                "Imagen no disponible", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar imagen");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes PNG", "png"));
            
            fileChooser.setSelectedFile(new File("firma.png"));
            
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                String filePath = file.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".png")) {
                    filePath += ".png";
                    file = new File(filePath);
                }
                
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    fos.write(ultimaImagenExtraida);
                    fos.flush();
                    
                    JOptionPane.showMessageDialog(null, 
                        "Imagen guardada exitosamente en:\n" + file.getAbsolutePath(),
                        "Éxito", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    components.areaInfo.append("✓ Imagen descargada en: " + file.getAbsolutePath() + 
                                  " (" + ImageProcessor.formatearTamaño(ultimaImagenExtraida.length) + ")\n");
                }
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error al descargar imagen: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            components.areaInfo.append("✗ Error al descargar imagen: " + e.getMessage() + "\n");
            System.err.println("Error descargando imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
