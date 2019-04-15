// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));

    JTable table1 = new JTable(1, 3);
    JScrollPane scroll1 = new JScrollPane(table1);
    scroll1.setBorder(BorderFactory.createTitledBorder("AUTO_RESIZE_SUBSEQUENT_COLUMNS(Default)"));

    JTable table2 = new JTable(1, 3);
    table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JScrollPane scroll2 = new JScrollPane(table2);
    scroll2.setBorder(BorderFactory.createTitledBorder("AUTO_RESIZE_OFF"));

    JTable table3 = new JTable(1, 3) {
      // java - How to make JTable both AutoResize and horizontall scrollable? - Stack Overflow
      // https://stackoverflow.com/questions/6104916/how-to-make-jtable-both-autoresize-and-horizontall-scrollable
      @Override public boolean getScrollableTracksViewportWidth() {
        return getPreferredSize().width < getParent().getWidth();
      }
    };
    table3.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JScrollPane scroll3 = new JScrollPane(table3);
    scroll3.setBorder(BorderFactory.createTitledBorder("AUTO_RESIZE_OFF + getScrollableTracksViewportWidth()"));

    add(scroll1);
    add(scroll2);
    add(scroll3);
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
