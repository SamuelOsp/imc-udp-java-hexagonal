package samuelospina.imc.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import samuelospina.imc.dominio.enums.ClasificacionImc;
import samuelospina.imc.dominio.excepciones.AlturaIncorrectaException;
import samuelospina.imc.dominio.excepciones.PesoIncorrectoException;
import samuelospina.imc.dominio.modelos.Calculo;
import samuelospina.imc.dominio.modelos.Resultado;
import samuelospina.imc.dominio.vo.Altura;
import samuelospina.imc.dominio.vo.Peso;

class CalculoTest {

  @ParameterizedTest
  @CsvSource({
    "50, 1.75, 16.33, BAJO_PESO",
    "65, 1.70, 22.49, NORMAL",
    "78, 1.70, 26.99, SOBREPESO",
    "95, 1.70, 32.87, OBESIDAD_I",
    "110, 1.70, 38.06, OBESIDAD_II",
    "130, 1.70, 44.98, OBESIDAD_III"
  })
  void calculaPuntosConocidos(
      final double peso, final double altura, final String imc, final ClasificacionImc clasificacion) {
    final Resultado resultado = new Calculo(new Peso(peso), new Altura(altura)).calcular();
    assertEquals(imc, resultado.imcFormateado());
    assertEquals(clasificacion, resultado.clasificacion());
  }

  @ParameterizedTest
  @CsvSource({
    "18.49, BAJO_PESO", "18.5, NORMAL", "24.99, NORMAL", "25, SOBREPESO", "29.99, SOBREPESO",
    "30, OBESIDAD_I", "34.99, OBESIDAD_I", "35, OBESIDAD_II", "39.99, OBESIDAD_II", "40, OBESIDAD_III"
  })
  void clasificaEnLasFronteras(final double imc, final ClasificacionImc esperada) {
    assertEquals(esperada, ClasificacionImc.deImc(imc));
  }

  @Test
  void alturaMayorQueTresSeTomaEnCentimetros() {
    assertEquals(1.70, new Altura(170).valorEnMetros(), 1e-9);
    assertEquals(3.0, new Altura(3.0).valorEnMetros(), 1e-9);
    assertEquals(
        new Calculo(new Peso(78), new Altura(1.70)).calcular().imcFormateado(),
        new Calculo(new Peso(78), new Altura(170)).calcular().imcFormateado());
  }

  @ParameterizedTest
  @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY})
  void rechazaPesosYAlturasImposibles(final double valor) {
    assertThrows(PesoIncorrectoException.class, () -> new Peso(valor));
    assertThrows(AlturaIncorrectaException.class, () -> new Altura(valor));
  }
}
