package samuelospina.imc.cliente.dominio.vo;

import java.util.Objects;
import samuelospina.imc.cliente.dominio.excepciones.DestinoIncorrectoException;

/** Value Object: a qué host y puerto UDP se envían los datagramas. */
public record DestinoServidor(String host, int puerto) {

  public DestinoServidor {
    if (Objects.isNull(host) || host.isBlank()) {
      throw new DestinoIncorrectoException("Escriba la DIRECCION IP del servidor.");
    }
    if (puerto < 1 || puerto > 65535) {
      throw new DestinoIncorrectoException("El puerto debe estar entre 1 y 65535.");
    }
    host = host.trim();
  }

  public String endpoint() {
    return host + ":" + puerto;
  }
}
