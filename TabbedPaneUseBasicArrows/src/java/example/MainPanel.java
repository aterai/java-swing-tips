// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    IntStream.range(0, 100).forEach(i -> tabs.addTab("title" + i, new JLabel("label" + i)));

    String key = "TabbedPane.useBasicArrows";
    // UIManager.put(key, Boolean.FALSE);
    // UIManager.put("ArrowButton.size", 24);

    JCheckBox check = new JCheckBox(key, UIManager.getBoolean(key));
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      UIManager.put(key, b);
      SwingUtilities.updateComponentTreeUI(tabs);
    });
    add(tabs);
    add(check, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
