package samuelospina.imc.aplicacion.mapper;

import samuelospina.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import samuelospina.imc.dominio.vo.Destinatario;

/** Traduce el comando de entrada a objetos del dominio. */
public final class PeticionMapper {

  public Destinatario toDestinatario(final ProcesarPeticionUdpCommand comando) {
    return new Destinatario(comando.ipOrigen(), comando.puertoOrigen());
  }
}
