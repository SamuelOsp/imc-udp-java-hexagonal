package samuelospina.imc.cliente.dominio.vo;

import samuelospina.imc.cliente.dominio.excepciones.AlturaIncorrectaException;

/**
 * Value Object: altura a enviar. Se acepta en metros (1.70) o en centímetros (170): quien decide
 * cómo interpretarla es el servidor.
 */
public record Altura(double valor) {

  public Altura {
    if (!Double.isFinite(valor) || valor <= 0) {
      throw new AlturaIncorrectaException("La altura debe ser un número mayor que 0.");
    }
  }
}
