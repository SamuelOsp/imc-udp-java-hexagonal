package samuelospina.imc.dominio.puertos.salida;

import samuelospina.imc.dominio.modelos.RespuestaCliente;

/** Puerto de salida: cómo el núcleo le devuelve una respuesta al cliente (sin saber que es UDP). */
public interface PuertoSalidaRed {

  void enviarRespuesta(RespuestaCliente respuesta);
}
