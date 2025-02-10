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
    DisableInputLayerUI<Component> layerUI = new DisableInputLayerUI<>();
    JCheckBox check = new JCheckBox("Lock all(JScrollPane, JTable, JPopupMenu)");
    check.addActionListener(e -> layerUI.setLocked(((JCheckBox) e.getSource()).isSelected()));
    JTable table = makeTable(makeModel());
    add(new JLayer<>(new JScrollPane(table), layerUI));
    add(check, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable(TableModel model) {
    JTable table = new JTable(model) {
      @Override public String getToolTipText(MouseEvent e) {
        String txt = super.getToolTipText(e);
        int idx = rowAtPoint(e.getPoint());
        if (idx >= 0) {
          int row = convertRowIndexToModel(idx);
          TableModel m = getModel();
          txt = String.format("%s, %s", m.getValueAt(row, 0), m.getValueAt(row, 2));
        }
        return txt;
      }
    };
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    return table;
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
      @SuppressWarnings("PMD.OnlyOneReturn")
      @Override public Class<?> getColumnClass(int column) {
        switch (column) {
          case 0: return String.class;
          case 1: return Number.class;
          case 2: return Boolean.class;
          default: return super.getColumnClass(column);
        }
        // // Java 12:
        // return switch (column) {
        //   case 0 -> String.class;
        //   case 1 -> Number.class;
        //   case 2 -> Boolean.class;
        //   default -> super.getColumnClass(column);
        // };
      }
    };
    IntStream.range(0, 100)
        .mapToObj(i -> new Object[] {"Name " + i, i, false})
        .forEach(model::addRow);
    return model;
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

class DisableInputLayerUI<V extends Component> extends LayerUI<V> {
  private static final String CMD_REPAINT = "lock";
  private final transient MouseListener emptyMouseAdapter = new MouseAdapter() {
    /* do nothing listener */
  };
  private boolean isBlocking;

  public void setLocked(boolean flag) {
    firePropertyChange(CMD_REPAINT, isBlocking, flag);
    isBlocking = flag;
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> jlayer = (JLayer<?>) c;
      jlayer.getGlassPane().addMouseListener(emptyMouseAdapter);
      jlayer.setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
          | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      JLayer<?> jlayer = (JLayer<?>) c;
      jlayer.setLayerEventMask(0);
      jlayer.getGlassPane().removeMouseListener(emptyMouseAdapter);
    }
    super.uninstallUI(c);
  }

  @Override public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
    if (isBlocking && e instanceof InputEvent) {
      ((InputEvent) e).consume();
    }
  }

  @Override public void applyPropertyChange(PropertyChangeEvent e, JLayer<? extends V> l) {
    if (CMD_REPAINT.equals(e.getPropertyName())) {
      l.getGlassPane().setVisible((Boolean) e.getNewValue());
    }
  }
}

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
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
