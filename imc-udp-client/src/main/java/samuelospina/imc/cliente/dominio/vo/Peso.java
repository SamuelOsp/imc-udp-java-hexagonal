package samuelospina.imc.cliente.dominio.vo;

import samuelospina.imc.cliente.dominio.excepciones.PesoIncorrectoException;

/**
 * Value Object: peso a enviar, en kilogramos. El cliente valida ANTES de mandar el datagrama,
 * pero no calcula nada: la fórmula vive solo en el servidor.
 */
public record Peso(double valor) {

  public Peso {
    if (!Double.isFinite(valor) || valor <= 0) {
      throw new PesoIncorrectoException("El peso debe ser un número mayor que 0.");
    }
  }
}
