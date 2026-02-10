package com.pruebas.firma.pequena.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

// Descomenta esta importación si añades la dependencia de docx4j
// import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Pantalla principal de la aplicación con interfaz gráfica
 */
public class PantallaPrincipal extends JFrame {
    
    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + ".pruebasFirmaPequeña.properties";
    private static final String LAST_XML_KEY = "lastXmlContent";
    private static final String LAST_TAG_KEY = "lastTagName";
    
    private String ultimoXmlCargado = null;
    private String ultimoTagCargado = null;
    private boolean inicializando = true;
    
    private DocumentSelector documentSelector;
    private JLabel labelArchivo;
    private JTextArea areaInfo;
    private JLabel labelImagen;
    private JButton btnSeleccionar;
    private JButton btnProcesar;
    private JButton btnLimpiar;
    private JTextField txtControlName;
    private JTextArea txtXmlInput;
    private JTextField txtTagName;
    private App app;

    /**
     * Constructor que inicializa la pantalla principal
     */
    public PantallaPrincipal() {
        setTitle("Pruebas Firma Pequeña - Procesador XML");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        
        this.documentSelector = new DocumentSelector();
        this.app = new App();
        this.labelImagen = new JLabel("Imagen extraída aparecerá aquí", SwingConstants.CENTER);
        
        // Cargar configuración guardada ANTES de inicializar componentes
        cargarConfiguracion();
        
        // Inicializar componentes
        inicializarComponentes();
        
        // Configurar listeners ANTES de establecer el texto inicial
        configurarListeners();
        
        // Establecer valores desde configuración o valores por defecto
        if (ultimoXmlCargado != null && !ultimoXmlCargado.trim().isEmpty()) {
            txtXmlInput.setText(ultimoXmlCargado);
        } else {
            txtXmlInput.setText("Pega aquí el contenido XML del documento...");
        }
        if (ultimoTagCargado != null && !ultimoTagCargado.trim().isEmpty()) {
            txtTagName.setText(ultimoTagCargado);
        } else {
            txtTagName.setText("firmaPeq");
        }
        
        // Redirigir salida de consola a la interfaz
        redirigirSalidaConsola();
        
        // Finalizar inicialización - ahora se puede guardar configuración
        inicializando = false;
        
        // Hacer visible
        setVisible(true);
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel superior - Selección de archivo
        JPanel panelSeleccion = crearPanelSeleccion();
        panelPrincipal.add(panelSeleccion, BorderLayout.NORTH);
        
        // Panel central - Información
        JPanel panelInfo = crearPanelInfo();
        panelPrincipal.add(panelInfo, BorderLayout.CENTER);
        
        // Panel inferior - Botones
        JPanel panelBotones = crearPanelBotones();
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        setContentPane(panelPrincipal);
    }

    /**
     * Configura los listeners para los campos de texto
     */
    private void configurarListeners() {
        txtXmlInput.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarBotones(); guardarConfiguracion(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarBotones(); guardarConfiguracion(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarBotones(); guardarConfiguracion(); }
        });
        
        txtTagName.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarBotones(); guardarConfiguracion(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarBotones(); guardarConfiguracion(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarBotones(); guardarConfiguracion(); }
        });
    }

    /**
     * Crea el panel de entrada de datos
     * 
     * @return Panel con componentes de entrada
     */
    private JPanel crearPanelSeleccion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Entrada de Datos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // XML Input
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("XML a procesar:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        txtXmlInput = new JTextArea(8, 50);
        txtXmlInput.setLineWrap(true);
        txtXmlInput.setWrapStyleWord(true);
        txtXmlInput.setFont(new Font("Monospaced", Font.PLAIN, 10));
        // No establecer texto por defecto aquí - se hace después de configurar listeners
        txtXmlInput.setToolTipText("Pega el contenido XML extraído del documento .docx");
        
        JScrollPane xmlScroll = new JScrollPane(txtXmlInput);
        xmlScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(xmlScroll, gbc);
        
        // Tag Name
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Etiqueta a buscar:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtTagName = new JTextField(20);
        // No establecer texto por defecto aquí - se hace después de configurar listeners
        txtTagName.setToolTipText("Nombre de la etiqueta que contiene la imagen base64");
        panel.add(txtTagName, gbc);
        
        // Documento (opcional)
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Documento (opcional):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        labelArchivo = new JLabel("Ninguno");
        labelArchivo.setForeground(Color.BLUE);
        labelArchivo.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(labelArchivo, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        btnSeleccionar = new JButton("Examinar...");
        btnSeleccionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSeleccionarArchivo();
            }
        });
        panel.add(btnSeleccionar, gbc);
        
        return panel;
    }

    /**
     * Crea el panel de información
     * 
     * @return Panel con área de información
     */
    private JPanel crearPanelInfo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Información del Documento"));
        
        areaInfo = new JTextArea();
        areaInfo.setEditable(false);
        areaInfo.setLineWrap(true);
        areaInfo.setWrapStyleWord(true);
        areaInfo.setFont(new Font("Monospaced", Font.PLAIN, 10));
        areaInfo.setText("Introduce XML y especifica la etiqueta a buscar.");
        
        JScrollPane scrollPane = new JScrollPane(areaInfo);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior para la imagen
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Imagen Extraída"));
        imagePanel.setPreferredSize(new Dimension(200, 180));
        
        labelImagen.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        labelImagen.setHorizontalAlignment(SwingConstants.CENTER);
        labelImagen.setVerticalAlignment(SwingConstants.CENTER);
        labelImagen.setPreferredSize(new Dimension(180, 160));
        
        imagePanel.add(labelImagen, BorderLayout.CENTER);
        panel.add(imagePanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Crea el panel de botones
     * 
     * @return Panel con botones de acción
     */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        btnProcesar = new JButton("Procesar Documento");
        btnProcesar.setEnabled(false);
        btnProcesar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onProcesarDocumento();
            }
        });
        
        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setEnabled(false);
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onLimpiar();
            }
        });
        
        JButton btnSalir = new JButton("Salir");
        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        panel.add(btnProcesar);
        panel.add(btnLimpiar);
        panel.add(btnSalir);
        
        return panel;
    }

    /**
     * Manejador del evento de seleccionar archivo
     */
    private void onSeleccionarArchivo() {
        if (documentSelector.seleccionar()) {
            actualizarInterfaz();
            mostrarInformacionArchivo();
        }
    }

    /**
     * Actualiza los elementos de la interfaz después de seleccionar un archivo
     */
    private void actualizarInterfaz() {
        labelArchivo.setText(documentSelector.getFileName());
        actualizarBotones();
    }

    /**
     * Actualiza el estado de los botones según la entrada
     */
    private void actualizarBotones() {
        boolean hayXml = !txtXmlInput.getText().trim().isEmpty();
        boolean hayTag = !txtTagName.getText().trim().isEmpty();
        boolean hayDocumento = documentSelector.hayArchivoSeleccionado();
        
        btnProcesar.setEnabled(hayXml && hayTag);
        btnLimpiar.setEnabled(hayXml || hayDocumento);
    }

    /**
     * Muestra la información del archivo seleccionado
     */
    private void mostrarInformacionArchivo() {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMACIÓN DEL DOCUMENTO ===\n\n");
        info.append("Documento: ").append(documentSelector.getFileName()).append("\n");
        info.append("Ruta: ").append(documentSelector.getFilePath()).append("\n");
        info.append("Tamaño: ").append(formatearTamaño(documentSelector.getFileContent().length)).append("\n\n");
        
        // Extraer y mostrar XML del documento si es un .docx
        if (documentSelector.getFileName().toLowerCase().endsWith(".docx")) {
            String xmlContent = extraerXmlDelDocx(documentSelector.getFileContent());
            if (xmlContent != null && !xmlContent.trim().isEmpty()) {
                String xmlPreview = xmlContent.length() > 300 ? xmlContent.substring(0, 300) + "..." : xmlContent;
                
                // Auto-llenar el campo XML si está vacío (solo texto por defecto)
                String currentXml = txtXmlInput.getText().trim();
                boolean isDefaultText = currentXml.equals("Pega aquí el contenido XML del documento...") || currentXml.isEmpty();
                
                if (xmlContent != null && !xmlContent.trim().isEmpty() && isDefaultText) {
                    txtXmlInput.setText(xmlContent);
                    info.append("✓ XML cargado automáticamente en el campo de entrada.\n\n");
                } else {
                    info.append("ℹ Campo XML ya contiene contenido personalizado - no se sobrescribió.\n\n");
                }
            } else {
                info.append("⚠ No se pudo extraer XML del documento.\n\n");
            }
        }
        
        info.append("Estado: Documento cargado y listo para procesar.\n\n");
        
        areaInfo.setText(info.toString());
    }

    /**
     * Formatea el tamaño en bytes a una unidad legible
     * 
     * @param bytes Número de bytes
     * @return String con el tamaño formateado
     */
    private String formatearTamaño(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] unidades = {"B", "KB", "MB", "GB"};
        int index = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, index), unidades[index]);
    }

    /**
     * Manejador del evento de procesar documento
     */
    private void onProcesarDocumento() {
        String xmlInput = txtXmlInput.getText().trim();
        String tagName = txtTagName.getText().trim();
        
        if (xmlInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, introduce el contenido XML a procesar.",
                "XML requerido", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (tagName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, especifica el nombre de la etiqueta a buscar.",
                "Etiqueta requerida", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        areaInfo.setText("Procesando XML...\n\n");
         
        // Procesar la imagen del XML introducido
        procesarImagenDelDocumento(xmlInput, tagName);
        
        JOptionPane.showMessageDialog(this, 
            "XML procesado exitosamente.\nEtiqueta buscada: " + tagName,
            "Éxito", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Procesa la imagen contenida en el XML proporcionado
     */
    private void procesarImagenDelDocumento(String xmlContent, String tagName) {
        try {
            // Usar directamente el XML proporcionado
            String templateData = xmlContent;
            String TagXML = "firmaPeq";//NO TOCAR
            
            // Mostrar información de debug sobre el XML
            areaInfo.append("=== DEBUG: XML Analizado ===\n");
            
            // Verificar si contiene el tag especificado
            boolean contieneTag = templateData.toLowerCase().contains(TagXML);
            areaInfo.append("¿Contiene <" + TagXML + ">? " + contieneTag + "\n");
            
            // Si contiene firmaPeq pero el usuario especificó otro tag, usar firmaPeq para la extracción
            String tagToUse = tagName;
            if (contieneTag && !tagName.equalsIgnoreCase(TagXML)) {
                areaInfo.append("⚠ El XML contiene <" + TagXML + "> pero se buscó <" + tagName + ">. Usando <" + TagXML + "> para la extracción.\n");
                tagToUse = TagXML;
            }
            
            // Extraer imagen base64 del XML usando el tag especificado
            areaInfo.append("=== INICIANDO EXTRACCIÓN ===\n");
            Consumer<String> debugLogger = msg -> {
                areaInfo.append(msg + "\n");
                // Forzar actualización de la UI
                areaInfo.revalidate();
                areaInfo.repaint();
            };
            byte[] imageBytes = app.extraerImagenBase64DelXml(templateData, tagToUse, debugLogger);
            areaInfo.append("=== FIN EXTRACCIÓN ===\n");
            
            if (imageBytes != null && imageBytes.length > 0) {
                areaInfo.append("✓ Imagen extraída del XML (" + formatearTamaño(imageBytes.length) + ")\n");
                mostrarImagen(imageBytes);
                
                // Intentar insertar la imagen en el content control (si hay archivo seleccionado)
                if (documentSelector.hayArchivoSeleccionado()) {
                    areaInfo.append("⚠ Para insertar la imagen en el documento, descomenta la dependencia de docx4j en pom.xml\n");
                } else {
                    areaInfo.append("ℹ Archivo no seleccionado - solo se extrajo la imagen del XML\n");
                }
            } else {
                areaInfo.append("ℹ No se encontró imagen en el campo <" + tagName + "> del XML\n");
                areaInfo.append("=== FIN DEBUG ===\n\n");
                labelImagen.setIcon(null);
                labelImagen.setText("No se encontró imagen");
            }
            
        } catch (Exception e) {
            areaInfo.append("✗ Error al procesar la imagen: " + e.getMessage() + "\n");
            System.err.println("Error procesando imagen del XML: " + e.getMessage());
            labelImagen.setText("Error al procesar imagen");
            labelImagen.setIcon(null);
        }
    }

    /**
     * Extrae el contenido XML de un documento Word (.docx)
     * 
     * @return El contenido XML del documento o null si hay error
     */
    private String extraerXmlDelDocumentoWord() {
        String fileName = documentSelector.getFileName();
        if (fileName == null || !fileName.toLowerCase().endsWith(".docx")) {
            // Si no es un archivo .docx, intentar procesarlo como XML plano
            try {
                return new String(documentSelector.getFileContent(), "UTF-8");
            } catch (Exception e) {
                areaInfo.append("✗ Error al leer el archivo como texto: " + e.getMessage() + "\n");
                return null;
            }
        }

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(documentSelector.getFileContent()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    // Encontramos el archivo XML principal
                    byte[] buffer = new byte[1024];
                    StringBuilder sb = new StringBuilder();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        sb.append(new String(buffer, 0, len, "UTF-8"));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            areaInfo.append("✗ Error al extraer XML del documento Word: " + e.getMessage() + "\n");
            return null;
        }
        
        areaInfo.append("✗ No se encontró el archivo document.xml en el documento Word\n");
        return null;
    }

    /**
     * Manejador del evento de limpiar
     */
    private void onLimpiar() {
        documentSelector.limpiar();
        labelArchivo.setText("Ninguno");
        txtXmlInput.setText("Pega aquí el contenido XML del documento...");
        txtTagName.setText("firmaPeq");
        areaInfo.setText("Introduce XML y especifica la etiqueta a buscar.");
        labelImagen.setIcon(null);
        labelImagen.setText("Imagen extraída aparecerá aquí");
        actualizarBotones();
    }

    /**
     * Redirige la salida de System.out y System.err a la interfaz gráfica
     */
    private void redirigirSalidaConsola() {
        PrintStream textAreaPrintStream = new TextAreaPrintStream(System.out, areaInfo);
        System.setOut(textAreaPrintStream);
        System.setErr(textAreaPrintStream);
    }

    /**
     * PrintStream personalizado que escribe tanto en la consola como en el área de texto
     */
    private static class TextAreaPrintStream extends PrintStream {
        private final PrintStream originalStream;
        private final JTextArea textArea;

        public TextAreaPrintStream(PrintStream originalStream, JTextArea textArea) {
            super(originalStream);
            this.originalStream = originalStream;
            this.textArea = textArea;
        }

        @Override
        public void print(String s) {
            originalStream.print(s);
            SwingUtilities.invokeLater(() -> {
                textArea.append(s);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }

        @Override
        public void println(String s) {
            originalStream.println(s);
            SwingUtilities.invokeLater(() -> {
                textArea.append(s + "\n");
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }

        @Override
        public void println() {
            originalStream.println();
            SwingUtilities.invokeLater(() -> {
                textArea.append("\n");
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }

    /**
     * Extrae el contenido XML del archivo .docx
     * 
     * @param fileContent Contenido del archivo en bytes
     * @return Contenido XML del documento o null si hay error
     */
    private String extraerXmlDelDocx(byte[] fileContent) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(fileContent);
            ZipInputStream zis = new ZipInputStream(bais);
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("word/document.xml")) {
                    // Leer el contenido del archivo document.xml
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    
                    String xmlContent = baos.toString("UTF-8");
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

    /**
     * Carga la configuración guardada (último XML y etiqueta)
     */
    private void cargarConfiguracion() {
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
     * Guarda la configuración actual (XML y etiqueta)
     */
    private void guardarConfiguracion() {
        if (inicializando) {
            return; // No guardar durante la inicialización
        }
        
        Properties props = new Properties();
        String xmlContent = txtXmlInput.getText();
        String tagContent = txtTagName.getText();
        
        props.setProperty(LAST_XML_KEY, xmlContent != null ? xmlContent : "");
        props.setProperty(LAST_TAG_KEY, tagContent != null ? tagContent : "");
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuración de Pruebas Firma Pequeña");
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }

    /**
     * Establece una imagen en el label a partir de datos en bytes
     */
    private void mostrarImagen(byte[] imageBytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img != null) {
                // Escalar la imagen si es muy grande
                int maxWidth = 150;
                int maxHeight = 150;
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
     * Punto de entrada de la aplicación
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PantallaPrincipal();
            }
        });
    }
}
