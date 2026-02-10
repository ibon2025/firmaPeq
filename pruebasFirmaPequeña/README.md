# Pruebas Firma Pequeña

Proyecto Java de pruebas para aplicaciones de firma digital con interfaz gráfica.

## Estructura del Proyecto

```
pruebasFirmaPequeña/
├── src/
│   ├── main/java/com/pruebas/firma/pequena/app/
│   │   ├── App.java                    # Clase principal con método de inserción de imágenes
│   │   ├── DocumentSelector.java       # Selector de documentos
│   │   ├── ImageSelector.java          # Selector de imágenes
│   │   └── PantallaPrincipal.java      # Interfaz gráfica principal
│   └── test/java/com/pruebas/firma/pequena/app/
│       └── AppTest.java                # Tests unitarios
├── target/
│   └── pruebas-firma-pequena-1.0.0.jar # JAR ejecutable
├── pom.xml                             # Configuración Maven
└── README.md                           # Este archivo
```

## Requisitos

- Java 11 o superior
- Maven 3.6 o superior

## Ejecución

### Opción 1: Ejecutar JAR
```bash
java -jar target/pruebas-firma-pequena-1.0.0.jar
```

### Opción 2: Ejecutar con Maven
```bash
mvn exec:java -Dexec.mainClass="com.pruebas.firma.pequena.app.PantallaPrincipal"
```

### Opción 3: Ejecutar directamente
```bash
mvn compile
java -cp target/classes com.pruebas.firma.pequena.app.PantallaPrincipal
```

## Funcionalidades

### ✅ Selector de Documentos
- Seleccionar documentos Word (.docx, .doc) y PDF
- Carga automática del contenido en memoria
- **Ordenación automática:** Los archivos se muestran ordenados de más nuevo a más viejo
- **Memoria de directorio:** Recuerda el último directorio usado entre sesiones
- Visualización de información del archivo

### ✅ Procesamiento Automático de Imágenes
- **Extracción automática:** Al procesar un documento, busca imágenes codificadas en base64 dentro del XML
- **Soporte completo para .docx:** Extrae automáticamente el contenido XML de archivos Word comprimidos
- **Tag específico:** Busca imágenes con el tag `<firmaPeq>base64data</firmaPeq>`
- **Mensajes en interfaz:** Todos los mensajes de consola (System.out.println) se muestran en la interfaz gráfica
- **Inserción opcional:** Si se encuentra imagen, se prepara para insertarla en content controls (requiere docx4j)

### 🔄 Inserción de Imágenes en Word (Requiere configuración adicional)

### Configuración para Inserción Completa

**Para activar la inserción automática de imágenes extraídas:**

1. **Descomenta la dependencia en pom.xml:**
   ```xml
   <dependency>
       <groupId>org.docx4j</groupId>
       <artifactId>docx4j</artifactId>
       <version>8.3.8</version>
   </dependency>
   ```

2. **Descomenta las importaciones en App.java:**
   ```java
   import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
   // ... otras importaciones
   ```

3. **Descomenta el método insertarImagenEnContentControl en App.java**

4. **Descomenta el código en PantallaPrincipal.java** (en el método procesarImagenDelDocumento)

5. **Recompila el proyecto:**
   ```bash
   mvn clean compile
   ```

**Nota:** La imagen se extrae automáticamente del documento XML, no requiere selección manual.

## Uso de la Aplicación

1. **Ejecuta la aplicación** usando cualquiera de los métodos anteriores
2. **Selecciona un documento Word** (.docx) haciendo clic en "Examinar..."
   - Los archivos se mostrarán ordenados de más nuevo a más viejo
   - La próxima vez, se abrirá en el último directorio usado
3. **Haz clic en "Procesar Documento"** para:
   - Mostrar información detallada del documento
   - **Extraer automáticamente el XML** del archivo .docx (descomprimiendo el ZIP interno)
   - **Ver todos los mensajes de procesamiento** directamente en la interfaz (no en consola)
   - **Buscar imágenes base64** en el campo `<firmaPeq>` del XML extraído
   - **Preparar inserción en content controls** (si docx4j está configurado)

### Funcionalidad de Extracción de Imágenes

La aplicación busca automáticamente en el contenido XML del documento etiquetas como:
```xml
<FirmaOutput>
  <firmaPeq>iVBORw0KGgoAAAANSUhEUgAAAHEAAABECAIAAAA5q02ZAAAB...</firmaPeq>
  <nombre>Lobato Elosegui, Eduardo</nombre>
</FirmaOutput>
```

Si encuentra datos base64 válidos en el campo `<firmaPeq>`, los decodifica y prepara para inserción.

### Configuración para Inserción Completa

## Construcción

```bash
mvn clean compile
```

## Ejecución de Tests

```bash
mvn test
```

## Instalación de Dependencias

```bash
mvn install
```

## Notas Técnicas

- **Paquetes Java:** `com.pruebas.firma.pequena.app`
- **Java Version:** 11
- **Interfaz:** Swing (GUI nativa)
- **Procesamiento Word:** docx4j (opcional)
- **Testing:** JUnit 5 + Mockito + AssertJ
- **Extracción de Imágenes:** Base64 desde XML del documento
- **Soporte .docx:** Descompresión automática de archivos ZIP para extraer document.xml
- **Persistencia:** Último directorio usado se guarda en `~/.pruebasFirmaPequeña.properties`
- **Salida de Consola:** Redirigida automáticamente a la interfaz gráfica
