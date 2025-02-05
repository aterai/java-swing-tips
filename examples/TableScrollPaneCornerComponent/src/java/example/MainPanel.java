// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    // System.out.println(UIManager.get("Table.scrollPaneCornerComponent"));
    JTable table = new JTable(15, 3) {
      @Override public boolean getScrollableTracksViewportWidth() {
        Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (c instanceof JScrollPane) {
          JScrollPane scroll = (JScrollPane) c;
          Component ur = scroll.getCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER);
          if (ur != null) {
            ur.setVisible(getPreferredSize().width >= scroll.getViewport().getWidth());
          }
        }
        return super.getScrollableTracksViewportWidth();
      }
    };

    Arrays.asList(new JTable(15, 3), table).forEach(t -> {
      t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      add(new JScrollPane(t));
    });
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
