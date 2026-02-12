package com.pruebas.firma.pequena.app;

import java.io.*;
import java.util.Properties;

/**
 * Gestor de configuración de la aplicación
 * Encargado de persistir y cargar la configuración
 */
public class ConfigurationManager {
    
    private static final String CONFIG_FILE = new File(System.getProperty("user.dir"), ".pruebasFirmaPequeña.properties").getAbsolutePath();
    private static final String LAST_XML_KEY = "lastXmlContent";
    private static final String LAST_TAG_KEY = "lastTagName";
    
    private String ultimoXmlCargado;
    private String ultimoTagCargado;
    
    /**
     * Carga la configuración guardada
     */
    public void cargarConfiguracion() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                ultimoXmlCargado = props.getProperty(LAST_XML_KEY);
                ultimoTagCargado = props.getProperty(LAST_TAG_KEY);
            } catch (IOException e) {
                System.err.println("Error al cargar configuración: " + e.getMessage());
            }
        }
    }
    
    /**
     * Guarda la configuración actual
     */
    public void guardarConfiguracion(String xmlContent, String tagContent) {
        Properties props = new Properties();
        
        props.setProperty(LAST_XML_KEY, xmlContent != null ? xmlContent : "");
        props.setProperty(LAST_TAG_KEY, tagContent != null ? tagContent : "");
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuración de Pruebas Firma Pequeña");
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el último XML cargado
     */
    public String getUltimoXmlCargado() {
        return ultimoXmlCargado;
    }
    
    /**
     * Obtiene el último tag cargado
     */
    public String getUltimoTagCargado() {
        return ultimoTagCargado;
    }
}
