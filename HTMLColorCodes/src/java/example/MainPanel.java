package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    Box box = Box.createVerticalBox();
    box.add(makeLabel("new Color(0xff0000)", new Color(0xff0000)));
    box.add(makeLabel("new Color(0x88_88_88)", new Color(0x88_88_88)));
    box.add(makeLabel("new Color(Integer.parseInt(\"00ff00\", 16))", new Color(Integer.parseInt("00ff00", 16))));
    box.add(makeLabel("new Color(Integer.decode(\"#0000ff\"))", new Color(Integer.decode("#0000ff"))));
    box.add(makeLabel("Color.decode(\"#00ffff\")", Color.decode("#00ffff")));

    JLabel label = new JLabel("<html><span style='color: #ff00ff'>#ff00ff");
    label.setBorder(BorderFactory.createTitledBorder("new JLabel(\"<html><span style='color: #ff00ff'>#ff00ff\")"));
    box.add(label);
    box.add(Box.createVerticalGlue());

    add(new JScrollPane(box));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel makeLabel(String title, Color c) {
    JLabel label = new JLabel(String.format("#%06x", c.getRGB() & 0xffffff)) {
      @Override public Dimension getMaximumSize() {
        Dimension d = super.getPreferredSize();
        d.width = Short.MAX_VALUE;
        return d;
      }
    };
    label.setBorder(BorderFactory.createTitledBorder(title));
    label.setForeground(c);
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
