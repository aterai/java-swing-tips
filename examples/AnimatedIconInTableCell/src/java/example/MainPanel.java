// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable();
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/restore_to_background_color.gif");
    Icon icon = Optional.ofNullable(url)
        .<Icon>map(ImageIcon::new)
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));
    Object[][] data = {
        {"Default ImageIcon", icon},
        {"ImageIcon#setImageObserver", makeAnimatedIcon(url, table, 1, 1)}
    };
    String[] columnNames = {"String", "ImageIcon"};
    table.setModel(new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column == 0;
      }
    });
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(20);

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static Icon makeAnimatedIcon(URL url, JTable table, int row, int col) {
    return Optional.ofNullable(url)
        .map(u -> makeIcon(u, table, row, col))
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));
  }

  private static Icon makeIcon(URL url, JTable table, int row, int col) {
    ImageIcon icon = new ImageIcon(url);
    icon.setImageObserver((img, flags, x, y, w, h) -> {
      boolean repaint = false;
      if (table.isShowing()) {
        if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
          tableRepaint(table, row, col);
        }
        repaint = (flags & (ALLBITS | ABORT)) == 0;
      }
      return repaint;
    });
    return icon;
  }

  private static void tableRepaint(JTable table, int row, int col) {
    int vr = table.convertRowIndexToView(row); // JDK 1.6.0
    int vc = table.convertColumnIndexToView(col);
    table.repaint(table.getCellRect(vr, vc, false));
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
