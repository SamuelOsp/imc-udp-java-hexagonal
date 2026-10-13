package samuelospina.imc.cliente.dominio.modelos;

/** Lo que el servidor devolvió: el IMC, su clasificación y la recomendación. */
public record ResultadoImc(double imc, String clasificacion, String recomendacion) {}
