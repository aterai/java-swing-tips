// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    URL[] icons = {
        getUrl("wi0062-16.png"), getUrl("wi0063-16.png"), getUrl("wi0064-16.png")
    };
    String[] columnNames = {"Column1", "Column2", "Column3"};
    JTable table = new JTable(new DefaultTableModel(columnNames, 8));
    TableColumnModel m = table.getColumnModel();
    for (int i = 0; i < m.getColumnCount(); i++) {
      // m.getColumn(i).setHeaderRenderer(new IconColumnHeaderRenderer());
      // m.getColumn(i).setHeaderRenderer(new HtmlIconHeaderRenderer());
      String td = String.format("<td><img src='%s'/></td>&nbsp;%s", icons[i], columnNames[i]);
      m.getColumn(i).setHeaderValue("<html><table cellpadding='0' cellspacing='0'>" + td);
    }
    table.setAutoCreateRowSorter(true);
    add(new JScrollPane(table));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setPreferredSize(new Dimension(320, 240));
  }

  private URL getUrl(String str) {
    String path = "example/" + str;
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return cl.getResource(path);
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

// // TEST: LookAndFeel
// class IconColumnHeaderRenderer implements TableCellRenderer {
//   private final Icon icon = new ImageIcon(getClass().getResource("wi0063-16.png"));
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
//     Component c = r.getTableCellRendererComponent(
//         table, value, isSelected, hasFocus, row, column);
//     if (c instanceof JLabel) {
//       JLabel l = (JLabel) c;
//       l.setHorizontalTextPosition(SwingConstants.RIGHT);
//       l.setIcon(icon);
//     }
//     return c;
//   }
// }

// TEST: html baseline
// class HtmlIconHeaderRenderer implements TableCellRenderer {
//   private final URL url = getClass().getResource("wi0063-16.png");
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
//     String str = Objects.toString(value, "");
//     String html = String.format("<html><img src='%s'/>&nbsp;%s", url, str);
//     // = String.format("<html><table><td cellpadding='0'><img src='%s'/>%s", url, str);
//     return r.getTableCellRendererComponent(table, html, isSelected, hasFocus, row, column);
//   }
// }

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
