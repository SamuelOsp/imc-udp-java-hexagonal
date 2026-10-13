package samuelospina.imc.dominio.enums;

/** Rangos del IMC con su clasificación y recomendación, los mismos del repositorio guía. */
public enum ClasificacionImc {
  BAJO_PESO(
      "Bajo peso (Insuficiencia ponderal)",
      "Se sugiere una dieta nutritiva y balanceada para alcanzar el peso adecuado."),
  NORMAL(
      "Peso normal (Saludable)",
      "¡Excelente! Mantén tus hábitos de alimentación balanceada y actividad física."),
  SOBREPESO(
      "Sobrepeso (Preobesidad)",
      "Se aconseja incrementar la actividad física y moderar el consumo calórico."),
  OBESIDAD_I(
      "Obesidad Grado I (Moderada)", "Se recomienda asesoría nutricional y seguimiento médico."),
  OBESIDAD_II(
      "Obesidad Grado II (Severa)", "Riesgo elevado. Requiere control médico multidisciplinario."),
  OBESIDAD_III(
      "Obesidad Grado III (Mórbida)", "Riesgo crítico de salud. Consulta médica urgente requerida.");

  private final String clasificacion;
  private final String recomendacion;

  ClasificacionImc(final String clasificacion, final String recomendacion) {
    this.clasificacion = clasificacion;
    this.recomendacion = recomendacion;
  }

  public String getClasificacion() {
    return clasificacion;
  }

  public String getRecomendacion() {
    return recomendacion;
  }

  public static ClasificacionImc deImc(final double imc) {
    if (imc < 18.5) {
      return BAJO_PESO;
    } else if (imc < 25.0) {
      return NORMAL;
    } else if (imc < 30.0) {
      return SOBREPESO;
    } else if (imc < 35.0) {
      return OBESIDAD_I;
    } else if (imc < 40.0) {
      return OBESIDAD_II;
    }
    return OBESIDAD_III;
  }
}
