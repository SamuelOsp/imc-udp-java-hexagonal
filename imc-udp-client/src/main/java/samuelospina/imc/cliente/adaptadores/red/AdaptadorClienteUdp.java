package samuelospina.imc.cliente.adaptadores.red;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;
import samuelospina.imc.cliente.aplicacion.excepciones.ClienteRedException;
import samuelospina.imc.cliente.dominio.excepciones.RespuestaServidorException;
import samuelospina.imc.cliente.dominio.modelos.DatosImc;
import samuelospina.imc.cliente.dominio.modelos.ResultadoImc;
import samuelospina.imc.cliente.dominio.puertos.salida.ClienteUdpPort;
import samuelospina.imc.cliente.dominio.vo.DestinoServidor;

/**
 * Adaptador de salida que implementa ClienteUdpPort con datagramas. Como UDP no tiene conexión,
 * "conectar" es solo un saludo (CONECTAR → CONECTADO_OK) para comprobar que el servidor responde;
 * después se recuerda a dónde enviar.
 */
public final class AdaptadorClienteUdp implements ClienteUdpPort {

  private final CanalUdp canal;
  private final ProtocoloUdpMapper protocolo;
  private volatile Sesion sesion;

  public AdaptadorClienteUdp(final CanalUdp canal, final ProtocoloUdpMapper protocolo) {
    this.canal = Objects.requireNonNull(canal, "El canal UDP es obligatorio.");
    this.protocolo = Objects.requireNonNull(protocolo, "El protocolo es obligatorio.");
  }

  @Override
  public synchronized void conectar(final DestinoServidor destino) {
    Objects.requireNonNull(destino, "El destino es obligatorio.");
    try {
      final InetAddress direccion = InetAddress.getByName(destino.host());
      canal.abrir();
      protocolo.validarConexion(canal.intercambiar(ProtocoloUdpMapper.CONECTAR, direccion, destino.puerto()));
      sesion = new Sesion(direccion, destino);
    } catch (final SocketTimeoutException excepcion) {
      canal.cerrar();
      throw new ClienteRedException(
          "El servidor " + destino.endpoint() + " no respondió en " + CanalUdp.TIMEOUT_MS / 1000 + " s.", excepcion);
    } catch (final UnknownHostException excepcion) {
      canal.cerrar();
      throw new ClienteRedException("No se pudo resolver el host " + destino.host() + ".", excepcion);
    } catch (final IOException | RespuestaServidorException excepcion) {
      canal.cerrar();
      throw new ClienteRedException("No se pudo conectar con " + destino.endpoint() + ".", excepcion);
    }
  }

  @Override
  public synchronized void desconectar() {
    try {
      if (Objects.nonNull(sesion)) {
        // Se avisa sin esperar respuesta: en UDP no hay nada que "cerrar" del otro lado.
        canal.enviar(ProtocoloUdpMapper.DESCONECTAR, sesion.direccion(), sesion.destino().puerto());
      }
    } catch (final IOException excepcion) {
      throw new ClienteRedException("No se pudo notificar la desconexión.", excepcion);
    } finally {
      sesion = null;
      canal.cerrar();
    }
  }

  @Override
  public synchronized ResultadoImc solicitarCalculo(final DatosImc datos) {
    final Sesion actual = sesion;
    if (Objects.isNull(actual)) {
      throw new ClienteRedException("Debe conectarse antes de calcular.");
    }
    try {
      final String respuesta = canal.intercambiar(
          protocolo.serializarCalculo(datos), actual.direccion(), actual.destino().puerto());
      return protocolo.parsearCalculo(respuesta);
    } catch (final SocketTimeoutException excepcion) {
      throw new ClienteRedException("El servidor no respondió: el datagrama pudo perderse.", excepcion);
    } catch (final IOException excepcion) {
      throw new ClienteRedException("Falló la comunicación con el servidor.", excepcion);
    } catch (final RespuestaServidorException excepcion) {
      throw new ClienteRedException(excepcion.getMessage(), excepcion);
    }
  }

  @Override
  public boolean estaConectado() {
    return Objects.nonNull(sesion) && canal.estaAbierto();
  }

  private record Sesion(InetAddress direccion, DestinoServidor destino) {}
}
