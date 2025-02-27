// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"Name", "Class", "Value"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0);
    JTable table = new JTable(model) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    table.setAutoCreateRowSorter(true);
    PropertyChangeListener l = e -> updateModel(model, e);
    Toolkit.getDefaultToolkit().addPropertyChangeListener("win.xpstyle.colorName", l);
    Toolkit.getDefaultToolkit().addPropertyChangeListener("awt.multiClickInterval", l);
    updateModel(model, null);
    setPreferredSize(new Dimension(320, 240));
    add(new JScrollPane(table));
  }

  private static void updateModel(DefaultTableModel model, PropertyChangeEvent e) {
    if (Objects.nonNull(e)) {
      LOGGER.info(() -> {
        String n = e.getPropertyName();
        Object p = Toolkit.getDefaultToolkit().getDesktopProperty(n);
        return String.format("%s: %s", n, p);
      });
    }
    model.setRowCount(0);
    Toolkit tk = Toolkit.getDefaultToolkit();
    for (String s : (String[]) tk.getDesktopProperty("win.propNames")) {
      Object o = tk.getDesktopProperty(s);
      Object[] row = {s, o.getClass(), o};
      model.addRow(row);
    }
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
