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
  public static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);
  private final String[] columnNames = {"Name", "Class", "Value"};
  private final DefaultTableModel model = new DefaultTableModel(null, columnNames);

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(model) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    table.setAutoCreateRowSorter(true);
    PropertyChangeListener l = this::initModel;
    Toolkit.getDefaultToolkit().addPropertyChangeListener("win.xpstyle.colorName", l);
    Toolkit.getDefaultToolkit().addPropertyChangeListener("awt.multiClickInterval", l);
    initModel(null);
    setPreferredSize(new Dimension(320, 240));
    add(new JScrollPane(table));
  }

  private void initModel(PropertyChangeEvent e) {
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
