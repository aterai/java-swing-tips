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
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
  private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private static final String ECHO_CHAR = "PasswordField.echoChar";

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(0, 1, 5, 25));
    JPanel p1 = makePasswordPanel1();
    p.add(makeTitledPanel("JPasswordField#setEchoChar(...) + HighlightFilter", p1));
    JPanel p2 = makePasswordPanel2();
    p.add(makeTitledPanel("CardLayout + (JPasswordField <> JTextPane)", p2));
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(25, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makePasswordPanel1() {
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

  private JPanel makePasswordPanel2() {
    JPasswordField password = new JPasswordField(40);
    password.setFont(FONT);
    password.setText("!1l2c$%34e5&6#7=8g9O0");
    JTextPane revealPassword = makeRevealPassword(password);
    CardLayout cardLayout = new CardLayout();
    JPanel p = new JPanel(cardLayout) {
      @Override public void updateUI() {
        super.updateUI();
        setAlignmentX(RIGHT_ALIGNMENT);
      }
    };
    p.add(password, PasswordField.HIDE.toString());
    p.add(revealPassword, PasswordField.SHOW.toString());

    AbstractButton button = new JToggleButton();
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      if (b) {
        copyText(password.getDocument(), revealPassword.getStyledDocument());
        cardLayout.show(p, PasswordField.SHOW.toString());
      } else {
        copyText(revealPassword.getStyledDocument(), password.getDocument());
        cardLayout.show(p, PasswordField.HIDE.toString());
      }
    });
    initEyeButton(button);

    JPanel panel = new OverlayLayoutPanel();
    panel.add(button);
    panel.add(p);
    return panel;
  }

  private static void copyText(Document src, Document dst) {
    try {
      dst.remove(0, dst.getLength());
      String text = src.getText(0, src.getLength());
      dst.insertString(0, text, null);
    } catch (BadLocationException ex) {
      Logger.getGlobal().severe(ex::getMessage);
    }
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

  private static JTextPane makeRevealPassword(JPasswordField password) {
    JTextPane textPane = new OneLineTextPane();
    textPane.setBorder(password.getBorder());
    textPane.setFont(password.getFont());
    StyledDocument doc = textPane.getStyledDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new HighlightDocumentFilter());
      try {
        int length = password.getDocument().getLength();
        String text = password.getDocument().getText(0, length);
        doc.insertString(0, text, new SimpleAttributeSet());
      } catch (BadLocationException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(textPane);
      }
    }
    return textPane;
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

enum PasswordField {
  SHOW, HIDE
}

class DigitHighlightPasswordField extends JPasswordField {
  protected DigitHighlightPasswordField(int columns) {
    super(columns);
  }

  @Override public void setEchoChar(char c) {
    super.setEchoChar(c);
    Document doc = getDocument();
    if (doc instanceof AbstractDocument) {
      boolean reveal = c == '\u0000';
      if (reveal) {
        ((AbstractDocument) doc).setDocumentFilter(new HighlightFilter(this));
        try {
          doc.remove(0, 0);
        } catch (BadLocationException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
      } else {
        getHighlighter().removeAllHighlights();
        ((AbstractDocument) doc).setDocumentFilter(null);
      }
    }
  }
}

class HighlightFilter extends DocumentFilter {
  private final Highlighter.HighlightPainter painter =
      new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
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
        highlighter.addHighlight(matcher.start(), pos, painter);
      }
    } catch (BadLocationException | PatternSyntaxException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(field);
    }
  }
}

class HighlightDocumentFilter extends DocumentFilter {
  private final SimpleAttributeSet defAttr = new SimpleAttributeSet();
  private final SimpleAttributeSet numAttr = new SimpleAttributeSet();
  private final Pattern pattern = Pattern.compile("\\d");

  protected HighlightDocumentFilter() {
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
