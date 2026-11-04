// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultTableModel model = createModel();
    JTable table = new AdjustRowHeightTable(model);
    JScrollPane scroll = createScrollPane(table);
    JButton button = new JButton("add");
    button.addActionListener(e -> model.addRow(new Object[] {"", 0, false}));
    add(scroll);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTableModel createModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true},
        {"bbb", 5, false},
        {"CCC", 92, true},
        {"DDD", 0, false},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private static JScrollPane createScrollPane(Component comp) {
    return new JScrollPane(comp) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
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

class AdjustRowHeightTable extends JTable {
  private int prevHeight = -1;
  private int prevRowCount = -1;

  protected AdjustRowHeightTable(TableModel model) {
    super(model);
  }

  @Override public boolean getScrollableTracksViewportHeight() {
    // Always follow the viewport height so that doLayout (and thus
    // adjustRowHeights) runs again whenever the viewport is resized
    return getParent() instanceof JViewport;
  }

  @Override public void doLayout() {
    super.doLayout();
    Container parent = getParent();
    if (parent instanceof JViewport) {
      adjustRowHeights(((JViewport) parent).getExtentSize().height);
    }
  }

  private void adjustRowHeights(int height) {
    // Use the view row count, setRowHeight(int, int) takes a view index
    int rowCount = getRowCount();
    // setRowHeight(...) revalidates the table and calls doLayout() again,
    // so skip the update if neither the height nor the row count has changed
    if (rowCount > 0 && (height != prevHeight || rowCount != prevRowCount)) {
      int baseRowHeight = height / rowCount;
      if (baseRowHeight > 0) {
        int remainder = height % rowCount;
        // Distribute the remainder one pixel at a time to the first rows
        for (int i = 0; i < rowCount; i++) {
          setRowHeight(i, baseRowHeight + (i < remainder ? 1 : 0));
        }
      }
    }
    prevHeight = height;
    prevRowCount = rowCount;
  }
}

// // TEST when not considering adding rows
// class TableRowHeightAdjuster extends ComponentAdapter {
//   private int prevHeight = -1;
//
//   @Override public void componentResized(ComponentEvent e) {
//     Component c = e.getComponent();
//     if (c instanceof JScrollPane) {
//       JScrollPane scroll = (JScrollPane) c;
//       JTable table = (JTable) scroll.getViewport().getView();
//       int height = scroll.getViewportBorderBounds().height;
//       int rowCount = table.getModel().getRowCount();
//       int rowHeight = height / rowCount;
//       if (height != prevHeight && rowHeight > 0) {
//         int remainder = height % rowCount;
//         for (int i = 0; i < rowCount; i++) {
//           int a = remainder > 0 ? i == rowCount - 1 ? remainder : 1 : 0;
//           table.setRowHeight(i, rowHeight + a);
//           remainder--;
//         }
//       }
//       prevHeight = height;
//     }
//   }
// }
