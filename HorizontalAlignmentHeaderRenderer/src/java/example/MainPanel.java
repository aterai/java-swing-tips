// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table0 = makeTable();
    TableCellRenderer renderer0 = table0.getTableHeader().getDefaultRenderer();
    if (renderer0 instanceof JLabel) {
      ((JLabel) renderer0).setHorizontalAlignment(SwingConstants.CENTER);
    }

    JTable table1 = makeTable();
    table1.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel) {
          ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        }
        return c;
      }
    });

    JTable table2 = makeTable();
    TableColumn c0 = table2.getColumnModel().getColumn(0);
    c0.setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.LEFT));
    TableColumn c1 = table2.getColumnModel().getColumn(1);
    c1.setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.CENTER));
    TableColumn c2 = table2.getColumnModel().getColumn(2);
    c2.setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.RIGHT));

    // // LnF NullPointerException
    // JTable table3 = makeTable();
    // TableCellRenderer r = table3.getTableHeader().getDefaultRenderer();
    // table3.getTableHeader().setDefaultRenderer((t, v, isSelected, hasFocus, row, column) -> {
    //   Component c = r.getTableCellRendererComponent(t, v, isSelected, hasFocus, row, column);
    //   if (t.convertColumnIndexToModel(column) == 0) {
    //     ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
    //   } else {
    //     ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
    //   }
    //   return c;
    // });

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Default", new JScrollPane(makeTable()));
    tabs.addTab("Test0", new JScrollPane(table0));
    tabs.addTab("Test1", new JScrollPane(table1));
    tabs.addTab("Test2", new JScrollPane(table2));
    // tabs.addTab("Test3", new JScrollPane(table3));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  public static JTable makeTable() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {{"aa", 12, true}, {"bb", 5, false}, {"CC", 92, true}, {"DD", 0, false}};
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    return table;
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

class HorizontalAlignmentHeaderRenderer implements TableCellRenderer {
  private final int horizAlignment; // = SwingConstants.LEFT;

  protected HorizontalAlignmentHeaderRenderer(int horizAlignment) {
    this.horizAlignment = horizAlignment;
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      ((JLabel) c).setHorizontalAlignment(horizAlignment);
    }
    return c;
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
