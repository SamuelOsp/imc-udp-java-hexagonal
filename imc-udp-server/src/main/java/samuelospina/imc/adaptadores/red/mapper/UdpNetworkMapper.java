package samuelospina.imc.adaptadores.red.mapper;

import java.util.Objects;
import samuelospina.imc.adaptadores.red.response.UdpResponse;
import samuelospina.imc.dominio.modelos.RespuestaCliente;

/**
 * Serializa una respuesta del dominio al protocolo de texto que viaja en el datagrama:
 * CONECTADO_OK;msg · DESCONECTADO_OK;msg · OK_CALCULO;imc;clasificacion;recomendacion · ERROR;msg
 */
public final class UdpNetworkMapper {

  public UdpResponse toNetworkResponse(final RespuestaCliente respuesta) {
    Objects.requireNonNull(respuesta, "No se puede serializar una respuesta nula.");
    final String payload =
        switch (respuesta.tipo()) {
          case CONECTADO -> "CONECTADO_OK;" + respuesta.mensaje();
          case DESCONECTADO -> "DESCONECTADO_OK;" + respuesta.mensaje();
          case OK_CALCULO ->
              "OK_CALCULO;"
                  + respuesta.resultado().imcFormateado()
                  + ";"
                  + respuesta.resultado().clasificacion().getClasificacion()
                  + ";"
                  + respuesta.resultado().clasificacion().getRecomendacion();
          case ERROR ->
              "ERROR;" + (Objects.nonNull(respuesta.mensaje()) ? respuesta.mensaje() : "Error desconocido.");
        };
    return new UdpResponse(payload, respuesta.destinatario().ip(), respuesta.destinatario().puerto());
  }
}
