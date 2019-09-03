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

    UIManager.put("Button.disabledToolBarBorderBackground", Color.RED);
    UIManager.put("Button.toolBarBorderBackground", Color.GREEN);

    // TEST: JPanel toolbar = new JPanel();
    JToolBar toolbar = new JToolBar();

    JToggleButton tg1 = new JToggleButton("Tg1");
    tg1.setEnabled(false);
    JToggleButton tg2 = new JToggleButton("Tg2");
    JToggleButton tg3 = new JToggleButton("Tg3");
    tg3.setBorder(BorderFactory.createLineBorder(Color.BLUE));
    JToggleButton tg4 = new JToggleButton("Tg4", true);
    JToggleButton tg5 = new JToggleButton("Tg5");

    Dimension dim = new Dimension(5, 5);
    ButtonGroup bg = new ButtonGroup();
    Stream.of(tg1, tg2, tg3, tg4, tg5).forEach(b -> {
      b.setFocusPainted(false);
      toolbar.add(b);
      toolbar.add(Box.createRigidArea(dim));
      bg.add(b);
    });

    JButton button = new JButton("Button");
    toolbar.add(button);
    // JRadioButton radio = new JRadioButton("Radio");
    // JCheckBox check = new JCheckBox("Check");

    add(toolbar, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
