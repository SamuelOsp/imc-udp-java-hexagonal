package samuelospina.imc.aplicacion.servicios;

import java.util.Objects;
import samuelospina.imc.aplicacion.puertos.entrada.GestionarServidorInputPort;
import samuelospina.imc.dominio.enums.EstadoServidor;
import samuelospina.imc.dominio.modelos.EventoServidor;
import samuelospina.imc.dominio.puertos.salida.ControladorServidorRedPort;
import samuelospina.imc.dominio.puertos.salida.PuertoNotificacionEvento;
import samuelospina.imc.dominio.vo.PuertoRed;

/** Caso de uso: encender/apagar el servidor y avisar el cambio de estado. */
public final class GestionarServidorService implements GestionarServidorInputPort {

  private final ControladorServidorRedPort controladorRed;
  private final PuertoNotificacionEvento notificador;

  public GestionarServidorService(
      final ControladorServidorRedPort controladorRed, final PuertoNotificacionEvento notificador) {
    this.controladorRed = Objects.requireNonNull(controladorRed, "El controlador de red es obligatorio.");
    this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
  }

  @Override
  public void iniciar(final int puerto) {
    final PuertoRed puertoRed = new PuertoRed(puerto);
    controladorRed.iniciar(puertoRed);
    notificador.notificarEstado(EstadoServidor.ESCUCHANDO);
    notificador.notificarEvento(new EventoServidor(
        "SISTEMA", "servidor", "DatagramSocket escuchando en el puerto UDP " + puertoRed.valor()));
  }

  @Override
  public void detener() {
    controladorRed.detener();
    notificador.notificarEstado(EstadoServidor.DETENIDO);
    notificador.notificarEvento(new EventoServidor("SISTEMA", "servidor", "servidor detenido"));
  }

  @Override
  public boolean estaActivo() {
    return controladorRed.estaActivo();
  }
}
