package samuelospina.imc.dominio.modelos;

import java.util.Objects;
import samuelospina.imc.dominio.enums.TipoRespuesta;
import samuelospina.imc.dominio.vo.Destinatario;

/** Respuesta de negocio para un cliente, independiente del formato con que viaje por la red. */
public record RespuestaCliente(
    TipoRespuesta tipo, Destinatario destinatario, String mensaje, Resultado resultado) {

  public RespuestaCliente {
    Objects.requireNonNull(tipo, "El tipo de respuesta es obligatorio.");
    Objects.requireNonNull(destinatario, "El destinatario es obligatorio.");
  }

  public static RespuestaCliente conectado(final Destinatario destinatario, final String mensaje) {
    return new RespuestaCliente(TipoRespuesta.CONECTADO, destinatario, mensaje, null);
  }

  public static RespuestaCliente desconectado(final Destinatario destinatario, final String mensaje) {
    return new RespuestaCliente(TipoRespuesta.DESCONECTADO, destinatario, mensaje, null);
  }

  public static RespuestaCliente calculoExitoso(
      final Destinatario destinatario, final Resultado resultado) {
    return new RespuestaCliente(
        TipoRespuesta.OK_CALCULO,
        destinatario,
        null,
        Objects.requireNonNull(resultado, "El resultado es obligatorio."));
  }

  public static RespuestaCliente error(final Destinatario destinatario, final String mensaje) {
    return new RespuestaCliente(TipoRespuesta.ERROR, destinatario, mensaje, null);
  }
}
