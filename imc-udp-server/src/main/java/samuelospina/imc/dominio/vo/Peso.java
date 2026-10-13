package samuelospina.imc.dominio.vo;

import samuelospina.imc.dominio.excepciones.PesoIncorrectoException;

/** Value Object: peso en kilogramos. Regla de negocio: debe ser un número finito mayor que 0. */
public record Peso(double valor) {

  public Peso {
    if (!Double.isFinite(valor) || valor <= 0) {
      throw new PesoIncorrectoException("Peso incorrecto: debe ser mayor que 0");
    }
  }
}
