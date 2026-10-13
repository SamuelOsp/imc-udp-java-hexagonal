package samuelospina.imc.cliente.aplicacion.servicios;

import java.util.Objects;
import samuelospina.imc.cliente.aplicacion.dto.CalcularImcCommand;
import samuelospina.imc.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.imc.cliente.aplicacion.excepciones.ClienteRedException;
import samuelospina.imc.cliente.aplicacion.mapper.ClienteMapper;
import samuelospina.imc.cliente.aplicacion.puertos.entrada.CalcularImcInputPort;
import samuelospina.imc.cliente.aplicacion.puertos.entrada.GestionarConexionInputPort;
import samuelospina.imc.cliente.dominio.modelos.ResultadoImc;
import samuelospina.imc.cliente.dominio.puertos.salida.ClienteUdpPort;

/** Implementa los casos de uso del cliente: valida con el dominio y delega en el puerto de red. */
public final class ClienteImcService implements GestionarConexionInputPort, CalcularImcInputPort {

  private final ClienteUdpPort clienteUdp;
  private final ClienteMapper mapper;

  public ClienteImcService(final ClienteUdpPort clienteUdp, final ClienteMapper mapper) {
    this.clienteUdp = Objects.requireNonNull(clienteUdp, "El puerto UDP es obligatorio.");
    this.mapper = Objects.requireNonNull(mapper, "El mapper es obligatorio.");
  }

  @Override
  public void conectar(final ConectarCommand comando) {
    clienteUdp.conectar(mapper.toDestino(comando));
  }

  @Override
  public void desconectar() {
    clienteUdp.desconectar();
  }

  @Override
  public boolean estaConectado() {
    return clienteUdp.estaConectado();
  }

  @Override
  public ResultadoImc calcular(final CalcularImcCommand comando) {
    if (!clienteUdp.estaConectado()) {
      throw new ClienteRedException("Cliente offline: conecte con el servidor primero.");
    }
    return clienteUdp.solicitarCalculo(mapper.toDatosImc(comando));
  }
}
