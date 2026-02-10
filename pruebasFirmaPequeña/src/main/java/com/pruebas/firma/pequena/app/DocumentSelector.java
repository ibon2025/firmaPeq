package com.pruebas.firma.pequena.app;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/**
 * Clase para seleccionar documentos desde el sistema de archivos local
 */
public class DocumentSelector {
    
    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + ".pruebasFirmaPequeña.properties";
    private static final String LAST_DIR_KEY = "lastDirectory";
    
    private JFileChooser fileChooser;
    private File selectedFile;
    private byte[] fileContent;
    private File lastDirectory;

    /**
     * Constructor que inicializa el selector de archivos
     */
    public DocumentSelector() {
        this.lastDirectory = loadLastDirectory();
        this.fileChooser = new JFileChooser();
        
        // Configurar filtros de archivo
        FileNameExtensionFilter wordFilter = new FileNameExtensionFilter(
            "Documentos Word (*.docx, *.doc)", "docx", "doc");
        FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(
            "Documentos PDF (*.pdf)", "pdf");
        FileNameExtensionFilter allFilter = new FileNameExtensionFilter(
            "Todos los archivos", "*");
        
        fileChooser.setFileFilter(wordFilter);
        fileChooser.addChoosableFileFilter(pdfFilter);
        fileChooser.addChoosableFileFilter(allFilter);
        
        // Configuración inicial
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(lastDirectory);
        
        // Establecer vista de sistema de archivos personalizada para ordenar por fecha
        fileChooser.setFileSystemView(new CustomFileSystemView());
    }

    /**
     * Carga el último directorio utilizado desde el archivo de configuración
     * 
     * @return El último directorio o el directorio home del usuario si no existe
     */
    private File loadLastDirectory() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                String lastDirPath = props.getProperty(LAST_DIR_KEY);
                if (lastDirPath != null) {
                    File lastDir = new File(lastDirPath);
                    if (lastDir.exists() && lastDir.isDirectory()) {
                        return lastDir;
                    }
                }
            } catch (IOException e) {
                // Ignorar errores de carga, usar valor por defecto
                System.err.println("Error al cargar configuración: " + e.getMessage());
            }
        }
        return new File(System.getProperty("user.home"));
    }

    /**
     * Guarda el último directorio utilizado en el archivo de configuración
     */
    private void saveLastDirectory() {
        Properties props = new Properties();
        props.setProperty(LAST_DIR_KEY, lastDirectory.getAbsolutePath());
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuración de Pruebas Firma Pequeña");
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }

    /**
     * Abre el diálogo de selección de archivo
     * 
     * @return true si se seleccionó un archivo, false si se canceló
     */
    public boolean seleccionar() {
        int returnValue = fileChooser.showOpenDialog(null);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.getParentFile() != null) {
                this.lastDirectory = selectedFile.getParentFile();
                saveLastDirectory();
            }
            cargarContenidoArchivo();
            return true;
        }
        
        return false;
    }

    /**
     * Carga el contenido del archivo seleccionado en memoria
     */
    private void cargarContenidoArchivo() {
        try {
            Path ruta = selectedFile.toPath();
            this.fileContent = Files.readAllBytes(ruta);
            System.out.println("Archivo cargado exitosamente: " + selectedFile.getAbsolutePath());
            System.out.println("Tamaño: " + fileContent.length + " bytes");
        } catch (Exception e) {
            System.err.println("Error al cargar el archivo: " + e.getMessage());
            e.printStackTrace();
            this.fileContent = null;
        }
    }

    /**
     * Obtiene el archivo seleccionado
     * 
     * @return El objeto File del documento seleccionado
     */
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Obtiene el contenido del archivo en bytes
     * 
     * @return Array de bytes del contenido del archivo
     */
    public byte[] getFileContent() {
        return fileContent;
    }

    /**
     * Obtiene la ruta absoluta del archivo seleccionado
     * 
     * @return Ruta absoluta like string
     */
    public String getFilePath() {
        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Obtiene el nombre del archivo seleccionado
     * 
     * @return Nombre del archivo
     */
    public String getFileName() {
        if (selectedFile != null) {
            return selectedFile.getName();
        }
        return null;
    }

    /**
     * Verifica si hay un archivo seleccionado
     * 
     * @return true si hay archivo seleccionado, false en caso contrario
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

/**
 * Vista personalizada del sistema de archivos que ordena los archivos por fecha de modificación,
 * de más nuevo a más viejo
 */
class CustomFileSystemView extends FileSystemView {
    
    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        File[] files = super.getFiles(dir, useFileHiding);
        // Ordenar por fecha de modificación descendente (más nuevo primero)
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return files;
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        File newFolder = new File(containingDir, "Nueva Carpeta");
        if (newFolder.mkdir()) {
            return newFolder;
        } else {
            throw new IOException("No se puede crear la carpeta");
        }
    }
}
