package samuelospina.imc.adaptadores.red;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import samuelospina.imc.adaptadores.red.mapper.UdpNetworkMapper;
import samuelospina.imc.adaptadores.red.response.UdpResponse;
import samuelospina.imc.dominio.modelos.EventoServidor;
import samuelospina.imc.dominio.modelos.RespuestaCliente;
import samuelospina.imc.dominio.puertos.salida.PuertoNotificacionEvento;
import samuelospina.imc.dominio.puertos.salida.PuertoSalidaRed;

/** Adaptador de salida: implementa PuertoSalidaRed enviando un datagrama UDP de respuesta. */
public final class AdaptadorSalidaUdp implements PuertoSalidaRed {

  private final CanalUdp canal;
  private final UdpNetworkMapper mapper;
  private final PuertoNotificacionEvento notificador;

  public AdaptadorSalidaUdp(
      final CanalUdp canal, final UdpNetworkMapper mapper, final PuertoNotificacionEvento notificador) {
    this.canal = Objects.requireNonNull(canal, "El canal UDP es obligatorio.");
    this.mapper = Objects.requireNonNull(mapper, "El mapper es obligatorio.");
    this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
  }

  @Override
  public void enviarRespuesta(final RespuestaCliente respuesta) {
    final UdpResponse udp = mapper.toNetworkResponse(respuesta);
    try {
      final byte[] datos = udp.payload().getBytes(StandardCharsets.UTF_8);
      canal.enviar(datos, InetAddress.getByName(udp.ip()), udp.puerto());
    } catch (final IOException excepcion) {
      notificador.notificarEvento(new EventoServidor(
          "ERROR", udp.ip() + ":" + udp.puerto(), "no se pudo enviar la respuesta: " + excepcion.getMessage()));
    }
  }
}
