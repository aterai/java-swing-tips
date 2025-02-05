// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Optional;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field1 = new JTextField("Please enter your E-mail address");
    field1.addFocusListener(new GhostFocusListener(field1));
    JTextField field2 = new WatermarkTextField();

    String amKey = "clearGlobalFocus";
    field2.getActionMap().put(amKey, new AbstractAction(amKey) {
      @Override public void actionPerformed(ActionEvent e) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
      }
    });
    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    InputMap im = field2.getInputMap(WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, modifiers), amKey);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("E-mail", field1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Search", field2));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
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
      ex.printStackTrace();
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

class WatermarkTextField extends JTextField {
  private final transient Icon icon;
  private boolean showWatermark = true;
  private transient FocusListener listener;

  protected WatermarkTextField() {
    super();
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    icon = Optional.ofNullable(cl.getResource("example/watermark.png"))
        .<Icon>map(url -> new ImageIcon(url))
        .orElse(UIManager.getIcon("html.missingImage"));
  }

  @Override public void updateUI() {
    removeFocusListener(listener);
    super.updateUI();
    listener = new FocusListener() {
      @Override public void focusGained(FocusEvent e) {
        showWatermark = false;
        e.getComponent().repaint();
      }

      @Override public void focusLost(FocusEvent e) {
        // showWatermark = getText().trim().isEmpty();
        showWatermark = getText().isEmpty();
        e.getComponent().repaint();
      }
    };
    addFocusListener(listener);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (showWatermark) {
      Graphics2D g2 = (Graphics2D) g.create();
      // Insets i = getMargin();
      Insets i = getInsets();
      int yy = (getHeight() - icon.getIconHeight()) / 2;
      // g2.drawImage(image, i.left, yy, this);
      icon.paintIcon(this, g2, i.left, yy);
      g2.dispose();
    }
  }
}

class GhostFocusListener implements FocusListener {
  private static final Color INACTIVE_COLOR = UIManager.getColor("TextField.inactiveForeground");
  private static final Color ORIGINAL_COLOR = UIManager.getColor("TextField.foreground");
  private final String ghostMessage;

  protected GhostFocusListener(JTextComponent tf) {
    ghostMessage = tf.getText();
    tf.setForeground(INACTIVE_COLOR);
  }

  @Override public void focusGained(FocusEvent e) {
    JTextComponent textField = (JTextComponent) e.getComponent();
    String str = textField.getText();
    Color col = textField.getForeground();
    if (ghostMessage.equals(str) && INACTIVE_COLOR.equals(col)) {
      textField.setForeground(ORIGINAL_COLOR);
      textField.setText("");
    }
  }

  @Override public void focusLost(FocusEvent e) {
    JTextComponent textField = (JTextComponent) e.getComponent();
    if (textField.getText().isEmpty()) {
      textField.setForeground(INACTIVE_COLOR);
      textField.setText(ghostMessage);
    }
  }
}
