package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public final class MainPanel extends JPanel {
  @SuppressWarnings("PMD.UseProperClassLoader")
  private MainPanel() {
    super(new BorderLayout());

    URL url1 = getClass().getClassLoader().getResource("example/test.png");
    // URL url1 = Thread.currentThread().getContextClassLoader().getResource("example/test.png");
    URL url2 = getClass().getResource("test.png");

    JPanel p = new JPanel(new GridLayout(3, 1, 5, 5));
    p.add(new JLabel(new ImageIcon(url1)));
    p.add(makeTitledPanel("getClassLoader().getResource(\"example/test.png\")", new JLabel(url1.toString())));
    p.add(makeTitledPanel("getClass().getResource(\"test.png\")", new JLabel(url2.toString())));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
