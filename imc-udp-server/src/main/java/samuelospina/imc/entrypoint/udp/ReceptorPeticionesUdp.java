package samuelospina.imc.entrypoint.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import samuelospina.imc.adaptadores.red.CanalUdp;
import samuelospina.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import samuelospina.imc.aplicacion.puertos.entrada.ProcesarPeticionUdpInputPort;
import samuelospina.imc.dominio.modelos.EventoServidor;
import samuelospina.imc.dominio.puertos.salida.PuertoNotificacionEvento;

/**
 * Entrypoint primario (adaptador de entrada). Un solo hilo en segundo plano hace receive() en
 * bucle: cada datagrama es una petición independiente y trae consigo la IP y el puerto de quien
 * lo envió. Con eso arma el comando y lo entrega al puerto de entrada.
 */
public final class ReceptorPeticionesUdp {

  private static final int TAMANO_BUFFER = 2048;

  private final CanalUdp canal;
  private final ProcesarPeticionUdpInputPort inputPort;
  private final PuertoNotificacionEvento notificador;

  private Thread hiloEscucha;
  private volatile boolean escuchando;

  public ReceptorPeticionesUdp(
      final CanalUdp canal,
      final ProcesarPeticionUdpInputPort inputPort,
      final PuertoNotificacionEvento notificador) {
    this.canal = Objects.requireNonNull(canal, "El canal UDP es obligatorio.");
    this.inputPort = Objects.requireNonNull(inputPort, "El puerto de entrada es obligatorio.");
    this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
  }

  public synchronized void iniciar() {
    if (escuchando) {
      return;
    }
    escuchando = true;
    hiloEscucha = new Thread(this::cicloEscucha, "ReceptorUdp-" + canal.getPuertoActual());
    hiloEscucha.setDaemon(true);
    hiloEscucha.start();
  }

  public synchronized void detener() {
    escuchando = false;
    hiloEscucha = null;
  }

  public boolean isEscuchando() {
    return escuchando;
  }

  private void cicloEscucha() {
    final byte[] buffer = new byte[TAMANO_BUFFER];
    while (escuchando && canal.isAbierto()) {
      try {
        final DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        canal.recibir(paquete);

        final String texto =
            new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8).trim();
        inputPort.procesar(new ProcesarPeticionUdpCommand(
            paquete.getAddress().getHostAddress(), paquete.getPort(), texto));

      } catch (final SocketException excepcion) {
        if (!escuchando) {
          break; // el socket se cerró a propósito al detener el servidor
        }
        notificarError("error de socket al recibir: " + excepcion.getMessage());
      } catch (final IOException | RuntimeException excepcion) {
        if (escuchando) {
          notificarError("error al procesar datagrama: " + excepcion.getMessage());
        }
      }
    }
  }

  private void notificarError(final String mensaje) {
    notificador.notificarEvento(new EventoServidor("ERROR", "socket", mensaje));
  }
}
