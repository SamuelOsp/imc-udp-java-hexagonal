package samuelospina.imc.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import samuelospina.imc.cliente.adaptadores.red.ProtocoloUdpMapper;
import samuelospina.imc.cliente.aplicacion.dto.CalcularImcCommand;
import samuelospina.imc.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.imc.cliente.aplicacion.mapper.ClienteMapper;
import samuelospina.imc.cliente.dominio.excepciones.AlturaIncorrectaException;
import samuelospina.imc.cliente.dominio.excepciones.DestinoIncorrectoException;
import samuelospina.imc.cliente.dominio.excepciones.PesoIncorrectoException;
import samuelospina.imc.cliente.dominio.excepciones.RespuestaServidorException;
import samuelospina.imc.cliente.dominio.modelos.DatosImc;
import samuelospina.imc.cliente.dominio.modelos.ResultadoImc;
import samuelospina.imc.cliente.dominio.vo.Altura;
import samuelospina.imc.cliente.dominio.vo.Peso;

class ProtocoloYMapperTest {

  private final ProtocoloUdpMapper protocolo = new ProtocoloUdpMapper();
  private final ClienteMapper mapper = new ClienteMapper();

  @Test
  void serializaYParsea() {
    assertEquals("CALCULAR;78.50;1.70",
        protocolo.serializarCalculo(new DatosImc(new Peso(78.5), new Altura(1.7))));
    assertEquals(new ResultadoImc(26.99, "Sobrepeso (Preobesidad)", "Modere el consumo calórico."),
        protocolo.parsearCalculo("OK_CALCULO;26.99;Sobrepeso (Preobesidad);Modere el consumo calórico."));
  }

  @Test
  void errorDelServidorLlegaComoExcepcion() {
    final RespuestaServidorException e = assertThrows(RespuestaServidorException.class,
        () -> protocolo.parsearCalculo("ERROR;Peso incorrecto: debe ser mayor que 0"));
    assertEquals("Peso incorrecto: debe ser mayor que 0", e.getMessage());
    assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OK_CALCULO;abc;x;y"));
    assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OK_CALCULO;26.99"));
    assertThrows(RespuestaServidorException.class, () -> protocolo.validarConexion("HOLA"));
  }

  @Test
  void validaEntradasDelUsuario() {
    assertEquals(new DatosImc(new Peso(78.5), new Altura(1.7)),
        mapper.toDatosImc(new CalcularImcCommand(" 78,5 ", "1,70")));
    assertThrows(PesoIncorrectoException.class, () -> mapper.toDatosImc(new CalcularImcCommand("", "1.70")));
    assertThrows(PesoIncorrectoException.class, () -> mapper.toDatosImc(new CalcularImcCommand("abc", "1.70")));
    assertThrows(PesoIncorrectoException.class, () -> mapper.toDatosImc(new CalcularImcCommand("0", "1.70")));
    assertThrows(AlturaIncorrectaException.class, () -> mapper.toDatosImc(new CalcularImcCommand("70", "")));
    assertThrows(AlturaIncorrectaException.class, () -> mapper.toDatosImc(new CalcularImcCommand("70", "x")));
    assertThrows(AlturaIncorrectaException.class, () -> mapper.toDatosImc(new CalcularImcCommand("70", "-1")));
    assertThrows(DestinoIncorrectoException.class, () -> mapper.toDestino(new ConectarCommand("localhost", "x")));
    assertThrows(DestinoIncorrectoException.class, () -> mapper.toDestino(new ConectarCommand(" ", "9007")));
  }
}
