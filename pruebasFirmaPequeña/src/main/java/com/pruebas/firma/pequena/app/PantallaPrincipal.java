package com.pruebas.firma.pequena.app;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

/**
 * Pantalla principal de la aplicación con interfaz gráfica
 * Actúa como coordinadora central que delega responsabilidades a componentes especializados
 */
public class PantallaPrincipal extends JFrame {
    
    private boolean inicializando = true;
    
    private DocumentSelector documentSelector;
    private ConfigurationManager configManager;
    private EventHandlers eventHandlers;
    private UIComponentBuilder.Components components;
    private App app;

    /**
     * Constructor que inicializa la pantalla principal
     */
    public PantallaPrincipal() {
        setTitle("Pruebas Firma Pequeña - Procesador XML");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 640);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Inicializar componentes y gestores
        this.documentSelector = new DocumentSelector();
        this.configManager = new ConfigurationManager();
        this.app = new App();
        this.components = new UIComponentBuilder.Components();
        
        // Cargar configuración
        configManager.cargarConfiguracion();
        
        // Inicializar interfaz
        inicializarComponentes();
        configurarListeners();
        cargarValoresPorDefecto();
        
        redirigirSalidaConsola();
        
        inicializando = false;
        setVisible(true);
    }

    /**
     * Inicializa la estructura de componentes de la interfaz
     */
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel superior - Selección de archivo
        JPanel panelSeleccion = UIComponentBuilder.crearPanelSeleccion(components);
        panelPrincipal.add(panelSeleccion, BorderLayout.NORTH);
        
        // Panel central - Árbol a la izquierda y el resto a la derecha
        JPanel panelCentral = UIComponentBuilder.crearPanelCentral(components);
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        
        // Panel inferior - Botones
        JPanel panelBotones = UIComponentBuilder.crearPanelBotones(components);
        agregarListenerAlBotonSalir(panelBotones);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        setContentPane(panelPrincipal);
    }

    /**
     * Carga los valores guardados por defecto o anteriores
     */
    private void cargarValoresPorDefecto() {
        String xmlCargado = configManager.getUltimoXmlCargado();
        if (xmlCargado != null && !xmlCargado.trim().isEmpty()) {
            components.txtXmlInput.setText(xmlCargado);
        } else {
            components.txtXmlInput.setText("Pega aquí el contenido XML del documento...");
        }
        
        String tagCargado = configManager.getUltimoTagCargado();
        if (tagCargado != null && !tagCargado.trim().isEmpty()) {
            components.txtTagName.setText(tagCargado);
        } else {
            components.txtTagName.setText("firmaPeq");
        }
        
        components.txtControlName.setText("imagenFirmaPeq");
    }

    /**
     * Configura los listeners para campos de texto y botones
     */
    private void configurarListeners() {
        eventHandlers = new EventHandlers(documentSelector, app, configManager);
        eventHandlers.setComponents(components);
        
        // Listeners para cambios en campos de texto
        components.txtXmlInput.getDocument().addDocumentListener(
            new SimpleDocumentListener(() -> {
                eventHandlers.actualizarBotones();
                guardarConfiguracion();
            }));
        
        components.txtTagName.getDocument().addDocumentListener(
            new SimpleDocumentListener(() -> {
                eventHandlers.actualizarBotones();
                guardarConfiguracion();
            }));
        
        components.txtControlName.getDocument().addDocumentListener(
            new SimpleDocumentListener(this::guardarConfiguracion));
        
        // Listeners para botones de acción
        components.btnSeleccionar.addActionListener(e -> eventHandlers.onSeleccionarArchivo());
        components.btnProcesar.addActionListener(e -> eventHandlers.onProcesarDocumento());
        components.btnLimpiar.addActionListener(e -> eventHandlers.onLimpiar());
        components.btnDescargar.addActionListener(e -> eventHandlers.onDescargarDocumento());
        components.btnDescargarImagen.addActionListener(e -> eventHandlers.onDescargarImagen());
    }

    /**
     * Agrega el listener al botón Salir
     */
    private void agregarListenerAlBotonSalir(JPanel panelBotones) {
        for (Component comp : panelBotones.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if ("Salir".equals(btn.getText())) {
                    btn.addActionListener(e -> System.exit(0));
                    break;
                }
            }
        }
    }

    /**
     * Guarda la configuración actual (último XML y etiqueta usados)
     */
    private void guardarConfiguracion() {
        if (inicializando) {
            return;
        }
        
        String xmlContent = components.txtXmlInput.getText();
        String tagContent = components.txtTagName.getText();
        configManager.guardarConfiguracion(xmlContent, tagContent);
    }

    /**
     * Redirige la salida de System.out y System.err a la interfaz gráfica
     */
    private void redirigirSalidaConsola() {
        PrintStream textAreaPrintStream = new TextAreaPrintStream(System.out, components.areaInfo);
        System.setOut(textAreaPrintStream);
        System.setErr(textAreaPrintStream);
    }

    /**
     * Listener simple para cambios en documentos de texto
     */
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private Runnable action;

        public SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }
    }

    /**
     * PrintStream personalizado que escribe tanto en consola como en el área de texto
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
     * Punto de entrada de la aplicación
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PantallaPrincipal());
    }
}
