// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.Serializable;
import javax.swing.*;
import javax.swing.plaf.UIResource;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenu menu = new JMenu("RadioButtonMenuItem-Test");

    menu.add(new JRadioButtonMenuItem("default", true));

    UIManager.put("RadioButtonMenuItem.checkIcon", new RadioButtonMenuItemIcon1());
    menu.add(new JRadioButtonMenuItem("ANTIALIASING", true));

    UIManager.put("RadioButtonMenuItem.checkIcon", new RadioButtonMenuItemIcon2());
    menu.add(new JRadioButtonMenuItem("fillOval", true));

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    return menuBar;
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

// @see com.sun.java.swing.plaf.windows.WindowsIconFactory.java
class RadioButtonMenuItemIcon1 implements Icon, UIResource, Serializable {
  private static final long serialVersionUID = 1L;

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    if (model.isSelected()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(x, y);
      g2.fillRoundRect(3, 3, getIconWidth() - 6, getIconHeight() - 6, 4, 4);
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}

class RadioButtonMenuItemIcon2 implements Icon, UIResource, Serializable {
  private static final long serialVersionUID = 1L;

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    AbstractButton b = (AbstractButton) c;
    ButtonModel model = b.getModel();
    if (model.isSelected()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(x, y);
      // g2.fillRoundRect(3, 3, getIconWidth() - 6, getIconHeight() - 6, 4, 4);
      g2.fillOval(2, 2, getIconWidth() - 5, getIconHeight() - 5);
      // g2.fillArc(2, 2, getIconWidth() - 5, getIconHeight() - 5, 0, 360);
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}
