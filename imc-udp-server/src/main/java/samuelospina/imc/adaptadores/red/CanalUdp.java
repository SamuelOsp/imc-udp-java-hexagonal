package samuelospina.imc.adaptadores.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Envoltura del único DatagramSocket del servidor. El mismo socket se usa para recibir todas las
 * peticiones y para enviar todas las respuestas: en UDP no hay un socket por cliente.
 */
public final class CanalUdp {

  private volatile DatagramSocket socket;

  public synchronized void abrir(final int puerto) throws SocketException {
    if (isAbierto()) {
      return;
    }
    socket = new DatagramSocket(puerto);
  }

  /** Bloquea hasta que llegue un datagrama (o hasta que el socket se cierre). */
  public void recibir(final DatagramPacket paquete) throws IOException {
    final DatagramSocket actual = socket;
    if (actual == null) {
      throw new SocketException("El canal UDP está cerrado.");
    }
    actual.receive(paquete);
  }

  public void enviar(final byte[] datos, final InetAddress destino, final int puerto)
      throws IOException {
    final DatagramSocket actual = socket;
    if (actual == null) {
      throw new SocketException("El canal UDP está cerrado.");
    }
    actual.send(new DatagramPacket(datos, datos.length, destino, puerto));
  }

  public synchronized void cerrar() {
    if (socket != null) {
      socket.close();
      socket = null;
    }
  }

  public boolean isAbierto() {
    final DatagramSocket actual = socket;
    return actual != null && !actual.isClosed();
  }

  public int getPuertoActual() {
    final DatagramSocket actual = socket;
    return actual == null ? -1 : actual.getLocalPort();
  }
}
