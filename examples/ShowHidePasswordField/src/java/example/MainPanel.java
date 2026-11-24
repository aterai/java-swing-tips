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
  private static final String ECHO_CHAR_KEY = "PasswordField.echoChar";

  private MainPanel() {
    super(new GridLayout(4, 1, 0, 2));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    JPanel p1 = createCheckBoxPasswordPanel();
    add(createTitledPanel("BorderLayout + JCheckBox", p1));
    JPanel p2 = createToggleButtonPasswordPanel();
    add(createTitledPanel("OverlayLayout + JToggleButton", p2));
    JPanel p3 = createCardLayoutPasswordPanel();
    add(createTitledPanel("CardLayout + JTextField(can copy) + ...", p3));
    JPanel p4 = createHoldToShowPasswordPanel();
    add(createTitledPanel("press and hold down the mouse button", p4));
    setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel createCheckBoxPasswordPanel() {
    JPasswordField password = createPasswordField();
    AbstractButton button = new JCheckBox("show passwords");
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      setPasswordVisible(password, b);
    });
    JPanel p = new JPanel(new BorderLayout());
    p.add(password);
    p.add(button, BorderLayout.SOUTH);
    return p;
  }

  private static JPanel createToggleButtonPasswordPanel() {
    JPasswordField password = createPasswordField();
    // AbstractDocument doc = (AbstractDocument) password.getDocument();
    // doc.setDocumentFilter(new ASCIIOnlyDocumentFilter());
    AbstractButton button = EyeButtonUtils.createEyeToggleButton();
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      setPasswordVisible(password, b);
    });
    JPanel p = createOverlayPanel();
    p.add(button);
    p.add(password);
    return p;
  }

  private static JPanel createCardLayoutPasswordPanel() {
    JPasswordField password = createPasswordField();
    JTextField textField = new JTextField(24);
    textField.setFont(FONT);
    textField.enableInputMethods(false);
    // Share the Document so that both fields always display the same text
    textField.setDocument(password.getDocument());

    CardLayout visibilityLayout = new CardLayout();
    JPanel cardPanel = new JPanel(visibilityLayout) {
      @Override public void updateUI() {
        super.updateUI();
        setAlignmentX(RIGHT_ALIGNMENT);
      }
    };
    cardPanel.add(password, PasswordVisibility.HIDDEN.toString());
    cardPanel.add(textField, PasswordVisibility.VISIBLE.toString());

    AbstractButton button = EyeButtonUtils.createEyeToggleButton();
    button.addActionListener(e -> {
      boolean b = ((AbstractButton) e.getSource()).isSelected();
      PasswordVisibility s = b ? PasswordVisibility.VISIBLE : PasswordVisibility.HIDDEN;
      visibilityLayout.show(cardPanel, s.toString());
    });

    JPanel p = createOverlayPanel();
    p.add(button);
    p.add(cardPanel);
    return p;
  }

  private static JPanel createHoldToShowPasswordPanel() {
    JPasswordField password = createPasswordField();
    AbstractButton button = EyeButtonUtils.createEyeButton();
    button.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        setPasswordVisible(password, true);
      }

      @Override public void mouseReleased(MouseEvent e) {
        setPasswordVisible(password, false);
      }
    });
    JPanel p = createOverlayPanel();
    p.add(button);
    p.add(password);
    return p;
  }

  private static void setPasswordVisible(JPasswordField password, boolean visible) {
    // An echo character of 0 displays the text as typed, like a JTextField
    char echoChar = visible ? 0 : (Character) UIManager.get(ECHO_CHAR_KEY);
    password.setEchoChar(echoChar);
  }

  private static JPanel createOverlayPanel() {
    JPanel p = new JPanel() {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    p.setLayout(new OverlayLayout(p));
    return p;
  }

  private static JPasswordField createPasswordField() {
    JPasswordField pf = new JPasswordField(24) {
      @Override public void updateUI() {
        super.updateUI();
        // Once JPasswordField#setEchoChar(...) is called, the new LookAndFeel no longer
        // installs its own echo character, so reapply it while the password is hidden
        Object echoChar = UIManager.get(ECHO_CHAR_KEY);
        if (echoCharIsSet() && echoChar instanceof Character) {
          setEchoChar((Character) echoChar);
        }
      }
    };
    pf.setText("1234567890");
    pf.setAlignmentX(RIGHT_ALIGNMENT);
    return pf;
  }

  private static Component createTitledPanel(String title, Component cmp) {
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

final class EyeButtonUtils {
  private EyeButtonUtils() {
    /* Singleton */
  }

  public static AbstractButton createEyeToggleButton() {
    AbstractButton b = new JToggleButton() {
      @Override public void updateUI() {
        super.updateUI();
        // WindowsToggleButtonUI replaces even a non-UIResource border with an XP border,
        // so reapply the empty border every time the LookAndFeel is changed
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
      }
    };
    configureEyeButton(b);
    return b;
  }

  public static AbstractButton createEyeButton() {
    AbstractButton b = new JButton() {
      @Override public void updateUI() {
        super.updateUI();
        // WindowsButtonUI also replaces the border with an XP border
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
      }
    };
    configureEyeButton(b);
    return b;
  }

  private static void configureEyeButton(AbstractButton b) {
    b.setFocusable(false);
    // WindowsButtonUI enlarges an even preferred size to an odd one when the focus is painted
    b.setFocusPainted(false);
    b.setOpaque(false);
    b.setContentAreaFilled(false);
    b.setAlignmentX(Component.RIGHT_ALIGNMENT);
    b.setAlignmentY(Component.CENTER_ALIGNMENT);
    b.setIcon(new EyeIcon(Color.BLUE));
    b.setRolloverIcon(new EyeIcon(Color.DARK_GRAY));
    b.setSelectedIcon(new EyeIcon(Color.BLUE));
    b.setRolloverSelectedIcon(new EyeIcon(Color.BLUE));
    b.setToolTipText("show/hide passwords");
  }
}

enum PasswordVisibility {
  VISIBLE, HIDDEN
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
    double s = iw / 12d;
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

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
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
