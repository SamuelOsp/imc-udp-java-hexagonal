package samuelospina.imc.cliente.dominio.puertos.salida;

import samuelospina.imc.cliente.dominio.modelos.DatosImc;
import samuelospina.imc.cliente.dominio.modelos.ResultadoImc;
import samuelospina.imc.cliente.dominio.vo.DestinoServidor;

/** Puerto de salida: hablar con el servidor, sin que el núcleo sepa que es UDP. */
public interface ClienteUdpPort {

  void conectar(DestinoServidor destino);

  void desconectar();

  ResultadoImc solicitarCalculo(DatosImc datos);

  boolean estaConectado();
}
