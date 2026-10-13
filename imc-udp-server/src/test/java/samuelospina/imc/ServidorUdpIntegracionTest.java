package samuelospina.imc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import samuelospina.imc.adaptadores.red.AdaptadorControlServidorRed;
import samuelospina.imc.adaptadores.red.AdaptadorNotificacionEvento;
import samuelospina.imc.adaptadores.red.AdaptadorSalidaUdp;
import samuelospina.imc.adaptadores.red.CanalUdp;
import samuelospina.imc.adaptadores.red.mapper.UdpNetworkMapper;
import samuelospina.imc.aplicacion.excepciones.ServidorRedException;
import samuelospina.imc.aplicacion.mapper.PeticionMapper;
import samuelospina.imc.aplicacion.servicios.GestionarServidorService;
import samuelospina.imc.aplicacion.servicios.ProcesarPeticionUdpService;
import samuelospina.imc.entrypoint.udp.ReceptorPeticionesUdp;

/** Levanta el servidor real (sin GUI) y le habla con un DatagramSocket de verdad. */
class ServidorUdpIntegracionTest {

  private static int puertoLibre() throws Exception {
    try (DatagramSocket s = new DatagramSocket(0)) {
      return s.getLocalPort();
    }
  }

  private static GestionarServidorService armarServidor() {
    final CanalUdp canal = new CanalUdp();
    final AdaptadorNotificacionEvento notificador = new AdaptadorNotificacionEvento();
    final ProcesarPeticionUdpService procesar = new ProcesarPeticionUdpService(
        new AdaptadorSalidaUdp(canal, new UdpNetworkMapper(), notificador), notificador, new PeticionMapper());
    final ReceptorPeticionesUdp receptor = new ReceptorPeticionesUdp(canal, procesar, notificador);
    return new GestionarServidorService(
        new AdaptadorControlServidorRed(canal, receptor::iniciar, receptor::detener), notificador);
  }

  private static String intercambiar(final DatagramSocket cliente, final String msg, final int puerto)
      throws Exception {
    final byte[] datos = msg.getBytes(StandardCharsets.UTF_8);
    cliente.send(new DatagramPacket(datos, datos.length, InetAddress.getLoopbackAddress(), puerto));
    final DatagramPacket respuesta = new DatagramPacket(new byte[2048], 2048);
    cliente.receive(respuesta);
    return new String(respuesta.getData(), 0, respuesta.getLength(), StandardCharsets.UTF_8);
  }

  @Test
  void atiendeDatagramasReales() throws Exception {
    final int puerto = puertoLibre();
    final GestionarServidorService servidor = armarServidor();
    servidor.iniciar(puerto);
    try (DatagramSocket cliente = new DatagramSocket()) {
      cliente.setSoTimeout(2000);
      assertTrue(intercambiar(cliente, "CONECTAR", puerto).startsWith("CONECTADO_OK;"));
      assertTrue(intercambiar(cliente, "CALCULAR;65;1.70", puerto).startsWith("OK_CALCULO;22.49;Peso normal"));
      assertEquals("ERROR;Peso incorrecto: debe ser mayor que 0", intercambiar(cliente, "CALCULAR;0;1.70", puerto));
    } finally {
      servidor.detener();
    }
    assertFalse(servidor.estaActivo());
  }

  @Test
  void seDetieneYVuelveAIniciarEnElMismoPuerto() throws Exception {
    final int puerto = puertoLibre();
    final GestionarServidorService servidor = armarServidor();
    for (int ciclo = 0; ciclo < 3; ciclo++) {
      servidor.iniciar(puerto);
      try (DatagramSocket cliente = new DatagramSocket()) {
        cliente.setSoTimeout(2000);
        assertTrue(intercambiar(cliente, "CALCULAR;78;1.70", puerto).startsWith("OK_CALCULO;26.99;"));
      } finally {
        servidor.detener();
      }
    }
  }

  @Test
  void puertoOcupadoLanzaExcepcionDeRed() throws Exception {
    try (DatagramSocket ocupado = new DatagramSocket(0)) {
      assertThrows(ServidorRedException.class, () -> armarServidor().iniciar(ocupado.getLocalPort()));
    }
  }
}
