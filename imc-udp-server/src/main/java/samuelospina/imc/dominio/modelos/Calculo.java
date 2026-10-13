package samuelospina.imc.dominio.modelos;

import java.util.Objects;
import samuelospina.imc.dominio.enums.ClasificacionImc;
import samuelospina.imc.dominio.vo.Altura;
import samuelospina.imc.dominio.vo.Peso;

/**
 * Entidad central del negocio. Es el ÚNICO lugar de todo el sistema donde vive la fórmula
 * IMC = peso / altura². No sabe nada de UDP, Swing ni sockets.
 */
public final class Calculo {

  private final Peso peso;
  private final Altura altura;

  public Calculo(final Peso peso, final Altura altura) {
    this.peso = Objects.requireNonNull(peso, "El peso es obligatorio.");
    this.altura = Objects.requireNonNull(altura, "La altura es obligatoria.");
  }

  public Resultado calcular() {
    final double metros = altura.valorEnMetros();
    final double imc = peso.valor() / (metros * metros);
    return new Resultado(imc, ClasificacionImc.deImc(imc));
  }

  public Peso getPeso() {
    return peso;
  }

  public Altura getAltura() {
    return altura;
  }
}
