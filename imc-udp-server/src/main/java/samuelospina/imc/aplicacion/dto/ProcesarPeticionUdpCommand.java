package samuelospina.imc.aplicacion.dto;

/** Datos crudos de un datagrama recibido: quién lo mandó y qué texto traía. */
public record ProcesarPeticionUdpCommand(String ipOrigen, int puertoOrigen, String mensaje) {}
