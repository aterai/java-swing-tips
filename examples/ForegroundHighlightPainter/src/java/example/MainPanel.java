// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;

public final class MainPanel extends JPanel {
  private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private static final String ECHO_CHAR = "PasswordField.echoChar";

  private MainPanel() {
    super(new BorderLayout());
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    JPanel p = new JPanel(new GridLayout(0, 1, 5, 25));
    JPanel p1 = makeTextField();
    p.add(makeTitledPanel("JTextField + HighlightFilter", p1));
    JPanel p2 = makePasswordPanel();
    p.add(makeTitledPanel("JPasswordField + HighlightFilter", p2));
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(25, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private JPanel makeTextField() {
    String txt = "The quick brown fox jumps over the lazy dog.";
    JTextField field = new JTextField(txt) {
      @Override public void updateUI() {
        super.updateUI();
        Color fg = UIManager.getColor("TextField.foreground");
        setForeground(new Color(0x0, true));
        setSelectedTextColor(new Color(0x0, true));
        Highlighter.HighlightPainter painter0 = new ForegroundPainter(fg);
        Highlighter.HighlightPainter painter1 = new ForegroundPainter(Color.RED);
        Highlighter highlighter = getHighlighter();
        highlighter.removeAllHighlights();
        try {
          highlighter.addHighlight(txt.indexOf("quick"), txt.indexOf("brown"), painter1);
          highlighter.addHighlight(0, txt.length(), painter0);
        } catch (BadLocationException ex) {
          Logger.getGlobal().severe(ex::getMessage);
        }
      }
    };
    JPanel p = new OverlayLayoutPanel();
    p.add(field);
    return p;
  }

  private static JPanel makePasswordPanel() {
    JPasswordField password = new DigitHighlightPasswordField(40);
    password.setFont(FONT);
    password.setAlignmentX(RIGHT_ALIGNMENT);
    password.setText("!1l2c$%34e5&6#7=8g9O0");

    AbstractButton button = new JToggleButton();
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      password.setEchoChar(b ? '\u0000' : (Character) UIManager.get(ECHO_CHAR));
    });
    initEyeButton(button);
    JPanel p = new OverlayLayoutPanel();
    p.add(button);
    p.add(password);
    return p;
  }

  private static void initEyeButton(AbstractButton b) {
    b.setFocusable(false);
    b.setOpaque(false);
    b.setContentAreaFilled(false);
    b.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
    b.setAlignmentX(RIGHT_ALIGNMENT);
    b.setAlignmentY(CENTER_ALIGNMENT);
    b.setIcon(new EyeIcon(Color.BLUE));
    b.setRolloverIcon(new EyeIcon(Color.DARK_GRAY));
    b.setSelectedIcon(new EyeIcon(Color.BLUE));
    b.setRolloverSelectedIcon(new EyeIcon(Color.BLUE));
    b.setToolTipText("show/hide passwords");
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class DigitHighlightPasswordField extends JPasswordField {
  protected DigitHighlightPasswordField(int columns) {
    super(columns);
  }

  @Override public void updateUI() {
    super.updateUI();
    Document doc = getDocument();
    if (doc instanceof AbstractDocument) {
      updateHighlightFilter((AbstractDocument) doc);
    }
  }

  @Override public void setEchoChar(char c) {
    super.setEchoChar(c);
    boolean hasCaret = getCaret() != null;
    int start = hasCaret ? getSelectionStart() : 0;
    int end = hasCaret ? getSelectionEnd() : 0;
    Document doc = getDocument();
    if (doc instanceof AbstractDocument) {
      updateHighlightFilter((AbstractDocument) doc);
    }
    if (hasCaret) {
      setSelectionStart(start);
      setSelectionEnd(end);
    }
  }

  private void updateHighlightFilter(AbstractDocument doc) {
    boolean reveal = getEchoChar() == '\u0000';
    if (reveal) {
      setForeground(new Color(0x0, true));
      setSelectedTextColor(new Color(0x0, true));
      doc.setDocumentFilter(new HighlightFilter(this));
      try {
        doc.remove(0, 0);
      } catch (BadLocationException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(this);
      }
    } else {
      Highlighter highlighter = getHighlighter();
      if (highlighter != null) {
        highlighter.removeAllHighlights();
      }
      setForeground(UIManager.getColor("PasswordField.foreground"));
      setSelectedTextColor(UIManager.getColor("PasswordField.selectionForeground"));
      doc.setDocumentFilter(null);
    }
  }
}

class HighlightFilter extends DocumentFilter {
  private final Color fgc = UIManager.getColor("PasswordField.foreground");
  private final Highlighter.HighlightPainter defPainter = new ForegroundPainter(fgc);
  private final Highlighter.HighlightPainter numPainter = new ForegroundPainter(Color.RED);
  private final Pattern pattern = Pattern.compile("\\d");
  private final JTextComponent field;

  protected HighlightFilter(JTextComponent field) {
    super();
    this.field = field;
  }

  @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    super.insertString(fb, offset, text, attr);
    update(fb);
  }

  @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    super.remove(fb, offset, length);
    update(fb);
  }

  @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    super.replace(fb, offset, length, text, attrs);
    update(fb);
  }

  private void update(FilterBypass fb) {
    Document doc = fb.getDocument();
    field.getHighlighter().removeAllHighlights();
    try {
      Highlighter highlighter = field.getHighlighter();
      String text = doc.getText(0, doc.getLength());
      Matcher matcher = pattern.matcher(text);
      int pos = 0;
      while (matcher.find(pos) && !matcher.group().isEmpty()) {
        pos = matcher.end();
        highlighter.addHighlight(matcher.start(), pos, numPainter);
      }
      highlighter.addHighlight(0, doc.getLength(), defPainter);
    } catch (BadLocationException | PatternSyntaxException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(field);
    }
  }
}

class OverlayLayoutPanel extends JPanel {
  @Override public void updateUI() {
    super.updateUI();
    setLayout(new OverlayLayout(this));
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false;
  }
}

class EyeIcon implements Icon {
  private final Color color;

  protected EyeIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(color);
    int iw = getIconWidth();
    int ih = getIconHeight();
    double s = getIconWidth() / 12d;
    g2.setStroke(new BasicStroke((float) s));
    double w = iw - s * 2d;
    double h = ih - s * 2d;
    // double r = (Math.sqrt(2d) * w - 2d * s) / 2d;
    double r = w * 3d / 4d - s * 2d;
    double x0 = w / 2d - r + s;
    Area eye = new Area(new Ellipse2D.Double(x0, s * 4d - r, r * 2d, r * 2d));
    eye.intersect(new Area(new Ellipse2D.Double(x0, h - r - s * 2d, r * 2d, r * 2d)));
    g2.draw(eye);
    double rr = iw / 6d;
    g2.draw(new Ellipse2D.Double(iw / 2d - rr, ih / 2d - rr, rr * 2d, rr * 2d));
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      if (m.isSelected() || m.isPressed()) {
        Shape l = new Line2D.Double(iw / 6d, ih * 5d / 6d, iw * 5d / 6d, ih / 6d);
        AffineTransform at = AffineTransform.getTranslateInstance(-s, 0d);
        g2.setPaint(Color.WHITE);
        g2.draw(at.createTransformedShape(l));
        g2.setPaint(color);
        g2.draw(l);
      }
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

class ForegroundPainter extends DefaultHighlighter.DefaultHighlightPainter {
  protected ForegroundPainter(Color color) {
    super(color);
  }

  @Override public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
    Rectangle r = getDrawingArea(offs0, offs1, bounds, view);
    if (!r.isEmpty()) {
      try {
        String s = c.getDocument().getText(offs0, offs1 - offs0);
        Graphics2D g2 = (Graphics2D) g.create();
        Font font = c.getFont();
        FontMetrics metrics = g2.getFontMetrics(font);
        int ascent = metrics.getAscent();
        g2.setColor(getColor());
        g2.drawString(s, (float) r.x, (float) (r.y + ascent));
        g2.dispose();
      } catch (BadLocationException ex) {
        Logger.getGlobal().severe(ex::getMessage);
      }
    }
    return r;
  }

  // @see javax.swing.text.DefaultHighlighter.DefaultHighlightPainter#paintLayer(...)
  private Rectangle getDrawingArea(int offs0, int offs1, Shape bounds, View view) {
    Rectangle r = new Rectangle();
    if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
      // Contained in view, can just use bounds.
      if (bounds instanceof Rectangle) {
        r.setBounds((Rectangle) bounds);
      } else {
        r.setBounds(bounds.getBounds());
      }
    } else {
      // Should only render part of View.
      try {
        // --- determine locations ---
        Shape shape = view.modelToView(
            offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
        r.setBounds(shape instanceof Rectangle ? (Rectangle) shape : shape.getBounds());
      } catch (BadLocationException ex) {
        // can't render
        r.setSize(0, 0);
      }
    }
    return r;
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
