// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    EventQueue.invokeLater(() -> {
      JMenu menu = descendants(getRootPane())
          .filter(JMenu.class::isInstance).map(JMenu.class::cast)
          .findFirst().orElse(new JMenu(" "));
      menu.add("added to the SystemMenu");

      log.append(menu.getPreferredSize() + "\n");
      menu.setIcon(UIManager.getIcon("InternalFrame.icon"));
      log.append(menu.getPreferredSize() + "\n---\n");

      Component c = menu;
      while (c != null) {
        log.append(c.getClass().getName() + "\n");
        c = c.getParent();
      }
    });

    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
