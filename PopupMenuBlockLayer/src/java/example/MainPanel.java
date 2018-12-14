// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"String", "Integer", "Boolean"};
    DefaultTableModel model = new DefaultTableModel(null, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        // ArrayIndexOutOfBoundsException: 0 >= 0
        // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
        // https://bugs.openjdk.java.net/browse/JDK-6967479
        // return getValueAt(0, column).getClass();
        switch (column) {
          case 0: return String.class;
          case 1: return Number.class;
          case 2: return Boolean.class;
          default: return super.getColumnClass(column);
        }
      }
    };
    IntStream.range(0, 100).forEach(i -> model.addRow(new Object[] {"Name " + i, i, Boolean.FALSE}));

    JTable table = new JTable(model) {
      @Override public String getToolTipText(MouseEvent e) {
        int row = convertRowIndexToModel(rowAtPoint(e.getPoint()));
        TableModel m = getModel();
        return String.format("%s, %s", m.getValueAt(row, 0), m.getValueAt(row, 2));
      }
    };
    table.setAutoCreateRowSorter(true);
    table.setComponentPopupMenu(new TablePopupMenu());

    DisableInputLayerUI<Component> layerUI = new DisableInputLayerUI<>();
    JCheckBox check = new JCheckBox("Lock all(JScrollPane, JTable, JPopupMenu)");
    check.addActionListener(e -> layerUI.setLocked(((JCheckBox) e.getSource()).isSelected()));

    JScrollPane scroll = new JScrollPane(table);

    add(new JLayer<>(scroll, layerUI));
    add(check, BorderLayout.NORTH);
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class DisableInputLayerUI<V extends Component> extends LayerUI<V> {
  private static final String CMD_REPAINT = "lock";
  private final transient MouseListener dmyMouseListener = new MouseAdapter() { /* Dummy listener */ };
  private boolean isBlocking;

  public void setLocked(boolean flag) {
    firePropertyChange(CMD_REPAINT, isBlocking, flag);
    isBlocking = flag;
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> jlayer = (JLayer<?>) c;
      jlayer.getGlassPane().addMouseListener(dmyMouseListener);
      jlayer.setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
          | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      JLayer<?> jlayer = (JLayer<?>) c;
      jlayer.setLayerEventMask(0);
      jlayer.getGlassPane().removeMouseListener(dmyMouseListener);
    }
    super.uninstallUI(c);
  }

  @Override public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
    if (isBlocking && e instanceof InputEvent) {
      ((InputEvent) e).consume();
    }
  }

  @Override public void applyPropertyChange(PropertyChangeEvent pce, JLayer<? extends V> l) {
    String cmd = pce.getPropertyName();
    if (CMD_REPAINT.equals(cmd)) {
      l.getGlassPane().setVisible((Boolean) pce.getNewValue());
    }
  }
}

class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  protected TablePopupMenu() {
    super();
    add("add").addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      model.addRow(new Object[] {"New row", model.getRowCount(), false});
      Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
      table.scrollRectToVisible(r);
    });
    addSeparator();
    delete = add("delete");
    delete.addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        model.removeRow(table.convertRowIndexToModel(selection[i]));
      }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }
}
