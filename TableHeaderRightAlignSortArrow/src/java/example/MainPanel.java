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
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);

    String key = "TableHeader.rightAlignSortArrow";
    JCheckBox check = new JCheckBox(key, UIManager.getBoolean(key)) {
      @Override public void updateUI() {
        super.updateUI();
        EventQueue.invokeLater(() -> {
          boolean b = UIManager.getLookAndFeelDefaults().getBoolean(key);
          setSelected(b);
          updateSortAlign(table, key, b);
        });
      }
    };
    check.setOpaque(false);
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      updateSortAlign(table, key, b);
    });

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    mb.add(Box.createHorizontalGlue());
    mb.add(check);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

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

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
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

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
