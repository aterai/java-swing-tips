// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.Collator;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("<html>Test<p>Test</p></html>", new JLabel("Test1"));
    // The tags do not have to be well-formed
    tabbedPane.addTab("<html>Test<p>test", new JLabel("Test2"));

    TableModel model = makeModel();
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
    // Sort by the displayed text instead of the raw "<html>..." markup
    Collator collator = Collator.getInstance();
    sorter.setComparator(0, Comparator.comparing(MainPanel::removeHtmlTags, collator));
    JTable table = new JTable(model);
    table.setRowSorter(sorter);
    table.setRowHeight(32);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

    add(tabbedPane, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "<html><span style='color:red'>Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"<html><span style='color:blue'>bbb", 5, false},
        {"CCC", 92, true}, {"<html><span style='color:green'>DDD", 0, false},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private static String removeHtmlTags(String text) {
    return HTML_TAG.matcher(text).replaceAll("");
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
      Logger.getGlobal().severe(ex::getMessage);
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
