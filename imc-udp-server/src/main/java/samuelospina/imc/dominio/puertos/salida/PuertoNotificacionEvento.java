package samuelospina.imc.dominio.puertos.salida;

import samuelospina.imc.dominio.enums.EstadoServidor;
import samuelospina.imc.dominio.modelos.EventoServidor;

/** Puerto de salida: avisar hacia afuera (la GUI) lo que ocurre en el servidor. */
public interface PuertoNotificacionEvento {

  void notificarEvento(EventoServidor evento);

  void notificarEstado(EstadoServidor estado);
}
