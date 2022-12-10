// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    String[] list = {
        "no_disposal_specified",
        "do_not_dispose",
        "restore_to_background_color",
        "restore_to_previous"
    };
    for (String s : list) {
      box.add(makeLabel(s));
      box.add(Box.createVerticalStrut(20));
    }
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
    setPreferredSize(new Dimension(320, 240));
  }

  public static JLabel makeLabel(String name) {
    String path = "example/" + name + ".gif";
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Icon icon = url == null ? UIManager.getIcon("html.missingImage") : new ImageIcon(url);
    return new JLabel(name, icon, SwingConstants.LEFT);
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
