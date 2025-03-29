// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellEditor;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table1 = new JTable(50, 50);
    table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    JTable table2 = new JTable(50, 50) {
      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Rectangle r = getCellRect(row, column, true);
        Container p = SwingUtilities.getAncestorOfClass(JViewport.class, this);
        if (p instanceof JViewport) {
          Rectangle viewRect = ((JViewport) p).getViewRect();
          if (viewRect.intersects(r)) {
            r.grow(r.width / 4, 0);
          } else {
            r.grow((viewRect.width - r.width) / 2, 0);
          }
          scrollRectToVisible(r);
        }
        return super.prepareEditor(editor, row, column);
      }

      @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        Rectangle r = getCellRect(rowIndex, columnIndex, true);
        r.grow(r.width / 4, 0);
        scrollRectToVisible(r);
      }
    };
    table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    String help1 = "Default: F2:startEditing not scroll";
    Component s1 = makeTitledPane(new JScrollPane(table1), help1);
    String help2 = "F2:startEditing scrollRectToVisible(...)";
    Component s2 = makeTitledPane(new JScrollPane(table2), help2);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, s1, s2);
    split.setResizeWeight(.5);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPane(Component c, String title) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
