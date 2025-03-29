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
    add(makeTitledPane(table1, "AUTO_RESIZE_SUBSEQUENT_COLUMNS(Default)"));

    JTable table2 = new JTable(1, 3);
    table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    add(makeTitledPane(table2, "AUTO_RESIZE_OFF"));

    JTable table3 = new JTable(1, 3) {
      // java - How to make JTable both AutoResize and horizontally scrollable? - Stack Overflow
      // https://stackoverflow.com/questions/6104916/how-to-make-jtable-both-autoresize-and-horizontall-scrollable
      // In Java 9?, this code may no longer work correctly.
      // @Override public boolean getScrollableTracksViewportWidth() {
      //   return getPreferredSize().width < getParent().getWidth();
      // }

      @Override public boolean getScrollableTracksViewportWidth() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (getPreferredSize().width < parent.getWidth()) {
          setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
          setAutoResizeMode(AUTO_RESIZE_OFF);
        }
        return super.getScrollableTracksViewportWidth();
      }
    };
    table3.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    add(makeTitledPane(table3, "AUTO_RESIZE_OFF + getScrollableTracksViewportWidth()"));

    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPane(Component c, String title) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(new JScrollPane(c));
    return p;
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
