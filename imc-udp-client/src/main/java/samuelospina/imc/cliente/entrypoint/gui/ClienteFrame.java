package samuelospina.imc.cliente.entrypoint.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import samuelospina.imc.cliente.aplicacion.dto.CalcularImcCommand;
import samuelospina.imc.cliente.aplicacion.dto.ConectarCommand;
import samuelospina.imc.cliente.aplicacion.puertos.entrada.CalcularImcInputPort;
import samuelospina.imc.cliente.aplicacion.puertos.entrada.GestionarConexionInputPort;
import samuelospina.imc.cliente.dominio.modelos.ResultadoImc;

/**
 * Entrypoint GUI del cliente. Solo habla con los puertos de entrada; no sabe que existe UDP. Las
 * llamadas de red corren fuera del hilo de Swing para que la ventana no se congele mientras se
 * espera un datagrama.
 *
 * @author Samuel David Ospina De Avila
 */
public final class ClienteFrame extends JFrame {

  private final transient GestionarConexionInputPort conexion;
  private final transient CalcularImcInputPort calculadora;

  /** Clasificaciones que devuelve el servidor, en el orden de los tramos del medidor. */
  private static final String[] CLASES = {"Bajo peso", "Peso normal", "Sobrepeso",
      "Obesidad Grado I ", "Obesidad Grado II ", "Obesidad Grado III "};
  private static final int MAX_HISTORIAL = 2;

  private CardLayout tarjetas;
  private JPanel contenedor;
  private Glass.Segmentado segmentado;
  private Glass.Pildora btnConectar;
  private Glass.Pildora btnCalcular;
  private Glass.Campo campoHost;
  private Glass.Campo campoPuerto;
  private Glass.Campo campoPeso;
  private Glass.Campo campoAltura;
  private Glass.Medidor medidor;
  private JPanel historial;
  private JLabel txtLatencia;
  private JLabel txtEstado;
  private JLabel txtResultado;
  private JLabel txtMensaje;
  private float ultimoValor;
  private Timer contador;

  public ClienteFrame(
      final GestionarConexionInputPort conexion, final CalcularImcInputPort calculadora) {
    this.conexion = Objects.requireNonNull(conexion, "El puerto de conexión es obligatorio.");
    this.calculadora = Objects.requireNonNull(calculadora, "El puerto de cálculo es obligatorio.");
    initComponents();
  }

  private void initComponents() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setTitle("Cálculo de IMC · Cliente UDP");
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        if (conexion.estaConectado()) {
          try {
            conexion.desconectar();
          } catch (final RuntimeException ignorada) {
            // se está cerrando la ventana: no hay a quién avisar
          }
        }
      }
    });

    final Glass.Fondo fondo = new Glass.Fondo();
    fondo.setLayout(new BorderLayout(0, 18));
    fondo.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
    setContentPane(fondo);
    fondo.add(construirCabecera(), BorderLayout.NORTH);

    tarjetas = new CardLayout();
    contenedor = new JPanel(tarjetas);
    contenedor.setOpaque(false);
    contenedor.add(construirPanelConexion(), "conexion");
    contenedor.add(construirPanelCalculo(), "calcular");

    segmentado = new Glass.Segmentado(new String[] {"CONEXION", "CALCULAR IMC"},
        i -> tarjetas.show(contenedor, i == 0 ? "conexion" : "calcular"));

    final JPanel centro = new JPanel(new BorderLayout(0, 16));
    centro.setOpaque(false);
    centro.add(segmentado, BorderLayout.NORTH);
    centro.add(contenedor, BorderLayout.CENTER);
    fondo.add(centro, BorderLayout.CENTER);

    setMinimumSize(new Dimension(760, 660));
    setSize(getMinimumSize());
    setLocationRelativeTo(null);
  }

  private JPanel construirCabecera() {
    final JPanel cabecera = new JPanel(new BorderLayout());
    cabecera.setOpaque(false);

    final JPanel titulos = new JPanel();
    titulos.setOpaque(false);
    titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));

    final JLabel titulo = new JLabel("Índice de Masa Corporal");
    titulo.setFont(Glass.fuente(Font.BOLD, 27f));
    titulo.setForeground(Glass.TINTA);

    final JLabel subtitulo = new JLabel("Cliente UDP  ·  Arquitectura Hexagonal  ·  Samuel Ospina");
    subtitulo.setFont(Glass.fuente(Font.PLAIN, 12f));
    subtitulo.setForeground(Glass.TINTA_SUAVE);

    titulos.add(titulo);
    titulos.add(Box.createVerticalStrut(5));
    titulos.add(subtitulo);
    cabecera.add(titulos, BorderLayout.WEST);

    final JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    derecha.setOpaque(false);
    derecha.add(new Glass.Chip("UDP", Glass.CALOR));
    derecha.add(new Glass.Chip("kg / m²", Glass.FRIO));
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

    campoHost = new Glass.Campo("localhost", Glass.FRIO);
    campoPuerto = new Glass.Campo("9007", Glass.FRIO);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    panel.add(Glass.rotulo("DIRECCION IP"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(campoHost, c);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0;
    panel.add(Glass.rotulo("PUERTO UDP"), c);
    c.gridx = 1;
    c.weightx = 1;
    panel.add(campoPuerto, c);

    btnConectar = new Glass.Pildora("Conectar", Glass.VERDE);
    btnConectar.addActionListener(evt -> alternarConexion());
    c.gridx = 1;
    c.gridy = 2;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(16, 0, 9, 0);
    panel.add(btnConectar, c);

    txtEstado = new JLabel("Desconectado");
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

    final JLabel ayuda = new JLabel("<html><div style='width:440px'>UDP no establece conexión: "
        + "\"Conectar\" envía un datagrama CONECTAR y espera CONECTADO_OK (máx. 3 s) para comprobar "
        + "que el servidor está escuchando.</div></html>");
    ayuda.setFont(Glass.fuente(Font.PLAIN, 11f));
    ayuda.setForeground(Glass.TINTA_SUAVE);
    c.gridy = 4;
    c.weighty = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    panel.add(ayuda, c);
    return panel;
  }

  private JPanel construirPanelCalculo() {
    final Glass.Tarjeta panel = new Glass.Tarjeta();
    panel.setLayout(new BorderLayout(0, 22));
    panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));

    final JPanel entrada = new JPanel(new GridBagLayout());
    entrada.setOpaque(false);
    final GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    campoPeso = campoGrande();
    campoAltura = campoGrande();
    campoPeso.addActionListener(evt -> campoAltura.requestFocusInWindow());
    campoAltura.addActionListener(evt -> calcular());

    c.insets = new Insets(0, 0, 0, 10);
    entrada.add(etiquetaMenor("PESO (KG)"), c);
    c.gridx = 1;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 18);
    entrada.add(campoPeso, c);

    c.gridx = 2;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, 0, 0, 10);
    entrada.add(etiquetaMenor("ALTURA (M)"), c);
    c.gridx = 3;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 18);
    entrada.add(campoAltura, c);

    btnCalcular = new Glass.Pildora("CALCULAR", Glass.CALOR);
    btnCalcular.addActionListener(evt -> calcular());
    c.gridx = 4;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, 0, 0, 0);
    entrada.add(btnCalcular, c);
    panel.add(entrada, BorderLayout.NORTH);

    final JPanel resultado = new JPanel();
    resultado.setOpaque(false);
    resultado.setLayout(new BoxLayout(resultado, BoxLayout.Y_AXIS));

    final JLabel rotulo = etiquetaMenor("INDICE DE MASA CORPORAL");
    rotulo.setAlignmentX(LEFT_ALIGNMENT);

    txtResultado = new JLabel("0.00");
    txtResultado.setFont(Glass.fuente(Font.BOLD, 52f));
    txtResultado.setForeground(Glass.CALOR);
    txtResultado.setAlignmentX(LEFT_ALIGNMENT);

    txtMensaje = new JLabel("Conecte con el servidor y escriba el peso y la altura.");
    txtMensaje.setFont(Glass.fuente(Font.PLAIN, 13f));
    txtMensaje.setForeground(Glass.TINTA);
    txtMensaje.setAlignmentX(LEFT_ALIGNMENT);

    // Escala visual: solo ubica el IMC en el rango que informó el servidor
    medidor = new Glass.Medidor(
        new float[] {10f, 18.5f, 25f, 30f, 35f, 40f, 50f},
        new Color[] {Glass.FRIO, Glass.VERDE, Glass.CALOR, new Color(0xD98A4A), Glass.ROJO,
            new Color(0xB0473F)},
        new String[] {"bajo", "normal", "sobrepeso", "obes. I", "obes. II", "obes. III"});
    medidor.setAlignmentX(LEFT_ALIGNMENT);

    resultado.add(rotulo);
    resultado.add(Box.createVerticalStrut(8));
    resultado.add(txtResultado);
    resultado.add(Box.createVerticalStrut(10));
    resultado.add(txtMensaje);
    resultado.add(Box.createVerticalStrut(18));
    resultado.add(medidor);
    resultado.add(Box.createVerticalStrut(18));

    // Accesos rápidos: solo rellenan los campos, el cálculo sigue siendo del servidor
    final JPanel rapidos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    rapidos.setOpaque(false);
    rapidos.setAlignmentX(LEFT_ALIGNMENT);
    rapidos.add(etiquetaMenor("PRUEBE CON  "));
    for (final String[] v : new String[][] {{"50", "1.75"}, {"65", "1.70"}, {"78", "1.70"}, {"110", "165"}}) {
      final Glass.Chip chip = new Glass.Chip(v[0] + " kg · " + v[1] + (v[1].contains(".") ? " m" : " cm"),
          Glass.FRIO);
      chip.setFont(Glass.fuente(Font.BOLD, 11f));
      chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      chip.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(final MouseEvent e) {
          campoPeso.setText(v[0]);
          campoAltura.setText(v[1]);
          campoAltura.requestFocusInWindow();
        }
      });
      rapidos.add(chip);
    }
    resultado.add(rapidos);
    panel.add(resultado, BorderLayout.CENTER);

    // Pie: historial de cálculos y tiempo de ida y vuelta del último datagrama
    historial = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    historial.setOpaque(false);
    historial.add(etiquetaMenor("HISTORIAL  "));
    txtLatencia = etiquetaMenor("");
    txtLatencia.setHorizontalAlignment(SwingConstants.RIGHT);

    final JPanel pie = new JPanel(new BorderLayout());
    pie.setOpaque(false);
    pie.add(historial, BorderLayout.CENTER);
    pie.add(txtLatencia, BorderLayout.EAST);
    panel.add(pie, BorderLayout.SOUTH);
    return panel;
  }

  private Glass.Campo campoGrande() {
    final Glass.Campo campo = new Glass.Campo("", Glass.FRIO);
    campo.setFont(Glass.fuente(Font.BOLD, 22f));
    campo.setHorizontalAlignment(SwingConstants.CENTER);
    campo.setPreferredSize(new Dimension(110, 50));
    return campo;
  }

  private JLabel etiquetaMenor(final String texto) {
    final JLabel etiqueta = new JLabel(texto);
    etiqueta.setFont(Glass.fuente(Font.BOLD, 11f));
    etiqueta.setForeground(Glass.TINTA_SUAVE);
    return etiqueta;
  }

  // ------------------------------------------------------------------
  // Acciones: siempre a través de los puertos de entrada
  // ------------------------------------------------------------------
  private void alternarConexion() {
    if (conexion.estaConectado()) {
      enSegundoPlano(() -> {
        conexion.desconectar();
        return null;
      }, nada -> pintarConexion(false));
      return;
    }
    final ConectarCommand comando = new ConectarCommand(campoHost.getText(), campoPuerto.getText());
    btnConectar.setEnabled(false);
    txtEstado.setText("Enviando CONECTAR...");
    enSegundoPlano(() -> {
      conexion.conectar(comando);
      return null;
    }, nada -> {
      pintarConexion(true);
      segmentado.setSeleccion(1);
    });
  }

  private void calcular() {
    final CalcularImcCommand comando = new CalcularImcCommand(campoPeso.getText(), campoAltura.getText());
    btnCalcular.setEnabled(false);
    final long inicio = System.nanoTime();
    enSegundoPlano(
        () -> calculadora.calcular(comando),
        resultado -> mostrarResultado(comando, resultado, (System.nanoTime() - inicio) / 1_000_000));
  }

  private void mostrarResultado(
      final CalcularImcCommand comando, final ResultadoImc resultado, final long milisegundos) {
    final float imc = (float) resultado.imc();
    txtResultado.setForeground(Glass.CALOR);
    animarResultado(ultimoValor, imc);
    ultimoValor = imc;
    txtMensaje.setText("<html><b>" + resultado.clasificacion() + "</b><br>" + resultado.recomendacion()
        + "</html>");
    medidor.setValor(imc, tramoDe(resultado.clasificacion()));
    agregarAlHistorial(String.format(Locale.US, "%s kg · %s → %.2f",
        comando.peso().trim(), comando.altura().trim(), resultado.imc()));
    txtLatencia.setText("IDA Y VUELTA  " + milisegundos + " ms");
  }

  /** Tramo del medidor según la clasificación que envió el servidor (-1 si no se reconoce). */
  private static int tramoDe(final String clasificacion) {
    final String texto = Objects.toString(clasificacion, "") + " ";
    for (int i = 0; i < CLASES.length; i++) {
      if (texto.startsWith(CLASES[i])) {
        return i;
      }
    }
    return -1;
  }

  /** Agrega el cálculo al historial, conservando solo los más recientes. */
  private void agregarAlHistorial(final String texto) {
    final Glass.Chip chip = new Glass.Chip(texto, Glass.CALOR);
    chip.setFont(Glass.fuente(Font.BOLD, 11f));
    historial.add(chip, 1); // después del rótulo: el más reciente primero
    while (historial.getComponentCount() > MAX_HISTORIAL + 1) {
      historial.remove(historial.getComponentCount() - 1);
    }
    historial.revalidate();
    historial.repaint();
  }

  private void pintarConexion(final boolean conectado) {
    btnConectar.setText(conectado ? "Desconectar" : "Conectar");
    btnConectar.setBackground(conectado ? Glass.ROJO : Glass.VERDE);
    txtEstado.setText(conectado
        ? "Servidor UDP responde en " + campoHost.getText().trim() + ":" + campoPuerto.getText().trim()
        : "Desconectado");
    txtEstado.setForeground(conectado ? Glass.VERDE : Glass.ROJO);
    repaint();
  }

  /** Ejecuta la llamada de red en otro hilo y vuelve al EDT con el resultado o el error. */
  private <T> void enSegundoPlano(final Supplier<T> tarea, final Consumer<T> alTerminar) {
    new Thread(() -> {
      try {
        final T valor = tarea.get();
        SwingUtilities.invokeLater(() -> {
          reactivarBotones();
          alTerminar.accept(valor);
        });
      } catch (final RuntimeException excepcion) {
        SwingUtilities.invokeLater(() -> {
          reactivarBotones();
          pintarConexion(conexion.estaConectado());
          JOptionPane.showMessageDialog(this, excepcion.getMessage(), "Cliente UDP",
              JOptionPane.WARNING_MESSAGE);
        });
      }
    }, "Cliente-UDP").start();
  }

  private void reactivarBotones() {
    btnConectar.setEnabled(true);
    btnCalcular.setEnabled(true);
  }

  /** Animación del contador: interpola del valor anterior al nuevo (ease-out cúbico). */
  private void animarResultado(final float desde, final float hasta) {
    if (contador != null && contador.isRunning()) {
      contador.stop();
    }
    final long inicio = System.currentTimeMillis();
    final int duracion = 420;
    contador = new Timer(16, null);
    contador.addActionListener(e -> {
      final float p = Math.min(1f, (System.currentTimeMillis() - inicio) / (float) duracion);
      final float suave = 1 - (1 - p) * (1 - p) * (1 - p);
      txtResultado.setText(String.format(Locale.US, "%.2f", desde + (hasta - desde) * suave));
      if (p >= 1f) {
        contador.stop();
      }
    });
    contador.start();
  }
}
