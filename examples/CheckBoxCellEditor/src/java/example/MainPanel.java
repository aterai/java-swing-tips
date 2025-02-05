// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        // setDefaultRenderer(Boolean.class, null);
        setDefaultEditor(Boolean.class, null);
        super.updateUI();
        // setDefaultRenderer(Boolean.class, new CheckBoxPanelRenderer());
        setDefaultEditor(Boolean.class, new CheckBoxPanelEditor());
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setRowHeight(24);
        setRowSelectionAllowed(true);
        setShowVerticalLines(false);
        setIntercellSpacing(new Dimension(0, 1));
        setFocusable(false);
      }
    };
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Boolean"};
    Object[][] data = {
        {"AAA", true}, {"bbb", false}, {"CCC", true},
        {"ddd", false}, {"EEE", true}, {"fff", false},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return column == 1 ? Boolean.class : super.getColumnClass(column);
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column == 1;
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

class CheckBoxPanelEditor extends AbstractCellEditor implements TableCellEditor {
  private final Container renderer = new JPanel(new GridBagLayout()) {
    private transient MouseListener listener;
    @Override public void updateUI() {
      removeMouseListener(listener);
      super.updateUI();
      setBorder(UIManager.getBorder("Table.noFocusBorder"));
      listener = new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) {
          fireEditingStopped();
        }
      };
      addMouseListener(listener);
    }
  };
  private final JCheckBox checkBox = new JCheckBox() {
    private transient Handler handler;
    @Override public void updateUI() {
      removeActionListener(handler);
      removeMouseListener(handler);
      super.updateUI();
      setOpaque(false);
      setFocusable(false);
      setRolloverEnabled(false);
      handler = new Handler();
      addActionListener(handler);
      addMouseListener(handler);
    }
  };

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    checkBox.setSelected(Objects.equals(value, Boolean.TRUE));
    // renderer.setBackground(table.getSelectionBackground());
    // renderer.removeAll();
    renderer.add(checkBox);
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return checkBox.isSelected();
  }

  private final class Handler extends MouseAdapter implements ActionListener {
    @Override public void actionPerformed(ActionEvent e) {
      fireEditingStopped();
    }

    @Override public void mousePressed(MouseEvent e) {
      Container c = SwingUtilities.getAncestorOfClass(JTable.class, e.getComponent());
      if (c instanceof JTable) {
        JTable t = (JTable) c;
        boolean isPressed = checkBox.getModel().isPressed();
        if (isPressed && t.isRowSelected(t.getEditingRow()) && e.isControlDown()) {
          renderer.setBackground(t.getBackground());
        } else {
          renderer.setBackground(t.getSelectionBackground());
        }
      }
    }

    @Override public void mouseExited(MouseEvent e) {
      Class<JTable> clz = JTable.class;
      Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, e.getComponent()))
          .filter(clz::isInstance).map(clz::cast)
          .filter(JTable::isEditing)
          .ifPresent(JTable::removeEditor);
      // Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, e.getComponent()))
      //   .filter(clz::isInstance).map(clz::cast)
      //   .filter(table -> table.isEditing() && !table.getCellEditor().stopCellEditing())
      //   .ifPresent(table -> table.getCellEditor().cancelCellEditing());
    }
  }
}

// class CheckBoxPanelEditor extends AbstractCellEditor implements TableCellEditor {
//   private final JPanel p = new JPanel(new GridBagLayout());
//   private final JCheckBox checkBox = new JCheckBox();
//   protected CheckBoxPanelEditor() {
//     super();
//     checkBox.setOpaque(false);
//     checkBox.setFocusable(false);
//     checkBox.setRolloverEnabled(false);
//     checkBox.addActionListener(e -> fireEditingStopped());
//     p.add(checkBox);
//     p.setBorder(UIManager.getBorder("Table.noFocusBorder"));
//   }
//
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     checkBox.setSelected(Objects.equals(value, Boolean.TRUE));
//     p.setBackground(table.getSelectionBackground());
//     return p;
//   }
//
//   @Override public Object getCellEditorValue() {
//     return checkBox.isSelected();
//   }
// }
//
// class CheckBoxPanelRenderer implements TableCellRenderer {
//   private final JPanel p = new JPanel(new GridBagLayout());
//   private final JCheckBox checkBox = new JCheckBox();
//   protected CheckBoxPanelRenderer() {
//     checkBox.setOpaque(false);
//     checkBox.setFocusable(false);
//     checkBox.setRolloverEnabled(false);
//     p.add(checkBox);
//     p.setBorder(UIManager.getBorder("Table.noFocusBorder"));
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     checkBox.setSelected(Objects.equals(value, Boolean.TRUE));
//     p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
//     return p;
//   }
// }
