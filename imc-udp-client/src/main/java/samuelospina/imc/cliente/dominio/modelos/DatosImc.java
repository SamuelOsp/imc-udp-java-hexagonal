package samuelospina.imc.cliente.dominio.modelos;

import java.util.Objects;
import samuelospina.imc.cliente.dominio.vo.Altura;
import samuelospina.imc.cliente.dominio.vo.Peso;

/** Lo que el cliente le envía al servidor: peso y altura ya validados. */
public record DatosImc(Peso peso, Altura altura) {

  public DatosImc {
    Objects.requireNonNull(peso, "El peso es obligatorio.");
    Objects.requireNonNull(altura, "La altura es obligatoria.");
  }
}
