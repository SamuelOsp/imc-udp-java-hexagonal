package samuelospina.imc.dominio.vo;

import java.util.Objects;
import samuelospina.imc.dominio.excepciones.DestinatarioIncorrectoException;

/**
 * Value Object: a quién se le responde. En UDP no hay conexión, así que cada respuesta debe llevar
 * explícitamente la IP y el puerto del cliente que envió el datagrama.
 */
public record Destinatario(String ip, int puerto) {

  public Destinatario {
    if (Objects.isNull(ip) || ip.isBlank()) {
      throw new DestinatarioIncorrectoException("La IP del destinatario es obligatoria.");
    }
    if (puerto < 1 || puerto > 65535) {
      throw new DestinatarioIncorrectoException("Puerto de destinatario fuera de rango: " + puerto);
    }
  }

  public String endpoint() {
    return ip + ":" + puerto;
  }
}
