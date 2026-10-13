package samuelospina.imc.adaptadores.red;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import samuelospina.imc.dominio.enums.EstadoServidor;
import samuelospina.imc.dominio.modelos.EventoServidor;
import samuelospina.imc.dominio.puertos.salida.PuertoNotificacionEvento;

/** Adaptador de salida (patrón Observer): reparte los eventos a todos los observadores. */
public final class AdaptadorNotificacionEvento implements PuertoNotificacionEvento {

  private final List<ObservadorServidor> observadores = new CopyOnWriteArrayList<>();

  public void registrarObservador(final ObservadorServidor observador) {
    observadores.add(Objects.requireNonNull(observador, "El observador es obligatorio."));
  }

  @Override
  public void notificarEvento(final EventoServidor evento) {
    observadores.forEach(o -> o.alRecibirEvento(evento));
  }

  @Override
  public void notificarEstado(final EstadoServidor estado) {
    observadores.forEach(o -> o.alCambiarEstado(estado));
  }
}
