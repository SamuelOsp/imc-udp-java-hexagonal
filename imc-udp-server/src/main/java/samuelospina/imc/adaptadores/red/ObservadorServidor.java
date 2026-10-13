package samuelospina.imc.adaptadores.red;

import samuelospina.imc.dominio.enums.EstadoServidor;
import samuelospina.imc.dominio.modelos.EventoServidor;

/** Quien quiera enterarse de lo que pasa en el servidor (la GUI) implementa esta interfaz. */
public interface ObservadorServidor {

  void alRecibirEvento(EventoServidor evento);

  void alCambiarEstado(EstadoServidor estado);
}
