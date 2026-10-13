package samuelospina.imc.aplicacion.puertos.entrada;

import samuelospina.imc.aplicacion.dto.ProcesarPeticionUdpCommand;

/** Caso de uso: atender una petición que llegó por la red. */
public interface ProcesarPeticionUdpInputPort {

  void procesar(ProcesarPeticionUdpCommand comando);
}
