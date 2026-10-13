package samuelospina.imc.cliente.aplicacion.dto;

/** Peso y altura tal como los escribió el usuario. */
public record CalcularImcCommand(String peso, String altura) {}
