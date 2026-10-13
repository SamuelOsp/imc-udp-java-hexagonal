package samuelospina.imc.cliente.adaptadores.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Envoltura del DatagramSocket del cliente. UDP no garantiza entrega: si la respuesta no llega en
 * TIMEOUT_MS, receive() lanza SocketTimeoutException en lugar de quedarse bloqueado para siempre.
 */
public final class CanalUdp {

  public static final int TIMEOUT_MS = 3000;
  private static final int TAMANO_BUFFER = 2048;

  private DatagramSocket socket;

  public synchronized void abrir() throws SocketException {
    if (estaAbierto()) {
      return;
    }
    socket = new DatagramSocket(); // puerto local efímero, lo elige el sistema operativo
    socket.setSoTimeout(TIMEOUT_MS);
  }

  public synchronized void enviar(final String mensaje, final InetAddress destino, final int puerto)
      throws IOException {
    if (!estaAbierto()) {
      throw new SocketException("El canal UDP está cerrado.");
    }
    final byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);
    socket.send(new DatagramPacket(datos, datos.length, destino, puerto));
  }

  /** Envía un datagrama y espera la respuesta del MISMO servidor (descarta datagramas ajenos). */
  public synchronized String intercambiar(
      final String mensaje, final InetAddress destino, final int puerto) throws IOException {
    enviar(mensaje, destino, puerto);
    final byte[] buffer = new byte[TAMANO_BUFFER];
    while (true) {
      final DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length);
      socket.receive(respuesta);
      if (destino.equals(respuesta.getAddress()) && respuesta.getPort() == puerto) {
        return new String(respuesta.getData(), 0, respuesta.getLength(), StandardCharsets.UTF_8).trim();
      }
    }
  }

  public synchronized void cerrar() {
    if (socket != null) {
      socket.close();
      socket = null;
    }
  }

  public synchronized boolean estaAbierto() {
    return socket != null && !socket.isClosed();
  }
}
