package com.pruebas.firma.pequena.app;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Clase para seleccionar imágenes desde el sistema de archivos local
 */
public class ImageSelector {
    
    private JFileChooser fileChooser;
    private File selectedFile;
    private byte[] fileContent;

    /**
     * Constructor que inicializa el selector de imágenes
     */
    public ImageSelector() {
        this.fileChooser = new JFileChooser();
        
        // Configurar filtros de archivo para imágenes
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
            "Imágenes (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", 
            "png", "jpg", "jpeg", "gif", "bmp");
        FileNameExtensionFilter allFilter = new FileNameExtensionFilter(
            "Todos los archivos", "*");
        
        fileChooser.setFileFilter(imageFilter);
        fileChooser.addChoosableFileFilter(allFilter);
        
        // Configuración inicial
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }

    /**
     * Abre el diálogo de selección de imagen
     * 
     * @return true si se seleccionó una imagen, false si se canceló
     */
    public boolean seleccionar() {
        int returnValue = fileChooser.showOpenDialog(null);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.selectedFile = fileChooser.getSelectedFile();
            cargarContenidoArchivo();
            return true;
        }
        
        return false;
    }

    /**
     * Carga el contenido de la imagen seleccionada en memoria
     */
    private void cargarContenidoArchivo() {
        try {
            Path ruta = selectedFile.toPath();
            this.fileContent = Files.readAllBytes(ruta);
            System.out.println("Imagen cargada exitosamente: " + selectedFile.getAbsolutePath());
            System.out.println("Tamaño: " + fileContent.length + " bytes");
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen: " + e.getMessage());
            e.printStackTrace();
            this.fileContent = null;
        }
    }

    /**
     * Obtiene la imagen seleccionada
     * 
     * @return El objeto File de la imagen seleccionada
     */
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Obtiene el contenido de la imagen en bytes
     * 
     * @return Array de bytes del contenido de la imagen
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * Obtiene la ruta absoluta de la imagen seleccionada
     * 
     * @return Ruta absoluta como string
     */
    public String getFilePath() {
        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Obtiene el nombre de la imagen seleccionada
     * 
     * @return Nombre del archivo de imagen
     */
    public String getFileName() {
        if (selectedFile != null) {
            return selectedFile.getName();
        }
        return null;
    }

    /**
     * Verifica si hay una imagen seleccionada
     * 
     * @return true si hay imagen seleccionada, false en caso contrario
     */
    public boolean hayArchivoSeleccionado() {
        return selectedFile != null && fileContent != null;
    }

    /**
     * Limpia la selección
     */
    public void limpiar() {
        this.selectedFile = null;
        this.fileContent = null;
    }
}
