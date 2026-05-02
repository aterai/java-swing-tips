// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Optional;
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
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
  private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private static final String ECHO_CHAR_KEY = "PasswordField.echoChar";

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(0, 1, 5, 25));
    JPanel p1 = createEchoCharStrategyPanel();
    p.add(createTitledPanel("JPasswordField#setEchoChar(...) + HighlightFilter", p1));
    JPanel p2 = createCardLayoutStrategyPanel();
    p.add(createTitledPanel("CardLayout + (JPasswordField <> JTextPane)", p2));
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(25, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel createEchoCharStrategyPanel() {
    JPasswordField password = new DigitHighlightField(40);
    password.setFont(FONT);
    password.setAlignmentX(RIGHT_ALIGNMENT);
    password.setText("!1l2c$%34e5&6#7=8g9O0");

    AbstractButton button = new JToggleButton();
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      password.setEchoChar(b ? '\u0000' : (Character) UIManager.get(ECHO_CHAR_KEY));
    });
    PasswordUiUtils.setupVisibilityToggleButton(button);
    JPanel p = new OverlapLayerPanel();
    p.add(button);
    p.add(password);
    return p;
  }

  private JPanel createCardLayoutStrategyPanel() {
    JPasswordField password = new JPasswordField(40);
    password.setFont(FONT);
    password.setText("!1l2c$%34e5&6#7=8g9O0");
    JTextPane visibleTextPane = PasswordUiUtils.createVisiblePasswordEditor(password);
    CardLayout cardLayout = new CardLayout();
    JPanel p = new JPanel(cardLayout) {
      @Override public void updateUI() {
        super.updateUI();
        setAlignmentX(RIGHT_ALIGNMENT);
      }
    };
    p.setOpaque(false);
    p.add(password, PasswordVisibility.HIDDEN.toString());
    p.add(visibleTextPane, PasswordVisibility.VISIBLE.toString());

    AbstractButton button = new JToggleButton();
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      if (b) {
        PasswordUiUtils.sync(password.getDocument(), visibleTextPane.getStyledDocument());
        cardLayout.show(p, PasswordVisibility.VISIBLE.toString());
      } else {
        PasswordUiUtils.sync(visibleTextPane.getStyledDocument(), password.getDocument());
        cardLayout.show(p, PasswordVisibility.HIDDEN.toString());
      }
    });
    PasswordUiUtils.setupVisibilityToggleButton(button);

    JPanel panel = new OverlapLayerPanel();
    panel.add(button);
    panel.add(p);
    return panel;
  }

  private static Component createTitledPanel(String title, Component c) {
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

enum PasswordVisibility {
  VISIBLE, HIDDEN
}

class DigitHighlightField extends JPasswordField {
  protected DigitHighlightField(int columns) {
    super(columns);
  }

  @Override public void setEchoChar(char c) {
    super.setEchoChar(c);
    Document doc = getDocument();
    if (doc instanceof AbstractDocument) {
      boolean reveal = c == '\u0000';
      if (reveal) {
        ((AbstractDocument) doc).setDocumentFilter(new BackgroundHighlightFilter(this));
        try {
          doc.remove(0, 0);
        } catch (BadLocationException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
      } else {
        Optional.ofNullable(getHighlighter()).ifPresent(Highlighter::removeAllHighlights);
        // getHighlighter().removeAllHighlights();
        ((AbstractDocument) doc).setDocumentFilter(null);
      }
    }
  }
}

class BackgroundHighlightFilter extends DocumentFilter {
  private final Highlighter.HighlightPainter painter =
      new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
  private final Pattern pattern = Pattern.compile("\\d");
  private final JTextComponent field;

  protected BackgroundHighlightFilter(JTextComponent field) {
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
        highlighter.addHighlight(matcher.start(), pos, painter);
      }
    } catch (BadLocationException | PatternSyntaxException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(field);
    }
  }
}

class TextForegroundFilter extends DocumentFilter {
  private final SimpleAttributeSet defAttr = new SimpleAttributeSet();
  private final SimpleAttributeSet numAttr = new SimpleAttributeSet();
  private final Pattern pattern = Pattern.compile("\\d");

  protected TextForegroundFilter() {
    super();
    StyleConstants.setForeground(defAttr, Color.BLACK);
    StyleConstants.setForeground(numAttr, Color.RED);
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

  private void update(FilterBypass fb) throws BadLocationException {
    StyledDocument doc = (StyledDocument) fb.getDocument();
    String text = doc.getText(0, doc.getLength());
    doc.setCharacterAttributes(0, doc.getLength(), defAttr, true);
    Matcher m = pattern.matcher(text);
    while (m.find()) {
      doc.setCharacterAttributes(m.start(), m.end() - m.start(), numAttr, true);
    }
  }
}

class OverlapLayerPanel extends JPanel {
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

final class PasswordUiUtils {
  private PasswordUiUtils() {
    // Singleton
  }

  public static void sync(Document src, Document dst) {
    try {
      dst.remove(0, dst.getLength());
      dst.insertString(0, src.getText(0, src.getLength()), null);
    } catch (BadLocationException ex) {
      Logger.getGlobal().severe(ex::getMessage);
    }
  }

  public static void setupVisibilityToggleButton(AbstractButton b) {
    b.setFocusable(false);
    b.setOpaque(false);
    b.setContentAreaFilled(false);
    b.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
    b.setAlignmentX(Component.RIGHT_ALIGNMENT);
    b.setAlignmentY(Component.CENTER_ALIGNMENT);

    Color fgc = UIManager.getColor("List.selectionBackground");

    b.setIcon(new EyeIcon(fgc));
    b.setRolloverIcon(new EyeIcon(Color.DARK_GRAY));
    b.setSelectedIcon(new EyeIcon(fgc));
    b.setRolloverSelectedIcon(new EyeIcon(fgc));
  }

  public static JTextPane createVisiblePasswordEditor(JPasswordField password) {
    JTextPane textPane = new JTextPane() {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(password.getBorder());
        setFont(password.getFont());
        setupDocument(getStyledDocument());
      }
    };

    setupDocument(textPane.getStyledDocument());
    return textPane;
  }

  private static void setupDocument(StyledDocument doc) {
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new TextForegroundFilter());
    }
  }
}
