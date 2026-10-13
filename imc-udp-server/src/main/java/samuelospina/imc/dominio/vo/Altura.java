package samuelospina.imc.dominio.vo;

import samuelospina.imc.dominio.excepciones.AlturaIncorrectaException;

/**
 * Value Object: altura de la persona. Regla de negocio: debe ser un número finito mayor que 0.
 * Como en el repositorio guía, un valor mayor que 3 se interpreta como centímetros.
 */
public record Altura(double valor) {

  /** Por encima de este valor nadie mide eso en metros: se asume que vino en centímetros. */
  public static final double MAXIMO_EN_METROS = 3.0;

  public Altura {
    if (!Double.isFinite(valor) || valor <= 0) {
      throw new AlturaIncorrectaException("Altura incorrecta: debe ser mayor que 0");
    }
  }

  public double valorEnMetros() {
    return valor > MAXIMO_EN_METROS ? valor / 100.0 : valor;
  }
}
