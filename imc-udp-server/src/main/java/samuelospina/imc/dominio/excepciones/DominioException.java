package samuelospina.imc.dominio.excepciones;

/** Raíz de las reglas de negocio violadas dentro del dominio. */
public class DominioException extends RuntimeException {

  public DominioException(final String mensaje) {
    super(mensaje);
  }
}
