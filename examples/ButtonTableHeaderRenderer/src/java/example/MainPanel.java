// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTable table0 = new JTable(makeModel());
    table0.setAutoCreateRowSorter(true);
    add(new JScrollPane(table0));

    JTable table1 = new JTable(makeModel()) {
      private transient MouseAdapter listener;

      @Override public void updateUI() {
        getTableHeader().removeMouseListener(listener);
        getTableHeader().removeMouseMotionListener(listener);
        super.updateUI();
        setAutoCreateRowSorter(true);
        JTableHeader header = getTableHeader();
        header.setCursor(Cursor.getDefaultCursor()); // MotifLookAndFeel???
        header.setDefaultRenderer(new ButtonHeaderRenderer());
        listener = new HeaderMouseListener();
        header.addMouseListener(listener);
        header.addMouseMotionListener(listener);
      }
    };
    add(new JScrollPane(table1));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
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

class ButtonHeaderRenderer extends JButton implements TableCellRenderer {
  private int pressedColumn = -1;
  private int rolloverColumn = -1;

  @Override public void updateUI() {
    super.updateUI();
    setHorizontalTextPosition(LEFT);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setText(Objects.toString(value, ""));
    int modelColumn = table.convertColumnIndexToModel(column);
    JTableHeader header = table.getTableHeader();
    if (header != null) {
      // setColor(header, hasFocus);
      ButtonModel model = getModel();
      boolean isPressed = modelColumn == pressedColumn;
      model.setPressed(isPressed);
      model.setArmed(isPressed);
      model.setRollover(modelColumn == rolloverColumn);
      model.setSelected(isSelected);
      setFont(header.getFont());
    }

    Icon sortIcon = null;
    if (table.getRowSorter() != null) {
      List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
      if (!sortKeys.isEmpty() && sortKeys.get(0).getColumn() == modelColumn) {
        SortOrder sortOrder = sortKeys.get(0).getSortOrder();
        switch (sortOrder) {
          case ASCENDING:
            sortIcon = UIManager.getIcon("Table.ascendingSortIcon");
            break;
          case DESCENDING:
            sortIcon = UIManager.getIcon("Table.descendingSortIcon");
            break;
          // case UNSORTED:
          //   sortIcon = UIManager.getIcon("Table.naturalSortIcon");
          //   break;
          default:
            sortIcon = UIManager.getIcon("Table.naturalSortIcon");
        }
      }
    }
    setIcon(sortIcon);
    return this;
  }

  // private void setColor(JTableHeader header, boolean hasFocus) {
  //   Color fgColor = null;
  //   Color bgColor = null;
  //   if (hasFocus) {
  //     fgColor = UIManager.getColor("TableHeader.focusCellForeground");
  //     bgColor = UIManager.getColor("TableHeader.focusCellBackground");
  //   }
  //   if (fgColor == null) {
  //     fgColor = header.getForeground();
  //   }
  //   if (bgColor == null) {
  //     bgColor = header.getBackground();
  //   }
  //   setForeground(fgColor);
  //   setBackground(bgColor);
  // }

  public void setPressedColumn(int column) {
    pressedColumn = column;
  }

  public void setRolloverColumn(int column) {
    rolloverColumn = column;
  }
}

class HeaderMouseListener extends MouseAdapter {
  @Override public void mousePressed(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    JTable table = header.getTable();
    // if (table.isEditing()) {
    //   table.getCellEditor().stopCellEditing();
    // }
    TableCellRenderer renderer = header.getDefaultRenderer();
    int viewColumn = table.columnAtPoint(e.getPoint());
    if (viewColumn >= 0 && renderer instanceof ButtonHeaderRenderer) {
      int column = table.convertColumnIndexToModel(viewColumn);
      ((ButtonHeaderRenderer) renderer).setPressedColumn(column);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    TableCellRenderer renderer = header.getDefaultRenderer();
    if (renderer instanceof ButtonHeaderRenderer) {
      ((ButtonHeaderRenderer) renderer).setPressedColumn(-1);
    }
  }

  @Override public void mouseMoved(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    JTable table = header.getTable();
    TableCellRenderer renderer = header.getDefaultRenderer();
    int viewColumn = table.columnAtPoint(e.getPoint());
    if (viewColumn >= 0 && renderer instanceof ButtonHeaderRenderer) {
      int column = table.convertColumnIndexToModel(viewColumn);
      ((ButtonHeaderRenderer) renderer).setRolloverColumn(column);
    }
  }

  @Override public void mouseExited(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    TableCellRenderer renderer = header.getDefaultRenderer();
    if (renderer instanceof ButtonHeaderRenderer) {
      ((ButtonHeaderRenderer) renderer).setRolloverColumn(-1);
    }
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
