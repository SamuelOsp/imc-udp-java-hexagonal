package samuelospina.imc.entrypoint.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import samuelospina.imc.adaptadores.red.ObservadorServidor;
import samuelospina.imc.aplicacion.excepciones.ServidorRedException;
import samuelospina.imc.aplicacion.puertos.entrada.GestionarServidorInputPort;
import samuelospina.imc.dominio.enums.EstadoServidor;
import samuelospina.imc.dominio.excepciones.DominioException;
import samuelospina.imc.dominio.modelos.EventoServidor;

/**
 * Entrypoint GUI del servidor. Solo conoce el puerto de entrada GestionarServidorInputPort para
 * encender/apagar, y recibe los eventos como ObservadorServidor. No toca sockets.
 *
 * @author Samuel David Ospina De Avila
 */
public final class ServidorFrame extends JFrame implements ObservadorServidor {

  private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

  private final transient GestionarServidorInputPort gestionarServidor;

  private CardLayout tarjetas;
  private JPanel contenedor;
  private Glass.Segmentado segmentado;
  private Glass.Pildora btnIniciar;
  private Glass.Campo campoIp;
  private Glass.Campo campoPuerto;
  private JLabel txtEstado;
  private Glass.Bitacora cajaLog;
  private Glass.Chip chipCalculos;
  private int calculos;

  public ServidorFrame(final GestionarServidorInputPort gestionarServidor) {
    this.gestionarServidor = Objects.requireNonNull(gestionarServidor, "El puerto de entrada es obligatorio.");
    initComponents();
    campoIp.setText(ipLocal());
  }

  private void initComponents() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setTitle("Cálculo de IMC · Servidor UDP");

    final Glass.Fondo fondo = new Glass.Fondo();
    fondo.setLayout(new BorderLayout(0, 18));
    fondo.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
    setContentPane(fondo);
    fondo.add(construirCabecera(), BorderLayout.NORTH);

    tarjetas = new CardLayout();
    contenedor = new JPanel(tarjetas);
    contenedor.setOpaque(false);
    contenedor.add(construirPanelConexion(), "conexion");
    contenedor.add(construirPanelLog(), "log");

    segmentado = new Glass.Segmentado(
        new String[] {"CONEXION", "LOG DE PETICIONES"},
        i -> tarjetas.show(contenedor, i == 0 ? "conexion" : "log"));

    final JPanel centro = new JPanel(new BorderLayout(0, 16));
    centro.setOpaque(false);
    centro.add(segmentado, BorderLayout.NORTH);
    centro.add(contenedor, BorderLayout.CENTER);
    fondo.add(centro, BorderLayout.CENTER);

    setMinimumSize(new Dimension(700, 560));
    setSize(getMinimumSize());
    setLocationRelativeTo(null);
  }

  private JPanel construirCabecera() {
    final JPanel cabecera = new JPanel(new BorderLayout());
    cabecera.setOpaque(false);

    final JPanel titulos = new JPanel();
    titulos.setOpaque(false);
    titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));

    final JLabel titulo = new JLabel("Servidor de IMC");
    titulo.setFont(Glass.fuente(Font.BOLD, 27f));
    titulo.setForeground(Glass.TINTA);

    final JLabel subtitulo = new JLabel("DatagramSocket UDP  ·  Arquitectura Hexagonal  ·  Ejercicio del IMC");
    subtitulo.setFont(Glass.fuente(Font.PLAIN, 12f));
    subtitulo.setForeground(Glass.TINTA_SUAVE);

    titulos.add(titulo);
    titulos.add(Box.createVerticalStrut(5));
    titulos.add(subtitulo);
    cabecera.add(titulos, BorderLayout.WEST);

    final JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    derecha.setOpaque(false);
    chipCalculos = new Glass.Chip("0 cálculos", Glass.FRIO);
    derecha.add(chipCalculos);
    derecha.add(new Glass.Chip("UDP", Glass.CALOR));
    cabecera.add(derecha, BorderLayout.EAST);
    return cabecera;
  }

  private JPanel construirPanelConexion() {
    final Glass.Tarjeta panel = new Glass.Tarjeta();
    panel.setLayout(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

    final GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(9, 0, 9, 12);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;

    campoIp = new Glass.Campo("", Glass.CALOR);
    campoIp.setEditable(false);
    campoPuerto = new Glass.Campo("9007", Glass.CALOR);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    panel.add(Glass.rotulo("DIRECCION IP"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(campoIp, c);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0;
    panel.add(Glass.rotulo("PUERTO UDP"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(campoPuerto, c);

    btnIniciar = new Glass.Pildora("INICIAR", Glass.VERDE);
    btnIniciar.addActionListener(evt -> alternarServidor());
    c.gridx = 1;
    c.gridy = 2;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(16, 0, 9, 0);
    panel.add(btnIniciar, c);

    txtEstado = new JLabel("DETENIDO");
    txtEstado.setFont(Glass.fuente(Font.BOLD, 13f));
    txtEstado.setForeground(Glass.ROJO);

    final JPanel estado = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
    estado.setOpaque(false);
    estado.add(new Glass.Punto(txtEstado));
    estado.add(txtEstado);

    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 2;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(10, 0, 0, 0);
    panel.add(estado, c);

    final JLabel ayuda = new JLabel("<html><div style='width:450px'>Un único DatagramSocket atiende a "
        + "todos los clientes: no hay conexión ni un hilo por cliente. Cada datagrama trae la IP y el "
        + "puerto de quien lo envió, y a esa dirección se devuelve la respuesta.</div></html>");
    ayuda.setFont(Glass.fuente(Font.PLAIN, 11f));
    ayuda.setForeground(Glass.TINTA_SUAVE);
    c.gridy = 4;
    c.weighty = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    panel.add(ayuda, c);
    return panel;
  }

  private JPanel construirPanelLog() {
    final Glass.Tarjeta panel = new Glass.Tarjeta();
    panel.setLayout(new BorderLayout(0, 14));
    panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

    cajaLog = new Glass.Bitacora();

    final JScrollPane scroll = new JScrollPane(cajaLog);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // La consola va dentro de su propia lámina de vidrio, más oscura
    final Glass.Tarjeta consola = new Glass.Tarjeta(18) {
      @Override
      protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(6, 11, 25, 190));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.dispose();
      }
    };
    consola.setLayout(new BorderLayout());
    consola.add(scroll, BorderLayout.CENTER);
    panel.add(consola, BorderLayout.CENTER);

    final Glass.Pildora btnLimpiar = new Glass.Pildora("LIMPIAR", new Color(0x263140));
    btnLimpiar.addActionListener(evt -> cajaLog.limpiar());
    final JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    pie.setOpaque(false);
    pie.add(btnLimpiar);
    panel.add(pie, BorderLayout.SOUTH);
    return panel;
  }

  // ------------------------------------------------------------------
  // Interacción con el núcleo: solo a través del puerto de entrada
  // ------------------------------------------------------------------
  private void alternarServidor() {
    if (gestionarServidor.estaActivo()) {
      gestionarServidor.detener();
      return;
    }
    final int puerto;
    try {
      puerto = Integer.parseInt(campoPuerto.getText().trim());
    } catch (final NumberFormatException excepcion) {
      advertir("El PUERTO UDP debe ser un número entero.");
      return;
    }
    try {
      gestionarServidor.iniciar(puerto);
      segmentado.setSeleccion(1);
    } catch (final DominioException | ServidorRedException excepcion) {
      advertir(excepcion.getMessage());
    }
  }

  private void advertir(final String mensaje) {
    JOptionPane.showMessageDialog(this, mensaje, "Servidor UDP", JOptionPane.WARNING_MESSAGE);
  }

  /**
   * IPv4 de la red local por la que otros equipos alcanzan este servidor. InetAddress.getLocalHost()
   * suele devolver 127.0.0.1, que solo sirve dentro del mismo computador.
   */
  private static String ipLocal() {
    try {
      final Enumeration<NetworkInterface> redes = NetworkInterface.getNetworkInterfaces();
      while (Objects.nonNull(redes) && redes.hasMoreElements()) {
        final NetworkInterface red = redes.nextElement();
        if (!red.isUp() || red.isLoopback() || red.isVirtual()) {
          continue;
        }
        final Enumeration<InetAddress> direcciones = red.getInetAddresses();
        while (direcciones.hasMoreElements()) {
          final InetAddress direccion = direcciones.nextElement();
          if (direccion instanceof Inet4Address && direccion.isSiteLocalAddress()) {
            return direccion.getHostAddress();
          }
        }
      }
      return InetAddress.getLocalHost().getHostAddress();
    } catch (final IOException excepcion) {
      return "127.0.0.1";
    }
  }

  // ------------------------------------------------------------------
  // ObservadorServidor: llega desde el hilo receptor, se pinta en el EDT
  // ------------------------------------------------------------------
  @Override
  public void alRecibirEvento(final EventoServidor evento) {
    final String marca = evento.hora().format(HORA) + "  ";
    final String texto = "[" + evento.tipo() + "] " + evento.origen() + "  " + evento.descripcion();
    SwingUtilities.invokeLater(() -> {
      cajaLog.agregar(marca, texto);
      if ("CALCULO".equals(evento.tipo())) {
        calculos++;
        chipCalculos.setText(calculos + (calculos == 1 ? " cálculo" : " cálculos"));
      }
    });
  }

  @Override
  public void alCambiarEstado(final EstadoServidor estado) {
    SwingUtilities.invokeLater(() -> {
      final boolean escuchando = estado == EstadoServidor.ESCUCHANDO;
      btnIniciar.setText(escuchando ? "DETENER" : "INICIAR");
      btnIniciar.setBackground(escuchando ? Glass.ROJO : Glass.VERDE);
      campoPuerto.setEditable(!escuchando);
      txtEstado.setText(escuchando ? "ESCUCHANDO · UDP " + campoPuerto.getText().trim() : "DETENIDO");
      txtEstado.setForeground(escuchando ? Glass.VERDE : Glass.ROJO);
      repaint();
    });
  }
}
