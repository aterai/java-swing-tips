// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JLabel label = makeLabel("FORCE_HEAVYWEIGHT_POPUP", Color.PINK);

    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      @Override public Void run() {
        try {
          Field field;
          if (System.getProperty("java.version").startsWith("1.6.0")) {
            // https://community.oracle.com/thread/1357949 ComboBox scroll and selected/highlight on glasspane
            // Class<?> clazz = Class.forName("javax.swing.PopupFactory"); // errorprone: LiteralClassName
            // field = clazz.getDeclaredField("forceHeavyWeightPopupKey");
            field = PopupFactory.class.getDeclaredField("forceHeavyWeightPopupKey");
          } else { // JDK 1.7.0, 1.8.0
            Class<?> clazz = Class.forName("javax.swing.ClientPropertyKey");
            field = clazz.getDeclaredField("PopupFactory_FORCE_HEAVYWEIGHT_POPUP");
          }
          field.setAccessible(true);
          label.putClientProperty(field.get(null), Boolean.TRUE);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ex) {
          throw new UnsupportedOperationException(ex);
        }
        return null;
      }
    });

    JComponent glass = new JPanel(new BorderLayout()) {
      private final Color backgroundColor = new Color(100, 100, 200, 100);
      @Override protected void paintComponent(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
      }
    };
    glass.setOpaque(false);
    glass.add(makeLabel("Default: ToolTipText", Color.ORANGE), BorderLayout.WEST);
    glass.add(label, BorderLayout.EAST);
    glass.add(Box.createVerticalStrut(60), BorderLayout.SOUTH);
    EventQueue.invokeLater(() -> {
      getRootPane().setGlassPane(glass);
      getRootPane().getGlassPane().setVisible(true);
    });
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel makeLabel(String title, Color color) {
    JLabel label = new JLabel(title);
    label.setOpaque(true);
    label.setBackground(color);
    label.setToolTipText("1234567890");
    return label;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
