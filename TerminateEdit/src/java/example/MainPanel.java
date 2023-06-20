// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = makeTable();

    JCheckBox focusCheck = new JCheckBox("DefaultCellEditor:focusLost", true);
    DefaultCellEditor dce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
    dce.getComponent().addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        if (!focusCheck.isSelected()) {
          return;
        }
        if (table.isEditing()) {
          table.getCellEditor().stopCellEditing();
        }
      }
    });

    JCheckBox headerCheck = new JCheckBox("TableHeader:mousePressed", true);
    table.getTableHeader().addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (!headerCheck.isSelected()) {
          return;
        }
        if (table.isEditing()) {
          table.getCellEditor().stopCellEditing();
        }
      }
    });

    // // Lost newly entered data in the cell when resizing column width
    // // https://bugs.openjdk.org/browse/JDK-4330950
    // table.getTableHeader().addComponentListener(new ComponentAdapter() {
    //   @Override public void componentResized(ComponentEvent e) {
    //     System.out.println("componentResized");
    //     if (table.isEditing()) {
    //       table.getCellEditor().stopCellEditing();
    //     }
    //   }
    // });

    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    JComboBox<? extends Enum<?>> comboBox = new JComboBox<>(AutoResizeMode.values());
    comboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        table.setAutoResizeMode(((AutoResizeMode) e.getItem()).getAutoResizeMode());
      }
    });

    JCheckBox teoflCheck = new JCheckBox("terminateEditOnFocusLost", true);
    teoflCheck.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      table.putClientProperty(c.getText(), c.isSelected());
    });

    JPanel box = new JPanel(new GridLayout(4, 0));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(teoflCheck);
    box.add(focusCheck);
    box.add(headerCheck);
    box.add(comboBox);

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable() {
    String[] columnNames = {"String", "Integer"};
    Object[][] data = {
        {"aaa", 12}, {"bbb", 5}, {"CCC", 92}, {"DDD", 0}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      private final Color evenColor = new Color(0xFA_FA_FA);
      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (isRowSelected(row)) {
          c.setForeground(getSelectionForeground());
          c.setBackground(getSelectionBackground());
        } else {
          c.setForeground(getForeground());
          c.setBackground(row % 2 == 0 ? evenColor : getBackground());
        }
        return c;
      }
    };
    table.setAutoCreateRowSorter(true);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    // table.setSurrendersFocusOnKeystroke(true);
    return table;
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

    JFrame frame2 = new JFrame("@title@" + 2);
    frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame2.getContentPane().add(new MainPanel());
    frame2.pack();
    frame2.setLocation(frame.getX() + 50, frame.getY() + 50);

    frame2.setVisible(true);
    frame.setVisible(true);
  }
}

enum AutoResizeMode {
  OFF(JTable.AUTO_RESIZE_OFF), ALL_COLUMNS(JTable.AUTO_RESIZE_ALL_COLUMNS);
  private final int mode;

  AutoResizeMode(int mode) {
    this.mode = mode;
  }

  public int getAutoResizeMode() {
    return mode;
  }


  @Override public String toString() {
    return "AUTO_RESIZE_" + super.toString();
  }
}
