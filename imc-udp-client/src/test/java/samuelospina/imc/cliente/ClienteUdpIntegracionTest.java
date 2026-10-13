package samuelospina.imc.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import samuelospina.imc.cliente.adaptadores.red.AdaptadorClienteUdp;
import samuelospina.imc.cliente.adaptadores.red.CanalUdp;
import samuelospina.imc.cliente.adaptadores.red.ProtocoloUdpMapper;
import samuelospina.imc.cliente.aplicacion.dto.CalcularImcCommand;
import samuelospina.imc.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.imc.cliente.aplicacion.excepciones.ClienteRedException;
import samuelospina.imc.cliente.aplicacion.mapper.ClienteMapper;
import samuelospina.imc.cliente.aplicacion.servicios.ClienteImcService;
import samuelospina.imc.cliente.dominio.modelos.ResultadoImc;

/** Prueba el cliente real contra un servidor UDP de mentira que responde lo esperado. */
class ClienteUdpIntegracionTest {

  private static ClienteImcService nuevoCliente() {
    return new ClienteImcService(
        new AdaptadorClienteUdp(new CanalUdp(), new ProtocoloUdpMapper()), new ClienteMapper());
  }

  @Test
  void conectaCalculaYDesconecta() throws Exception {
    final List<String> recibidos = Collections.synchronizedList(new ArrayList<>());
    try (DatagramSocket servidorFalso = new DatagramSocket(0)) {
      final Thread hilo = new Thread(() -> {
        try {
          for (String respuesta : new String[] {
            "CONECTADO_OK;listo", "OK_CALCULO;26.99;Sobrepeso (Preobesidad);Modere el consumo calórico."
          }) {
            final DatagramPacket p = new DatagramPacket(new byte[2048], 2048);
            servidorFalso.receive(p);
            recibidos.add(new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8));
            final byte[] datos = respuesta.getBytes(StandardCharsets.UTF_8);
            servidorFalso.send(new DatagramPacket(datos, datos.length, p.getAddress(), p.getPort()));
          }
        } catch (final Exception ignorada) {
          // el socket se cierra al final de la prueba
        }
      });
      hilo.start();

      final ClienteImcService cliente = nuevoCliente();
      cliente.conectar(new ConectarCommand("127.0.0.1", String.valueOf(servidorFalso.getLocalPort())));
      assertTrue(cliente.estaConectado());
      final ResultadoImc resultado = cliente.calcular(new CalcularImcCommand("78", "1,70"));
      assertEquals(26.99, resultado.imc(), 1e-9);
      assertEquals("Sobrepeso (Preobesidad)", resultado.clasificacion());
      assertEquals(List.of("CONECTAR", "CALCULAR;78.00;1.70"), recibidos);
      cliente.desconectar();
      assertFalse(cliente.estaConectado());
    }
  }

  @Test
  void sinServidorFallaPorTimeout() throws Exception {
    final int puertoMudo;
    try (DatagramSocket s = new DatagramSocket(0)) {
      puertoMudo = s.getLocalPort();
    }
    final ClienteImcService cliente = nuevoCliente();
    assertThrows(ClienteRedException.class,
        () -> cliente.conectar(new ConectarCommand("127.0.0.1", String.valueOf(puertoMudo))));
    assertThrows(ClienteRedException.class, () -> cliente.calcular(new CalcularImcCommand("70", "1.70")));
  }
}
