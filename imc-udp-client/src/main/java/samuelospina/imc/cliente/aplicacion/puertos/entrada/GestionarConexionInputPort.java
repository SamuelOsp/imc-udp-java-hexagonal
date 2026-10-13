package samuelospina.imc.cliente.aplicacion.puertos.entrada;

import samuelospina.imc.cliente.aplicacion.dto.ConectarCommand;

/** Caso de uso: verificar que el servidor responde y terminar la sesión. */
public interface GestionarConexionInputPort {

  void conectar(ConectarCommand comando);

  void desconectar();

  boolean estaConectado();
}
