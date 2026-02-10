package com.pruebasfirmapequeña.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Test de la clase App")
public class AppTest {
    
    @Test
    @DisplayName("Debe retornar un saludo correctamente")
    void testSaludo() {
        App app = new App();
        String resultado = app.saludo("Juan");
        assertThat(resultado).isEqualTo("Hola, Juan");
    }
}
