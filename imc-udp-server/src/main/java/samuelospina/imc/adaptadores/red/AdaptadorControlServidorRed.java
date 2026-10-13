package samuelospina.imc.adaptadores.red;

import java.net.SocketException;
import java.util.Objects;
import samuelospina.imc.aplicacion.excepciones.ServidorRedException;
import samuelospina.imc.dominio.puertos.salida.ControladorServidorRedPort;
import samuelospina.imc.dominio.vo.PuertoRed;

/**
 * Adaptador de salida: abre/cierra el DatagramSocket y arranca/detiene el hilo receptor. Recibe
 * el receptor como simples Runnable para no depender del entrypoint.
 */
public final class AdaptadorControlServidorRed implements ControladorServidorRedPort {

  private final CanalUdp canal;
  private final Runnable alIniciar;
  private final Runnable alDetener;

  public AdaptadorControlServidorRed(
      final CanalUdp canal, final Runnable alIniciar, final Runnable alDetener) {
    this.canal = Objects.requireNonNull(canal, "El canal UDP es obligatorio.");
    this.alIniciar = Objects.requireNonNull(alIniciar, "La acción de inicio es obligatoria.");
    this.alDetener = Objects.requireNonNull(alDetener, "La acción de detención es obligatoria.");
  }

  @Override
  public void iniciar(final PuertoRed puerto) {
    try {
      canal.abrir(puerto.valor());
    } catch (final SocketException excepcion) {
      throw new ServidorRedException(
          "No se pudo abrir el puerto UDP " + puerto.valor() + ": " + excepcion.getMessage(), excepcion);
    }
    alIniciar.run();
  }

  @Override
  public void detener() {
    alDetener.run();
    canal.cerrar(); // cerrar el socket desbloquea el receive() del hilo receptor
  }

  @Override
  public boolean estaActivo() {
    return canal.isAbierto();
  }
}
