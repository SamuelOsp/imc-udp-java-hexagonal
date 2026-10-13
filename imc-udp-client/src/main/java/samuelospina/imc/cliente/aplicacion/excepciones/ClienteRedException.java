package samuelospina.imc.cliente.aplicacion.excepciones;

/** Falla de comunicación con el servidor (timeout, host desconocido, respuesta de error...). */
public final class ClienteRedException extends RuntimeException {

  public ClienteRedException(final String mensaje) {
    super(mensaje);
  }

  public ClienteRedException(final String mensaje, final Throwable causa) {
    super(mensaje, causa);
  }
}
