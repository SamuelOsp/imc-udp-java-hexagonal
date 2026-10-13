package samuelospina.imc.dominio.modelos;

import java.time.LocalTime;

/** Algo que pasó en el servidor y que vale la pena mostrar en el log. */
public record EventoServidor(String tipo, String origen, String descripcion, LocalTime hora) {

  public EventoServidor(final String tipo, final String origen, final String descripcion) {
    this(tipo, origen, descripcion, LocalTime.now());
  }
}
