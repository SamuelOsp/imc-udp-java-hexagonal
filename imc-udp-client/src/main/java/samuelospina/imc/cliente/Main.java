package samuelospina.imc.cliente;

import javax.swing.SwingUtilities;
import samuelospina.imc.cliente.adaptadores.red.AdaptadorClienteUdp;
import samuelospina.imc.cliente.adaptadores.red.CanalUdp;
import samuelospina.imc.cliente.adaptadores.red.ProtocoloUdpMapper;
import samuelospina.imc.cliente.aplicacion.mapper.ClienteMapper;
import samuelospina.imc.cliente.aplicacion.servicios.ClienteImcService;
import samuelospina.imc.cliente.dominio.puertos.salida.ClienteUdpPort;
import samuelospina.imc.cliente.entrypoint.gui.ClienteFrame;

/**
 * Composition Root del cliente: crea el adaptador UDP, se lo inyecta al servicio y entrega el
 * servicio (visto como puertos de entrada) a la GUI.
 *
 * @author Samuel David Ospina De Avila
 */
public final class Main {

  private Main() {}

  public static void main(final String[] args) {
    final ClienteUdpPort clienteUdp = new AdaptadorClienteUdp(new CanalUdp(), new ProtocoloUdpMapper());
    final ClienteImcService servicio = new ClienteImcService(clienteUdp, new ClienteMapper());

    SwingUtilities.invokeLater(() -> new ClienteFrame(servicio, servicio).setVisible(true));
  }
}
