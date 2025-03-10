// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(new JScrollPane(new JTable(makeModel())));
    add(new JScrollPane(new CheckBoxTable(makeModel())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "CheckBox"};
    Object[][] data = {
        {"aaa", 12, false}, {"bbb", 5, false}, {"CCC", 92, false}, {"DDD", 0, false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
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

class CheckBoxTable extends JTable {
  private static final int CHECKBOX_COLUMN = 2;
  private transient MouseListener handler;
  private transient ListSelectionListener listener;
  private int checkedIndex = -1;

  protected CheckBoxTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    addMouseListener(handler);
    getSelectionModel().removeListSelectionListener(listener);
    super.updateUI();
    setSelectionModel(new CheckBoxListSelectionModel());
    handler = new CheckBoxListener();
    addMouseListener(handler);
    listener = e -> {
      if (checkedIndex < 0) { // e.getValueIsAdjusting();
        ListSelectionModel sm = (ListSelectionModel) e.getSource();
        TableModel model = getModel();
        for (int i = 0; i < getRowCount(); i++) {
          model.setValueAt(sm.isSelectedIndex(i), i, CHECKBOX_COLUMN);
        }
      }
    };
    getSelectionModel().addListSelectionListener(listener);
  }

  @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
    Component c = super.prepareEditor(editor, row, column);
    if (c instanceof JCheckBox) {
      JCheckBox b = (JCheckBox) c;
      boolean selected = getSelectionModel().isSelectedIndex(row);
      b.setBackground(selected ? getBackground() : getSelectionBackground());
      checkedIndex = row;
    } else {
      checkedIndex = -1;
    }
    return c;
  }

  private final class CheckBoxListSelectionModel extends DefaultListSelectionModel {
    @Override public void setSelectionInterval(int anchor, int lead) {
      if (checkedIndex < 0) {
        super.setSelectionInterval(anchor, lead);
      } else {
        EventQueue.invokeLater(() -> {
          if (checkedIndex >= 0 && lead == anchor && checkedIndex == anchor) {
            super.addSelectionInterval(checkedIndex, checkedIndex);
          } else {
            super.setSelectionInterval(anchor, lead);
          }
        });
      }
    }

    @Override public void removeSelectionInterval(int index0, int index1) {
      if (checkedIndex < 0) {
        super.removeSelectionInterval(index0, index1);
      } else {
        EventQueue.invokeLater(() -> super.removeSelectionInterval(index0, index1));
      }
    }
  }

  private final class CheckBoxListener extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      JTable table = (JTable) e.getComponent();
      Point pt = e.getPoint();
      if (table.columnAtPoint(pt) == CHECKBOX_COLUMN) {
        int row = table.rowAtPoint(pt);
        checkedIndex = row;
        ListSelectionModel sm = table.getSelectionModel();
        if (sm.isSelectedIndex(row)) {
          sm.removeSelectionInterval(row, row);
        } else {
          sm.addSelectionInterval(row, row);
        }
      } else {
        checkedIndex = -1;
      }
      table.repaint();
    }
  }
}
