package samuelospina.imc.dominio.vo;

import samuelospina.imc.dominio.excepciones.PuertoIncorrectoException;

/** Value Object: puerto UDP en el que escucha el servidor. */
public record PuertoRed(int valor) {

  public PuertoRed {
    if (valor < 1 || valor > 65535) {
      throw new PuertoIncorrectoException("El puerto debe estar entre 1 y 65535.");
    }
  }
}
