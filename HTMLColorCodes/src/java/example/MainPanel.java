// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    Box box = Box.createVerticalBox();
    box.add(makeLabel("new Color(0xFF0000)", new Color(0xFF0000)));
    box.add(makeLabel("new Color(0x88_88_88)", new Color(0x88_88_88)));
    box.add(makeLabel("new Color(Integer.parseInt(\"00FF00\", 16))", new Color(Integer.parseInt("00FF00", 16))));
    box.add(makeLabel("new Color(Integer.decode(\"#0000FF\"))", new Color(Integer.decode("#0000FF"))));
    box.add(makeLabel("Color.decode(\"#00FFFF\")", Color.decode("#00FFFF")));

    JLabel label = new JLabel("<html><span style='color: #FF00FF'>#FF00FF");
    label.setBorder(BorderFactory.createTitledBorder("new JLabel(\"<html><span style='color: #FF00FF'>#FF00FF\")"));
    box.add(label);
    box.add(Box.createVerticalGlue());

    add(new JScrollPane(box));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel makeLabel(String title, Color c) {
    JLabel label = new JLabel(String.format("#%06x", c.getRGB() & 0xFF_FF_FF)) {
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
