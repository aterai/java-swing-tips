// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private static final String ECHO_CHAR = "PasswordField.echoChar";

  private MainPanel() {
    super(new GridLayout(4, 1, 0, 2));
    JPanel p1 = makePasswordPanel1();
    add(makeTitledPanel("BorderLayout + JCheckBox", p1));
    JPanel p2 = makePasswordPanel2();
    add(makeTitledPanel("OverlayLayout + JToggleButton", p2));
    JPanel p3 = makePasswordPanel3();
    add(makeTitledPanel("CardLayout + JTextField(can copy) + ...", p3));
    JPanel p4 = makePasswordPanel4();
    add(makeTitledPanel("press and hold down the mouse button", p4));
    setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makePasswordPanel1() {
    JPasswordField password = makePasswordField();
    AbstractButton button = new JCheckBox("show passwords");
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      password.setEchoChar(b ? '\u0000' : (Character) UIManager.get(ECHO_CHAR));
    });
    JPanel p = new JPanel(new BorderLayout());
    p.add(password);
    p.add(button, BorderLayout.SOUTH);
    return p;
  }

  private static JPanel makePasswordPanel2() {
    JPasswordField password = makePasswordField();
    // AbstractDocument doc = (AbstractDocument) password.getDocument();
    // doc.setDocumentFilter(new ASCIIOnlyDocumentFilter());
    AbstractButton button = new JToggleButton();
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      password.setEchoChar(b ? '\u0000' : (Character) UIManager.get(ECHO_CHAR));
    });
    initEyeButton(button);
    JPanel p = makeOverlayLayoutPanel();
    p.add(button);
    p.add(password);
    return p;
  }

  private JPanel makePasswordPanel3() {
    JPasswordField password = makePasswordField();
    JTextField field = new JTextField(24);
    field.setFont(FONT);
    field.enableInputMethods(false);
    field.setDocument(password.getDocument());

    CardLayout cardLayout = new CardLayout();
    JPanel p = new JPanel(cardLayout) {
      @Override public void updateUI() {
        super.updateUI();
        setAlignmentX(RIGHT_ALIGNMENT);
      }
    };
    p.add(password, PasswordField.HIDE.toString());
    p.add(field, PasswordField.SHOW.toString());

    AbstractButton button = new JToggleButton();
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      PasswordField s = b ? PasswordField.SHOW : PasswordField.HIDE;
      cardLayout.show(p, s.toString());
    });
    initEyeButton(button);

    JPanel panel = makeOverlayLayoutPanel();
    panel.add(button);
    panel.add(p);
    return panel;
  }

  private static JPanel makePasswordPanel4() {
    JPasswordField password = makePasswordField();
    AbstractButton button = new JButton();
    button.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        password.setEchoChar('\u0000');
      }

      @Override public void mouseReleased(MouseEvent e) {
        password.setEchoChar((Character) UIManager.get(ECHO_CHAR));
      }
    });
    initEyeButton(button);
    JPanel p = makeOverlayLayoutPanel();
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

  private static JPanel makeOverlayLayoutPanel() {
    JPanel p = new JPanel() {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    p.setLayout(new OverlayLayout(p));
    return p;
  }

  private static JPasswordField makePasswordField() {
    JPasswordField pf = new JPasswordField(24);
    pf.setText("1234567890");
    pf.setAlignmentX(RIGHT_ALIGNMENT);
    return pf;
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
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

// class ASCIIOnlyDocumentFilter extends DocumentFilter {
//   // private static Pattern pattern = Pattern.compile("\\A\\p{ASCII}*\\z");
//   private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
//
//   @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
//     if (Objects.nonNull(text)) {
//       replace(fb, offset, 0, text, attr);
//     }
//   }
//
//   @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
//     replace(fb, offset, length, "", null);
//   }
//
//   @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//     Document doc = fb.getDocument();
//     int currentLength = doc.getLength();
//     String currentContent = doc.getText(0, currentLength);
//     String before = currentContent.substring(0, offset);
//     String after = currentContent.substring(length + offset, currentLength);
//     String newValue = before + Objects.toString(text, "") + after;
//     checkInput(newValue, offset);
//     fb.replace(offset, length, text, attrs);
//   }
//
//   // In Java, is it possible to check if a String is only ASCII? - Stack Overflow
//   // https://stackoverflow.com/questions/3585053/in-java-is-it-possible-to-check-if-a-string-is-only-ascii
//   private static void checkInput(String proposedValue, int offset)
//         throws BadLocationException {
//     if (!proposedValue.isEmpty() && !asciiEncoder.canEncode(proposedValue)) {
//       throw new BadLocationException(proposedValue, offset);
//     }
// //     for (char c : proposedValue.toCharArray()) {
// //       if (((int) c) > 127) {
// //         throw new BadLocationException(proposedValue, offset);
// //       }
// //     }
// //     // // Java 8:
// //     // if (!proposedValue.isEmpty() && !proposedValue.chars().allMatch(c -> c < 128)) {
// //     Matcher m = pattern.matcher(proposedValue);
// //     if (!proposedValue.isEmpty() && !m.find()) {
// //       throw new BadLocationException(proposedValue, offset);
// //     }
//   }
// }

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
