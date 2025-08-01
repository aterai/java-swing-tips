// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(new DefaultTableModel(15, 3));
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 5, 0, 5, Color.ORANGE));
    table.setBorder(BorderFactory.createLineBorder(Color.GREEN, 5));

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));
    scroll.setViewportBorder(BorderFactory.createLineBorder(Color.RED, 5));

    scroll.setBackground(Color.YELLOW);
    scroll.getViewport().setBackground(Color.PINK);
    table.setBackground(Color.WHITE);
    table.getTableHeader().setBackground(Color.MAGENTA);

    EventQueue.invokeLater(() -> {
      JViewport vp = scroll.getColumnHeader();
      vp.setOpaque(true);
      vp.setBackground(Color.CYAN);
    });

    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
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
      Logger.getGlobal().severe(ex::getMessage);
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
