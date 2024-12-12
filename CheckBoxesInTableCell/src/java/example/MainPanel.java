// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        TableColumn c = getColumnModel().getColumn(1);
        c.setCellRenderer(new CheckBoxesRenderer());
        c.setCellEditor(new CheckBoxesEditor());
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
      }
    };
    // if (System.getProperty("java.version").startsWith("1.6.0")) {
    //   // 1.6.0_xx bug? column header click -> edit cancel?
    //   table.getTableHeader().addMouseListener(new MouseAdapter() {
    //     @Override public void mousePressed(MouseEvent e) {
    //       if (table.isEditing()) {
    //         table.getCellEditor().stopCellEditing();
    //       }
    //     }
    //   });
    // }
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"user", "rwx"};
    Object[][] data = {
        {"owner", 7}, {"group", 6}, {"other", 5}
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

class CheckBoxesPanel extends JPanel {
  private static final Color BGC = new Color(0x0, true);
  private final String[] titles = {"r", "w", "x"};
  private final List<JCheckBox> buttons = Stream.of(titles).map(title -> {
    JCheckBox b = new JCheckBox(title);
    b.setOpaque(false);
    b.setFocusable(false);
    b.setRolloverEnabled(false);
    b.setBackground(BGC);
    return b;
  }).collect(Collectors.toList());

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBackground(BGC);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    EventQueue.invokeLater(this::initButtons);
  }

  private void initButtons() {
    removeAll();
    for (JCheckBox b : buttons) {
      add(b);
      add(Box.createHorizontalStrut(5));
    }
  }

  protected String[] getTitles() {
    return Arrays.copyOf(titles, titles.length);
  }

  protected void updateButtons(Object v) {
    initButtons();
    int i = v instanceof Integer ? (int) v : 0;
    buttons.get(0).setSelected((i & (1 << 2)) != 0);
    buttons.get(1).setSelected((i & (1 << 1)) != 0);
    buttons.get(2).setSelected((i & 1) != 0);
  }

  protected void doClickCheckBox(String title) {
    buttons.stream()
        .filter(b -> b.getText().equals(title))
        .findFirst()
        .ifPresent(JCheckBox::doClick);
  }

  protected Integer getPermissionsValue() {
    int i = 0;
    i = buttons.get(0).isSelected() ? 1 << 2 | i : i;
    i = buttons.get(1).isSelected() ? 1 << 1 | i : i;
    i = buttons.get(2).isSelected() ? 1 | i : i;
    return i;
  }
}

class CheckBoxesRenderer implements TableCellRenderer {
  private final CheckBoxesPanel renderer = new CheckBoxesPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.updateButtons(value);
    return renderer;
  }
  // public static class UIResource extends CheckBoxesRenderer implements UIResource {}
}

class CheckBoxesEditor extends AbstractCellEditor implements TableCellEditor {
  private final CheckBoxesPanel renderer = new CheckBoxesPanel();

  protected CheckBoxesEditor() {
    super();
    String[] titles = renderer.getTitles();
    ActionMap am = renderer.getActionMap();
    Stream.of(titles).forEach(t -> am.put(t, new AbstractAction(t) {
      @Override public void actionPerformed(ActionEvent e) {
        renderer.doClickCheckBox(t);
        fireEditingStopped();
      }
    }));
    InputMap im = renderer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), titles[0]);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), titles[1]);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), titles[2]);
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    renderer.updateButtons(value);
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return renderer.getPermissionsValue();
  }
}

// // TEST:
// class CheckBoxesEditor extends CheckBoxesPanel implements TableCellEditor {
//   private transient ChangeEvent changeEvent;
//
//   @Override public void updateUI() {
//     super.updateUI();
//     EventQueue.invokeLater(() -> {
//       ActionMap am = getActionMap();
//       for (int i = 0; i < buttons.length; i++) {
//         String t = titles[i];
//         am.put(t, new AbstractAction(t) {
//           @Override public void actionPerformed(ActionEvent e) {
//             for (JCheckBox b : buttons) {
//               if (b.getText().equals(t)) {
//                 b.doClick();
//                 break;
//               }
//             }
//             fireEditingStopped();
//           }
//         });
//       }
//       InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//       im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), titles[0]);
//       im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), titles[1]);
//       im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), titles[2]);
//     });
//   }
//
//   @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//     updateButtons(value);
//     return this;
//   }
//
//   @Override public Object getCellEditorValue() {
//     int i = 0;
//     i = buttons.get(0).isSelected() ? 1 << 2 | i : i;
//     i = buttons.get(1).isSelected() ? 1 << 1 | i : i;
//     i = buttons.get(2).isSelected() ? 1 << 0 | i : i;
//     // if (buttons.get(0).isSelected()) { i |= 1 << 2; }
//     // if (buttons.get(1).isSelected()) { i |= 1 << 1; }
//     // if (buttons.get(2).isSelected()) { i |= 1 << 0; }
//     return i;
//   }
//
//   // Copied from AbstractCellEditor
//   // protected EventListenerList listenerList = new EventListenerList();
//   // protected transient ChangeEvent changeEvent;
//   @Override public boolean isCellEditable(EventObject e) {
//     return true;
//   }
//
//   @Override public boolean shouldSelectCell(EventObject anEvent) {
//     return true;
//   }
//
//   @Override public boolean stopCellEditing() {
//     fireEditingStopped();
//     return true;
//   }
//
//   @Override public void cancelCellEditing() {
//     fireEditingCanceled();
//   }
//
//   @Override public void addCellEditorListener(CellEditorListener l) {
//     listenerList.add(CellEditorListener.class, l);
//   }
//
//   @Override public void removeCellEditorListener(CellEditorListener l) {
//     listenerList.remove(CellEditorListener.class, l);
//   }
//
//   public CellEditorListener[] getCellEditorListeners() {
//     return listenerList.getListeners(CellEditorListener.class);
//   }
//
//   protected void fireEditingStopped() {
//     // Guaranteed to return a non-null array
//     Object[] listeners = listenerList.getListenerList();
//     // Process the listeners last to first, notifying
//     // those that are interested in this event
//     for (int i = listeners.length - 2; i >= 0; i -= 2) {
//       if (listeners[i] == CellEditorListener.class) {
//         // Lazily create the event:
//         if (Objects.isNull(changeEvent)) {
//           changeEvent = new ChangeEvent(this);
//         }
//         ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
//       }
//     }
//   }
//
//   protected void fireEditingCanceled() {
//     // Guaranteed to return a non-null array
//     Object[] listeners = listenerList.getListenerList();
//     // Process the listeners last to first, notifying
//     // those that are interested in this event
//     for (int i = listeners.length - 2; i >= 0; i -= 2) {
//       if (listeners[i] == CellEditorListener.class) {
//         // Lazily create the event:
//         if (Objects.isNull(changeEvent)) {
//           changeEvent = new ChangeEvent(this);
//         }
//         ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
//       }
//     }
//   }
// }
