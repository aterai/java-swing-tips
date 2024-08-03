// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    TableModel model = makeModel();
    JTable table1 = new JTable(model);
    table1.setAutoCreateRowSorter(true);
    table1.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    JTable table2 = makeTable(model);
    table2.setAutoCreateRowSorter(true);
    table2.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    JScrollPane s1 = new JScrollPane(table1);
    JScrollPane s2 = new JScrollPane(table2);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, s1, s2);
    split.setResizeWeight(.5);
    add(split);

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

  private static JTable makeTable(TableModel model) {
    return new JTable(model) {
      @Override public void updateUI() {
        // Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
        // https://bugs.openjdk.org/browse/JDK-6788475
        // Set a temporary ColorUIResource to avoid this issue
        setSelectionForeground(new ColorUIResource(Color.RED));
        setSelectionBackground(new ColorUIResource(Color.RED));
        super.updateUI();
        updateRenderer();
        TableCellEditor editor = new DefaultCellEditor(new BooleanCellEditor());
        setDefaultEditor(Boolean.class, editor);
      }

      private void updateRenderer() {
        TableModel m = getModel();
        for (int i = 0; i < m.getColumnCount(); i++) {
          TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
          if (r instanceof Component) {
            SwingUtilities.updateComponentTreeUI((Component) r);
          }
        }
      }

      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JCheckBox) {
          JCheckBox b = (JCheckBox) c;
          b.setBackground(getSelectionBackground());
          b.setHorizontalAlignment(SwingConstants.CENTER);
          b.setBorderPainted(true);
        }
        return c;
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

class BooleanCellEditor extends JCheckBox {
  private transient MouseAdapter handler;

  @Override public void updateUI() {
    removeMouseListener(handler);
    super.updateUI();
    // setHorizontalAlignment(SwingConstants.CENTER);
    // setBorderPainted(true);
    setOpaque(true);
    handler = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        JCheckBox cb = (JCheckBox) e.getComponent();
        JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, cb);
        int editingRow = table.getEditingRow();
        ButtonModel m = cb.getModel();
        if (m.isPressed() && table.isRowSelected(editingRow) && e.isControlDown()) {
          if (editingRow % 2 == 0) {
            cb.setOpaque(false);
            // cb.setBackground(getBackground());
          } else {
            cb.setOpaque(true);
            cb.setBackground(UIManager.getColor("Table.alternateRowColor"));
          }
        } else {
          cb.setBackground(table.getSelectionBackground());
          cb.setOpaque(true);
        }
      }

      @Override public void mouseExited(MouseEvent e) {
        // in order to drag table row selection
        JCheckBox cb = (JCheckBox) e.getComponent();
        JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, cb);
        if (table.isEditing() && !table.getCellEditor().stopCellEditing()) {
          table.getCellEditor().cancelCellEditing();
        }
      }
    };
    addMouseListener(handler);
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
