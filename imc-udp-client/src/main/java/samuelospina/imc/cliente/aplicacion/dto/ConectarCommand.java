package samuelospina.imc.cliente.aplicacion.dto;

/** Datos tal como los escribió el usuario en la pantalla de conexión. */
public record ConectarCommand(String host, String puerto) {}
