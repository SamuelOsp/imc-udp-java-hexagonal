package samuelospina.imc.entrypoint.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GraphicsEnvironment;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Kit de interfaz "liquid glass" pintado a mano con Java2D: fondo de
 * gradientes animados, tarjetas translucidas con brillo especular,
 * botones pildora, campos de vidrio, un control segmentado animado, un
 * medidor por tramos y una bitacora con colores.
 *
 * Sin librerias externas: todo sale de java.awt / javax.swing.
 *
 * @author Samuel David Ospina De Avila
 */
public final class Glass {

    // ------------------------------------------------------------------
    // Paleta
    //
    // Neutros de matiz azul (escala tipo "slate"), fondo oscuro pero
    // nunca negro puro, texto off-white y UN solo acento desaturado.
    // Es la regla 60-30-10: 60% neutro, 30% superficies, 10% acento.
    // Los colores solidos claros (ambar, verde) llevan texto oscuro
    // encima, que es como alcanzan el contraste 4.5:1 de la WCAG.
    // ------------------------------------------------------------------
    public static final Color BASE = new Color(0x0B0F14);        // fondo app
    public static final Color VELO = new Color(0x0E141B);        // superficie
    public static final Color TINTA = new Color(0xE6EDF3);       // texto principal
    public static final Color TINTA_SUAVE = new Color(0x8B98A9); // texto secundario
    public static final Color CALOR = new Color(0xE0A040);       // ACENTO unico (ambar)
    public static final Color FRIO = new Color(0x6C93B8);        // apoyo frio, apagado
    public static final Color VERDE = new Color(0x4E9A6A);       // estado: exito
    public static final Color ROJO = new Color(0xD1615A);        // estado: error
    public static final Color VIOLETA = new Color(0x2A3A5C);     // profundidad del fondo

    /** Texto legible sobre un relleno solido: claro u oscuro segun su luminancia. */
    public static Color sobre(Color fondo) {
        double l = (0.2126 * fondo.getRed() + 0.7152 * fondo.getGreen()
                + 0.0722 * fondo.getBlue()) / 255.0;
        return l > 0.52 ? new Color(0x0B0F14) : TINTA;
    }

    private Glass() {
    }

    /** Primera tipografia disponible de la lista, para verse nativo en cada sistema. */
    public static Font fuente(int estilo, float tam) {
        String[] preferidas = {"SF Pro Display", "SF Pro Text", "Helvetica Neue",
            "Inter", "Segoe UI Variable", "Segoe UI", "Roboto"};
        String[] instaladas = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String p : preferidas) {
            for (String i : instaladas) {
                if (i.equalsIgnoreCase(p)) {
                    return new Font(p, estilo, 12).deriveFont(tam);
                }
            }
        }
        return new Font(Font.SANS_SERIF, estilo, 12).deriveFont(tam);
    }

    private static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    // ------------------------------------------------------------------
    // Fondo: cuatro manchas de color que se desplazan lentamente.
    // Se pintan en una imagen diminuta y se escalan con interpolacion
    // bilineal: eso produce el degradado difuso (y es barato de dibujar).
    // ------------------------------------------------------------------
    public static class Fondo extends JPanel {

        private static final int CHICO_W = 64;
        private static final int CHICO_H = 48;

        private final BufferedImage chico =
                new BufferedImage(CHICO_W, CHICO_H, BufferedImage.TYPE_INT_RGB);
        private float t = 0f;

        public Fondo() {
            setOpaque(true);
            setBackground(BASE);
            Timer reloj = new Timer(60, e -> {
                t += 0.012f;
                repaint();
            });
            reloj.setCoalesce(true);
            reloj.start();
        }

        private void pintarManchas() {
            Graphics2D g = suavizar(chico.getGraphics());
            g.setColor(BASE);
            g.fillRect(0, 0, CHICO_W, CHICO_H);
            // Dos tonos profundos (frio arriba, calido abajo): dan profundidad
            // sin robarle protagonismo al acento ni al contenido.
            mancha(g, VIOLETA, 0.22f + 0.09f * (float) Math.sin(t),
                    0.20f + 0.07f * (float) Math.cos(t * 0.8f), 0.62f, 170);
            mancha(g, new Color(0x16324F), 0.85f + 0.08f * (float) Math.cos(t * 0.7f),
                    0.34f + 0.08f * (float) Math.sin(t * 1.1f), 0.52f, 150);
            mancha(g, new Color(0x3A2A14), 0.74f + 0.09f * (float) Math.sin(t * 0.6f),
                    0.88f + 0.06f * (float) Math.cos(t * 0.9f), 0.50f, 140);
            g.dispose();
        }

        private void mancha(Graphics2D g, Color color, float cx, float cy,
                float radio, int intensidad) {
            float r = radio * CHICO_W;
            Point2D.Float centro = new Point2D.Float(cx * CHICO_W, cy * CHICO_H);
            g.setPaint(new RadialGradientPaint(centro, r,
                    new float[]{0f, 1f},
                    new Color[]{alfa(color, intensidad), alfa(color, 0)},
                    MultipleGradientPaint.CycleMethod.NO_CYCLE));
            g.fill(new Ellipse2D.Float(centro.x - r, centro.y - r, r * 2, r * 2));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            pintarManchas();
            Graphics2D g2 = suavizar(g);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(chico, 0, 0, getWidth(), getHeight(), null);
            // Vinieta para dar profundidad en los bordes
            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Float(getWidth() / 2f, getHeight() / 2f),
                    Math.max(getWidth(), getHeight()) * 0.75f,
                    new float[]{0.55f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 120)},
                    MultipleGradientPaint.CycleMethod.NO_CYCLE));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Tarjeta de vidrio: relleno translucido, borde luminoso, brillo
    // especular arriba y sombra suave debajo.
    // ------------------------------------------------------------------
    public static class Tarjeta extends JPanel {

        private final int radio;

        public Tarjeta() {
            this(28);
        }

        public Tarjeta(int radio) {
            this.radio = radio;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int w = getWidth();
            int h = getHeight();

            // Sombra difusa
            for (int i = 6; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 9));
                g2.fill(new RoundRectangle2D.Float(i, i + 2, w - i * 2f, h - i * 2f,
                        radio + i, radio + i));
            }

            RoundRectangle2D forma =
                    new RoundRectangle2D.Float(0, 0, w - 1f, h - 1f, radio, radio);

            // Velo oscuro: da contraste al texto sobre cualquier fondo
            g2.setColor(new Color(VELO.getRed(), VELO.getGreen(), VELO.getBlue(), 152));
            g2.fill(forma);

            // Material: mas claro arriba que abajo, como el vidrio de Apple
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 26),
                    0, h, new Color(255, 255, 255, 8)));
            g2.fill(forma);

            // Borde
            g2.setStroke(new BasicStroke(1f));
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 78),
                    0, h, new Color(255, 255, 255, 20)));
            g2.draw(forma);

            // Brillo especular del borde superior
            g2.setClip(forma);
            g2.setStroke(new BasicStroke(2f));
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 110),
                    w, 0, new Color(255, 255, 255, 16)));
            g2.drawLine(radio / 2, 1, w - radio / 2, 1);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------
    // Boton pildora con relleno de color, brillo y reaccion al mouse.
    // El color base es getBackground(), asi que setBackground() lo cambia.
    // ------------------------------------------------------------------
    public static class Pildora extends JButton {

        private boolean encima;
        private boolean presionado;

        public Pildora(String texto, Color color) {
            super(texto);
            setBackground(color);
            setForeground(sobre(color));
            setFont(fuente(Font.BOLD, 13f));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(12, 26, 12, 26));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    encima = false;
                    repaint();
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    presionado = true;
                    repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    presionado = false;
                    repaint();
                }
            });
        }

        @Override
        public void setBackground(Color c) {
            super.setBackground(c);
            setForeground(sobre(c));   // el texto sigue siendo legible al cambiar de estado
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int w = getWidth();
            int h = getHeight();
            int r = h;
            Color base = getBackground();

            // Halo del color del boton
            if (encima) {
                g2.setColor(alfa(base, 48));
                g2.fill(new RoundRectangle2D.Float(-3, -1, w + 6f, h + 4f, r, r));
            }

            RoundRectangle2D forma = new RoundRectangle2D.Float(0, 0, w - 1f, h - 1f, r, r);
            Color arriba = presionado ? base.darker() : (encima ? aclarar(base, 34) : aclarar(base, 18));
            Color abajo = presionado ? aclarar(base.darker(), 8) : base;
            g2.setPaint(new GradientPaint(0, 0, arriba, 0, h, abajo));
            g2.fill(forma);

            // Brillo superior tipo vidrio
            g2.setClip(forma);
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 64),
                    0, h * 0.55f, new Color(255, 255, 255, 0)));
            g2.fillRect(0, 0, w, h / 2);
            g2.setClip(null);

            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255, 255, 255, 70));
            g2.draw(forma);

            g2.dispose();
            super.paintComponent(g);
        }

        private Color aclarar(Color c, int d) {
            return new Color(Math.min(255, c.getRed() + d),
                    Math.min(255, c.getGreen() + d),
                    Math.min(255, c.getBlue() + d));
        }
    }

    // ------------------------------------------------------------------
    // Campo de texto de vidrio, con borde que se ilumina al enfocarse.
    // ------------------------------------------------------------------
    public static class Campo extends JTextField {

        private final Color acento;

        public Campo(String valor, Color acento) {
            super(valor);
            this.acento = acento;
            setOpaque(false);
            setForeground(TINTA);
            setCaretColor(acento);
            setSelectionColor(alfa(acento, 90));
            setFont(fuente(Font.PLAIN, 14f));
            setBorder(BorderFactory.createEmptyBorder(11, 15, 11, 15));
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D forma = new RoundRectangle2D.Float(0, 0, w - 1f, h - 1f, 14, 14);
            g2.setColor(new Color(255, 255, 255, isEditable() ? 20 : 10));
            g2.fill(forma);
            g2.setStroke(new BasicStroke(hasFocus() ? 1.6f : 1f));
            g2.setColor(hasFocus() ? alfa(acento, 210) : new Color(255, 255, 255, 48));
            g2.draw(forma);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------
    // Etiqueta tipo "chip": pastilla translucida con texto de color.
    // ------------------------------------------------------------------
    public static class Chip extends JLabel {

        private final Color color;

        public Chip(String texto, Color color) {
            super(texto, SwingConstants.CENTER);
            this.color = color;
            setOpaque(false);
            setForeground(color);
            setFont(fuente(Font.BOLD, 13f));
            setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int h = getHeight();
            RoundRectangle2D forma =
                    new RoundRectangle2D.Float(0, 0, getWidth() - 1f, h - 1f, h, h);
            g2.setColor(alfa(color, 36));
            g2.fill(forma);
            g2.setColor(alfa(color, 105));
            g2.draw(forma);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------
    // Punto de estado: circulo con halo que toma el color de una etiqueta.
    // ------------------------------------------------------------------
    public static class Punto extends JPanel {

        private final JLabel referencia;

        public Punto(JLabel referencia) {
            this.referencia = referencia;
            setOpaque(false);
            setPreferredSize(new Dimension(16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            Color c = referencia.getForeground();
            g2.setColor(alfa(c, 70));
            g2.fill(new Ellipse2D.Float(1, 1, 14, 14));
            g2.setColor(c);
            g2.fill(new Ellipse2D.Float(4.5f, 4.5f, 7, 7));
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Control segmentado (estilo iOS/macOS): el indicador se desliza.
    // ------------------------------------------------------------------
    public static class Segmentado extends JPanel {

        private final String[] opciones;
        private final java.util.function.IntConsumer alCambiar;
        private int seleccion = 0;
        private float x = -1;      // posicion actual del indicador
        private float destino = 0; // posicion objetivo
        private Timer animacion;

        public Segmentado(String[] opciones, java.util.function.IntConsumer alCambiar) {
            this.opciones = opciones;
            this.alCambiar = alCambiar;
            setOpaque(false);
            setFont(fuente(Font.BOLD, 12f));
            setPreferredSize(new Dimension(360, 42));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            animacion = new Timer(16, null);
            animacion.addActionListener(e -> {
                x += (destino - x) * 0.28f;   // suavizado exponencial
                if (Math.abs(destino - x) < 0.4f) {
                    x = destino;
                    animacion.stop();
                }
                repaint();
            });
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    setSeleccion(e.getX() * opciones.length / Math.max(1, getWidth()));
                }
            });
        }

        public final void setSeleccion(int i) {
            if (i < 0 || i >= opciones.length) {
                return;
            }
            seleccion = i;
            destino = i * anchoSegmento();
            if (x < 0) {
                x = destino;
            }
            if (!animacion.isRunning()) {
                animacion.start();
            }
            alCambiar.accept(i);
            repaint();
        }

        private float anchoSegmento() {
            return getWidth() / (float) opciones.length;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int w = getWidth();
            int h = getHeight();
            float seg = anchoSegmento();
            if (x < 0) {
                x = seleccion * seg;
                destino = x;
            }

            RoundRectangle2D pista = new RoundRectangle2D.Float(0, 0, w - 1f, h - 1f, h, h);
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fill(pista);
            g2.setColor(new Color(255, 255, 255, 48));
            g2.draw(pista);

            // Indicador deslizante de vidrio
            RoundRectangle2D ind =
                    new RoundRectangle2D.Float(x + 3, 3, seg - 6, h - 7f, h - 6, h - 6);
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 96),
                    0, h, new Color(255, 255, 255, 46)));
            g2.fill(ind);
            g2.setColor(new Color(255, 255, 255, 150));
            g2.draw(ind);

            // Rotulos
            g2.setFont(getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < opciones.length; i++) {
                String texto = opciones[i];
                int tx = (int) (i * seg + (seg - fm.stringWidth(texto)) / 2);
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(i == seleccion ? BASE : TINTA_SUAVE);
                g2.drawString(texto, tx, ty);
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Medidor por tramos: una escala dividida en segmentos de color con un
    // marcador que se desliza hasta el valor. El tramo activo se ilumina.
    // No decide nada: el tramo lo indica quien lo usa (el dato del servidor).
    // ------------------------------------------------------------------
    public static class Medidor extends JPanel {

        private final float[] cotas;      // cotas.length = tramos + 1
        private final Color[] colores;
        private final String[] etiquetas;
        private int tramo = -1;           // -1 = sin dato
        private float pos = 0f;           // posicion animada del marcador, 0..1
        private float destino = 0f;
        private final Timer animacion;

        /**
         * @param cotas limites de la escala, de menor a mayor; el tramo i va
         *              de cotas[i] a cotas[i + 1]
         */
        public Medidor(float[] cotas, Color[] colores, String[] etiquetas) {
            this.cotas = cotas;
            this.colores = colores;
            this.etiquetas = etiquetas;
            setOpaque(false);
            setFont(fuente(Font.BOLD, 10f));
            setPreferredSize(new Dimension(360, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            animacion = new Timer(16, null);
            animacion.addActionListener(e -> {
                pos += (destino - pos) * 0.18f;   // suavizado exponencial
                if (Math.abs(destino - pos) < 0.001f) {
                    pos = destino;
                    animacion.stop();
                }
                repaint();
            });
        }

        /** Mueve el marcador a {@code valor} dentro del tramo indicado. */
        public void setValor(float valor, int tramoActivo) {
            if (tramoActivo < 0 || tramoActivo >= etiquetas.length) {
                limpiar();
                return;
            }
            tramo = tramoActivo;
            float lo = cotas[tramo];
            float hi = cotas[tramo + 1];
            float dentro = Math.max(0.06f, Math.min(0.94f, (valor - lo) / (hi - lo)));
            destino = (tramo + dentro) / etiquetas.length;
            animacion.start();
        }

        /** Vuelve al estado "sin dato": ningun tramo iluminado. */
        public void limpiar() {
            tramo = -1;
            animacion.stop();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suavizar(g);
            int n = etiquetas.length;
            float seg = getWidth() / (float) n;
            float barraY = 8;
            float barraH = 8;

            g2.setFont(getFont());
            java.awt.FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < n; i++) {
                boolean activo = i == tramo;
                g2.setColor(alfa(colores[i], activo ? 235 : 62));
                g2.fill(new RoundRectangle2D.Float(i * seg + 2, barraY, seg - 4, barraH,
                        barraH, barraH));
                String texto = etiquetas[i].toUpperCase();
                g2.setColor(activo ? TINTA : alfa(TINTA_SUAVE, 170));
                g2.drawString(texto, i * seg + (seg - fm.stringWidth(texto)) / 2,
                        barraY + barraH + 8 + fm.getAscent());
            }

            if (tramo >= 0) {
                float cx = Math.max(9, Math.min(getWidth() - 9, pos * getWidth()));
                float cy = barraY + barraH / 2;
                g2.setColor(alfa(colores[tramo], 80));
                g2.fill(new Ellipse2D.Float(cx - 9, cy - 9, 18, 18));
                g2.setColor(TINTA);
                g2.fill(new Ellipse2D.Float(cx - 5.5f, cy - 5.5f, 11, 11));
                g2.setColor(BASE);
                g2.fill(new Ellipse2D.Float(cx - 2, cy - 2, 4, 4));
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Bitacora: consola de solo lectura que colorea cada linea segun su
    // etiqueta ([SOLICITUD], [CALCULO], [ERROR]...).
    // ------------------------------------------------------------------
    public static class Bitacora extends JTextPane {

        private static final Color NEUTRO = new Color(0x9FB4C7);

        public Bitacora() {
            setEditable(false);
            setOpaque(false);
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            setForeground(NEUTRO);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        }

        /** Agrega una linea: la marca de tiempo va apagada y el resto con color. */
        public void agregar(String marca, String texto) {
            escribir(marca, alfa(TINTA_SUAVE, 170));
            escribir(texto + "\n", colorDe(texto));
            setCaretPosition(getStyledDocument().getLength());
        }

        public void limpiar() {
            setText("");
        }

        private void escribir(String texto, Color color) {
            StyledDocument doc = getStyledDocument();
            SimpleAttributeSet estilo = new SimpleAttributeSet();
            StyleConstants.setForeground(estilo, color);
            try {
                doc.insertString(doc.getLength(), texto, estilo);
            } catch (BadLocationException ex) {
                // No ocurre: siempre se inserta al final del documento.
            }
        }

        private static Color colorDe(String texto) {
            if (texto.startsWith("[ERROR]")) {
                return ROJO;
            } else if (texto.startsWith("[CALCULO]")) {
                return CALOR;
            } else if (texto.startsWith("[SOLICITUD]")) {
                return FRIO;
            } else if (texto.startsWith("[SERVIDOR]") || texto.startsWith("[SISTEMA]")) {
                return VERDE;
            } else if (texto.startsWith("[CONEXION]")) {
                return FRIO;
            }
            return NEUTRO;
        }
    }

    /** Etiqueta pequeña en mayusculas para los rotulos de los campos. */
    public static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, 11f));
        l.setForeground(TINTA_SUAVE);
        l.setPreferredSize(new Dimension(140, 22));
        return l;
    }
}
