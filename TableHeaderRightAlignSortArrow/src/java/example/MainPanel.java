// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);

    String key = "TableHeader.rightAlignSortArrow";
    JCheckBox check = new JCheckBox(key, UIManager.getBoolean(key)) {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        EventQueue.invokeLater(() -> {
          boolean b = UIManager.getLookAndFeelDefaults().getBoolean(key);
          setSelected(b);
          updateSortAlign(table, key, b);
        });
      }
    };
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      updateSortAlign(table, key, b);
    });

    // // NimbusLookAndFeel TEST:
    // if (UIManager.getLookAndFeel().getClass().getName().contains("Nimbus")) {
    //   UIManager.put(key, Boolean.FALSE);
    //   TableCellRenderer renderer = (t, value, isSelected, hasFocus, row, column) -> {
    //     TableCellRenderer r = t.getTableHeader().getDefaultRenderer();
    //     Component c = r.getTableCellRendererComponent(
    //         t, value, isSelected, hasFocus, row, column);
    //     if (c instanceof JLabel) {
    //       JLabel l = (JLabel) c;
    //       UIDefaults d = new UIDefaults();
    //       d.put(key, Boolean.FALSE);
    //       l.putClientProperty("Nimbus.Overrides", d);
    //       l.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
    //     }
    //     return c;
    //   };
    //   TableColumnModel cm = table.getColumnModel();
    //   for (int i = 1; i < cm.getColumnCount(); i++) {
    //     cm.getColumn(i).setHeaderRenderer(renderer);
    //   }
    // }

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(Box.createHorizontalGlue());
    mb.add(check);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(new JScrollPane(table));
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

  public static void updateSortAlign(JTable table, String key, boolean b) {
    UIManager.put(key, b);
    table.getTableHeader().setCursor(Cursor.getDefaultCursor());
    SwingUtilities.updateComponentTreeUI(table);
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

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
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
