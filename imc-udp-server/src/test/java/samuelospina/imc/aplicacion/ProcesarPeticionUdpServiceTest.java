package samuelospina.imc.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import samuelospina.imc.adaptadores.red.mapper.UdpNetworkMapper;
import samuelospina.imc.aplicacion.dto.ProcesarPeticionUdpCommand;
import samuelospina.imc.aplicacion.mapper.PeticionMapper;
import samuelospina.imc.aplicacion.servicios.ProcesarPeticionUdpService;
import samuelospina.imc.dominio.enums.EstadoServidor;
import samuelospina.imc.dominio.modelos.EventoServidor;
import samuelospina.imc.dominio.puertos.salida.PuertoNotificacionEvento;

/** El caso de uso se prueba sin red: los puertos de salida se reemplazan por dobles en memoria. */
class ProcesarPeticionUdpServiceTest {

  private static final String SOBREPESO =
      "OK_CALCULO;26.99;Sobrepeso (Preobesidad);"
          + "Se aconseja incrementar la actividad física y moderar el consumo calórico.";

  private final List<String> enviados = new ArrayList<>();
  private final List<EventoServidor> eventos = new ArrayList<>();
  private ProcesarPeticionUdpService servicio;

  @BeforeEach
  void preparar() {
    final UdpNetworkMapper mapper = new UdpNetworkMapper();
    final PuertoNotificacionEvento notificador = new PuertoNotificacionEvento() {
      @Override
      public void notificarEvento(final EventoServidor evento) {
        eventos.add(evento);
      }

      @Override
      public void notificarEstado(final EstadoServidor estado) {}
    };
    servicio = new ProcesarPeticionUdpService(
        r -> enviados.add(mapper.toNetworkResponse(r).payload()), notificador, new PeticionMapper());
  }

  private String enviar(final String mensaje) {
    servicio.procesar(new ProcesarPeticionUdpCommand("127.0.0.1", 50000, mensaje));
    return enviados.get(enviados.size() - 1);
  }

  @Test
  void conectarYDesconectar() {
    assertEquals("CONECTADO_OK;Servidor UDP listo para calcular IMC", enviar("conectar"));
    assertEquals("DESCONECTADO_OK;Sesión finalizada", enviar("DESCONECTAR"));
    assertEquals(2, eventos.size());
    assertEquals("CONEXION", eventos.get(0).tipo());
  }

  @Test
  void calcula() {
    assertEquals(SOBREPESO, enviar("CALCULAR;78;1.70"));
    assertEquals(SOBREPESO, enviar("calcular;78,00;1,70"));
    assertEquals(SOBREPESO, enviar("CALCULAR;78;170"));
    assertEquals("CALCULO", eventos.get(0).tipo());
  }

  @Test
  void respondeErrores() {
    assertEquals("ERROR;El peso y la altura deben ser numéricos.", enviar("CALCULAR;abc;1.70"));
    assertEquals("ERROR;Formato inválido. Se esperaba CALCULAR;peso;altura", enviar("CALCULAR;70"));
    assertEquals("ERROR;Peso incorrecto: debe ser mayor que 0", enviar("CALCULAR;0;1.70"));
    assertEquals("ERROR;Altura incorrecta: debe ser mayor que 0", enviar("CALCULAR;70;-1"));
    assertEquals("ERROR;Comando no reconocido por el servidor UDP.", enviar("HOLA"));
    assertEquals("ERROR;Mensaje vacío recibido.", enviar("   "));
    assertEquals("ERROR", eventos.get(0).tipo());
  }
}
