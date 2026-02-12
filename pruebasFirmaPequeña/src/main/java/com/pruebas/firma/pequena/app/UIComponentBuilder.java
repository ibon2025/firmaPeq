package com.pruebas.firma.pequena.app;

import javax.swing.*;
import java.awt.*;

/**
 * Constructor de componentes de la interfaz gráfica
 * Responsable de crear y configurar todos los componentes Swing
 */
public class UIComponentBuilder {
    
    public static class Components {
        public JLabel labelImagen;
        public JLabel labelArchivo;
        public JTextArea areaInfo;
        public JTextArea txtXmlInput;
        public JTextField txtTagName;
        public JTextField txtControlName;
        public JTree arbolComponentes;
        public JScrollPane scrollArbol;
        public JButton btnSeleccionar;
        public JButton btnProcesar;
        public JButton btnLimpiar;
        public JButton btnDescargar;
        public JButton btnDescargarImagen;
    }
    
    /**
     * Crea el panel central con split entre árbol y contenido
     */
    public static JPanel crearPanelCentral(Components components) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Árbol de componentes a la izquierda
        components.arbolComponentes = new JTree(new javax.swing.tree.DefaultMutableTreeNode("Estructura Word"));
        components.arbolComponentes.setPreferredSize(new Dimension(250, 400));
        components.scrollArbol = new JScrollPane(components.arbolComponentes);
        components.scrollArbol.setBorder(BorderFactory.createTitledBorder("Estructura del Documento"));
        
        // Panel derecho con información e imagen
        JPanel panelDerecha = new JPanel(new BorderLayout());
        
        // Panel de información
        components.areaInfo = new JTextArea();
        components.areaInfo.setEditable(false);
        components.areaInfo.setLineWrap(true);
        components.areaInfo.setWrapStyleWord(true);
        components.areaInfo.setFont(new Font("Monospaced", Font.PLAIN, 10));
        components.areaInfo.setText("Introduce XML y especifica la etiqueta a buscar.");
        
        JScrollPane scrollPane = new JScrollPane(components.areaInfo);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Información del Documento"));
        
        // Panel inferior para la imagen
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Imagen Extraída"));
        imagePanel.setPreferredSize(new Dimension(400, 100));
        
        components.labelImagen = new JLabel("Imagen extraída aparecerá aquí", SwingConstants.CENTER);
        components.labelImagen.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY, 1));
        components.labelImagen.setHorizontalAlignment(SwingConstants.CENTER);
        components.labelImagen.setVerticalAlignment(SwingConstants.CENTER);
        components.labelImagen.setPreferredSize(new Dimension(380, 80));
        
        imagePanel.add(components.labelImagen, BorderLayout.CENTER);
        
        panelDerecha.add(scrollPane, BorderLayout.CENTER);
        panelDerecha.add(imagePanel, BorderLayout.SOUTH);
        
        // Split pane: árbol a la izquierda, contenido a la derecha
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, components.scrollArbol, panelDerecha);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.18);  // 18% para el árbol, 82% para el contenido
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Crea el panel de entrada de datos
     */
    public static JPanel crearPanelSeleccion(Components components) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Entrada de Datos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // XML Input
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("XML a procesar:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        components.txtXmlInput = new JTextArea(8, 50);
        components.txtXmlInput.setLineWrap(true);
        components.txtXmlInput.setWrapStyleWord(true);
        components.txtXmlInput.setFont(new Font("Monospaced", Font.PLAIN, 10));
        
        JScrollPane xmlScroll = new JScrollPane(components.txtXmlInput);
        xmlScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(xmlScroll, gbc);
        
        // Tag Name
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Etiqueta XML:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        components.txtTagName = new JTextField(20);
        components.txtTagName.setToolTipText("Nombre de la etiqueta XML que contiene la imagen base64");
        panel.add(components.txtTagName, gbc);
        
        // Documento (opcional)
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Documento .docx:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        components.labelArchivo = new JLabel("Ninguno");
        components.labelArchivo.setForeground(java.awt.Color.BLUE);
        components.labelArchivo.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(components.labelArchivo, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        components.btnSeleccionar = new JButton("Examinar...");
        panel.add(components.btnSeleccionar, gbc);
        
        // Control Name (para Word)
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Etiqueta Content Control(docx):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        components.txtControlName = new JTextField(20);
        components.txtControlName.setToolTipText("Nombre del Content Control en el documento Word donde insertar la imagen");
        panel.add(components.txtControlName, gbc);
        
        // Botón de descarga
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        components.btnDescargar = new JButton("Descargar documento con imagen");
        components.btnDescargar.setEnabled(false);
        components.btnDescargar.setToolTipText("Crea una copia del documento con la imagen insertada en el Content Control");
        panel.add(components.btnDescargar, gbc);
        
        // Botón de descargar imagen
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        components.btnDescargarImagen = new JButton("Descargar imagen extraída");
        components.btnDescargarImagen.setEnabled(false);
        components.btnDescargarImagen.setToolTipText("Descarga la imagen extraída del XML como archivo PNG");
        panel.add(components.btnDescargarImagen, gbc);
        
        return panel;
    }

    /**
     * Crea el panel de botones de acción
     */
    public static JPanel crearPanelBotones(Components components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        components.btnProcesar = new JButton("Procesar Documento");
        components.btnProcesar.setEnabled(false);
        
        components.btnLimpiar = new JButton("Limpiar");
        components.btnLimpiar.setEnabled(false);
        
        JButton btnSalir = new JButton("Salir");
        
        panel.add(components.btnProcesar);
        panel.add(components.btnLimpiar);
        panel.add(btnSalir);
        
        return panel;
    }
}
