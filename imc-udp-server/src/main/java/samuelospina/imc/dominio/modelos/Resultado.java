package samuelospina.imc.dominio.modelos;

import java.util.Locale;
import java.util.Objects;
import samuelospina.imc.dominio.enums.ClasificacionImc;

/** Resultado de un cálculo: el IMC y el rango en que cae. */
public record Resultado(double imc, ClasificacionImc clasificacion) {

  public Resultado {
    if (!Double.isFinite(imc)) {
      throw new IllegalArgumentException("El IMC debe ser un número finito.");
    }
    Objects.requireNonNull(clasificacion, "La clasificación es obligatoria.");
  }

  public String imcFormateado() {
    return String.format(Locale.US, "%.2f", imc);
  }
}
