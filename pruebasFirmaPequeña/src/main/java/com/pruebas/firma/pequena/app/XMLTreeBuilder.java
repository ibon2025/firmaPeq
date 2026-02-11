package com.pruebas.firma.pequena.app;

import javax.swing.*;

/**
 * Constructor del árbol de componentes del documento XML
 * Responsable de parsear el XML y construir la representación visual
 */
public class XMLTreeBuilder {
    
    /**
     * Carga el árbol de componentes del XML del documento Word
     */
    public static void cargarArbolComponentes(String xmlContent, JTree arbolComponentes) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            javax.swing.tree.DefaultMutableTreeNode root = new javax.swing.tree.DefaultMutableTreeNode("Error: XML vacío");
            arbolComponentes.setModel(new javax.swing.tree.DefaultTreeModel(root));
            return;
        }
        
        try {
            javax.swing.tree.DefaultMutableTreeNode root = new javax.swing.tree.DefaultMutableTreeNode("document.xml");
            
            // Extraer elementos principales
            if (xmlContent.contains("w:body")) {
                javax.swing.tree.DefaultMutableTreeNode body = new javax.swing.tree.DefaultMutableTreeNode("Body");
                root.add(body);
                
                // Contar párrafos
                int paragraphs = xmlContent.split("<w:p>").length - 1;
                body.add(new javax.swing.tree.DefaultMutableTreeNode("Párrafos: " + paragraphs));
                
                // Contar tablas
                int tables = xmlContent.split("<w:tbl>").length - 1;
                body.add(new javax.swing.tree.DefaultMutableTreeNode("Tablas: " + tables));
                
                // Buscar content controls (SdtBlock)
                javax.swing.tree.DefaultMutableTreeNode controls = new javax.swing.tree.DefaultMutableTreeNode("Content Controls");
                int controlCount = 0;
                int start = 0;
                while ((start = xmlContent.indexOf("w:tag", start)) != -1) {
                    int valStart = xmlContent.indexOf("w:val=\"", start) + 7;
                    int valEnd = xmlContent.indexOf("\"", valStart);
                    if (valStart > 6 && valEnd > valStart) {
                        String controlName = xmlContent.substring(valStart, valEnd);
                        controls.add(new javax.swing.tree.DefaultMutableTreeNode(controlName));
                        controlCount++;
                        start = valEnd;
                    } else {
                        break;
                    }
                }
                if (controlCount > 0) {
                    body.add(controls);
                }
            }
            
            arbolComponentes.setModel(new javax.swing.tree.DefaultTreeModel(root));
            
            // Expandir nodos
            for (int i = 0; i < arbolComponentes.getRowCount(); i++) {
                arbolComponentes.expandRow(i);
            }
        } catch (Exception e) {
            javax.swing.tree.DefaultMutableTreeNode root = new javax.swing.tree.DefaultMutableTreeNode("Error al parsear XML");
            arbolComponentes.setModel(new javax.swing.tree.DefaultTreeModel(root));
        }
    }
    
    /**
     * Busca si existe un Content Control con el nombre especificado en el XML
     * Usa la misma lógica que cargarArbolComponentes() para consistencia
     */
    public static boolean buscarContentControl(String xmlContent, String controlName) {
        if (xmlContent == null || xmlContent.isEmpty() || controlName == null || controlName.isEmpty()) {
            return false;
        }
        
        int start = 0;
        while ((start = xmlContent.indexOf("w:tag", start)) != -1) {
            int valStart = xmlContent.indexOf("w:val=\"", start) + 7;
            int valEnd = xmlContent.indexOf("\"", valStart);
            if (valStart > 6 && valEnd > valStart) {
                String extractedControlName = xmlContent.substring(valStart, valEnd);
                if (extractedControlName.equals(controlName)) {
                    return true;
                }
                start = valEnd;
            } else {
                break;
            }
        }
        return false;
    }
    
    /**
     * Extrae el contenido XML de un documento Word (.docx)
     */
    public static String extraerXmlDelDocx(byte[] fileContent) {
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(fileContent);
            java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(bais);
            java.util.zip.ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("word/document.xml")) {
                    // Leer el contenido del archivo document.xml
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    
                    // Usar UTF-8 explícitamente para la decodificación
                    String xmlContent = new String(baos.toByteArray(), "UTF-8");
                    zis.closeEntry();
                    zis.close();
                    return xmlContent;
                }
                zis.closeEntry();
            }
            
            zis.close();
        } catch (Exception e) {
            System.err.println("Error al extraer XML del documento .docx: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}
