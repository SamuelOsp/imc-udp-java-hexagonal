package samuelospina.imc.aplicacion.servicios;

import java.util.Locale;
import java.util.Objects;
import samuelospina.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import samuelospina.imc.aplicacion.mapper.PeticionMapper;
import samuelospina.imc.aplicacion.puertos.entrada.ProcesarPeticionUdpInputPort;
import samuelospina.imc.dominio.excepciones.DominioException;
import samuelospina.imc.dominio.modelos.Calculo;
import samuelospina.imc.dominio.modelos.EventoServidor;
import samuelospina.imc.dominio.modelos.RespuestaCliente;
import samuelospina.imc.dominio.modelos.Resultado;
import samuelospina.imc.dominio.puertos.salida.PuertoNotificacionEvento;
import samuelospina.imc.dominio.puertos.salida.PuertoSalidaRed;
import samuelospina.imc.dominio.vo.Altura;
import samuelospina.imc.dominio.vo.Destinatario;
import samuelospina.imc.dominio.vo.Peso;

/**
 * Caso de uso principal. Interpreta el protocolo de texto (CONECTAR, DESCONECTAR,
 * CALCULAR;peso;altura), delega el cálculo al dominio y responde por el puerto de salida.
 */
public final class ProcesarPeticionUdpService implements ProcesarPeticionUdpInputPort {

  private final PuertoSalidaRed puertoSalidaRed;
  private final PuertoNotificacionEvento notificador;
  private final PeticionMapper peticionMapper;

  public ProcesarPeticionUdpService(
      final PuertoSalidaRed puertoSalidaRed,
      final PuertoNotificacionEvento notificador,
      final PeticionMapper peticionMapper) {
    this.puertoSalidaRed = Objects.requireNonNull(puertoSalidaRed, "El puerto de salida es obligatorio.");
    this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
    this.peticionMapper = Objects.requireNonNull(peticionMapper, "El mapper es obligatorio.");
  }

  @Override
  public void procesar(final ProcesarPeticionUdpCommand comando) {
    Objects.requireNonNull(comando, "El comando es obligatorio.");
    final Destinatario destinatario = peticionMapper.toDestinatario(comando);
    final String texto = Objects.isNull(comando.mensaje()) ? "" : comando.mensaje().trim();
    final String orden = texto.toUpperCase(Locale.ROOT);

    if (orden.isEmpty()) {
      responderError(destinatario, "Mensaje vacío recibido.", "datos recibidos --> [vacío]");
    } else if (orden.equals("CONECTAR")) {
      puertoSalidaRed.enviarRespuesta(
          RespuestaCliente.conectado(destinatario, "Servidor UDP listo para calcular IMC"));
      notificar("CONEXION", destinatario, "CONECTAR --> CONECTADO_OK");
    } else if (orden.equals("DESCONECTAR")) {
      puertoSalidaRed.enviarRespuesta(RespuestaCliente.desconectado(destinatario, "Sesión finalizada"));
      notificar("CONEXION", destinatario, "DESCONECTAR --> sesión finalizada");
    } else if (orden.startsWith("CALCULAR;")) {
      procesarCalculo(destinatario, texto);
    } else {
      responderError(
          destinatario,
          "Comando no reconocido por el servidor UDP.",
          "datos recibidos --> comando desconocido [" + texto + "]");
    }
  }

  private void procesarCalculo(final Destinatario destinatario, final String texto) {
    final String[] partes = texto.split(";", -1);
    if (partes.length != 3) {
      responderError(
          destinatario,
          "Formato inválido. Se esperaba CALCULAR;peso;altura",
          "datos recibidos --> formato incorrecto [" + texto + "]");
      return;
    }
    try {
      final Peso peso = new Peso(Double.parseDouble(partes[1].trim().replace(',', '.')));
      final Altura altura = new Altura(Double.parseDouble(partes[2].trim().replace(',', '.')));
      final Resultado resultado = new Calculo(peso, altura).calcular();
      puertoSalidaRed.enviarRespuesta(RespuestaCliente.calculoExitoso(destinatario, resultado));
      notificar(
          "CALCULO",
          destinatario,
          String.format(
              Locale.US,
              "recibido %.2f kg y %.2f m --> enviado IMC %s (%s)",
              peso.valor(),
              altura.valorEnMetros(),
              resultado.imcFormateado(),
              resultado.clasificacion().getClasificacion()));
    } catch (final NumberFormatException excepcion) {
      responderError(
          destinatario,
          "El peso y la altura deben ser numéricos.",
          "datos recibidos --> no numérico [" + texto + "]");
    } catch (final DominioException excepcion) {
      responderError(
          destinatario,
          excepcion.getMessage(),
          "datos recibidos --> validación fallida: " + excepcion.getMessage());
    }
  }

  private void responderError(
      final Destinatario destinatario, final String mensaje, final String descripcionLog) {
    puertoSalidaRed.enviarRespuesta(RespuestaCliente.error(destinatario, mensaje));
    notificar("ERROR", destinatario, descripcionLog);
  }

  private void notificar(final String tipo, final Destinatario destinatario, final String descripcion) {
    notificador.notificarEvento(new EventoServidor(tipo, destinatario.endpoint(), descripcion));
  }
}
