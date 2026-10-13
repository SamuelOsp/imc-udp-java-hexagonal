package samuelospina.imc.adaptadores.red.response;

/** Lo que viaja por la red: el texto del datagrama y a dónde va. */
public record UdpResponse(String payload, String ip, int puerto) {}
