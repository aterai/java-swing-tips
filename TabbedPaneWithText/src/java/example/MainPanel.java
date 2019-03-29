// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

// https://community.oracle.com/thread/1392495 JTabbedPane with non-tabbed text
public final class MainPanel extends JPanel {
  public static final String TEXT = "<--1234567890";

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // FontMetrics fm = getFontMetrics(getFont());
        FontMetrics fm = g.getFontMetrics();
        int stringWidth = fm.stringWidth(TEXT) + 10;
        int x = getSize().width - stringWidth;
        Rectangle lastTab = getBoundsAt(getTabCount() - 1);
        int tabEnd = lastTab.x + lastTab.width;
        int xx = Math.max(x, tabEnd) + 5;
        g.drawString(TEXT, xx, 18);
      }
    };
    tabs.addTab("title1", new JLabel("tab1"));
    tabs.addTab("title2", new JLabel("tab2"));
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
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
