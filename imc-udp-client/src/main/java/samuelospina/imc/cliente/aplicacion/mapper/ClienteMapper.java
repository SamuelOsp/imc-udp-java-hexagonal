package samuelospina.imc.cliente.aplicacion.mapper;

import java.util.Objects;
import samuelospina.imc.cliente.aplicacion.dto.CalcularImcCommand;
import samuelospina.imc.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.imc.cliente.dominio.excepciones.AlturaIncorrectaException;
import samuelospina.imc.cliente.dominio.excepciones.DestinoIncorrectoException;
import samuelospina.imc.cliente.dominio.excepciones.PesoIncorrectoException;
import samuelospina.imc.cliente.dominio.modelos.DatosImc;
import samuelospina.imc.cliente.dominio.vo.Altura;
import samuelospina.imc.cliente.dominio.vo.DestinoServidor;
import samuelospina.imc.cliente.dominio.vo.Peso;

/** Convierte el texto de la GUI en objetos de dominio validados. */
public final class ClienteMapper {

  public DestinoServidor toDestino(final ConectarCommand comando) {
    final String puerto = Objects.toString(comando.puerto(), "").trim();
    try {
      return new DestinoServidor(comando.host(), Integer.parseInt(puerto));
    } catch (final NumberFormatException excepcion) {
      throw new DestinoIncorrectoException("El PUERTO DE RED debe ser un número entero.");
    }
  }

  public DatosImc toDatosImc(final CalcularImcCommand comando) {
    final String peso = normalizar(comando.peso());
    if (peso.isEmpty()) {
      throw new PesoIncorrectoException("Escriba el peso en kilogramos.");
    }
    final String altura = normalizar(comando.altura());
    if (altura.isEmpty()) {
      throw new AlturaIncorrectaException("Escriba la altura en metros.");
    }
    final Peso pesoValido;
    try {
      pesoValido = new Peso(Double.parseDouble(peso));
    } catch (final NumberFormatException excepcion) {
      throw new PesoIncorrectoException("\"" + peso + "\" no es un número válido.");
    }
    try {
      return new DatosImc(pesoValido, new Altura(Double.parseDouble(altura)));
    } catch (final NumberFormatException excepcion) {
      throw new AlturaIncorrectaException("\"" + altura + "\" no es un número válido.");
    }
  }

  private static String normalizar(final String texto) {
    return Objects.toString(texto, "").trim().replace(',', '.');
  }
}
