package samuelospina.imc.cliente.dominio.excepciones;

/** Raíz de las reglas de negocio violadas en el cliente. */
public class DominioException extends RuntimeException {

  public DominioException(final String mensaje) {
    super(mensaje);
  }
}
