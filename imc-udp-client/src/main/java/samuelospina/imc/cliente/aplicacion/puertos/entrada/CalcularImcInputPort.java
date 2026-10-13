package samuelospina.imc.cliente.aplicacion.puertos.entrada;

import samuelospina.imc.cliente.aplicacion.dto.CalcularImcCommand;
import samuelospina.imc.cliente.dominio.modelos.ResultadoImc;

/** Caso de uso: pedirle al servidor el cálculo del IMC. */
public interface CalcularImcInputPort {

  ResultadoImc calcular(CalcularImcCommand comando);
}
