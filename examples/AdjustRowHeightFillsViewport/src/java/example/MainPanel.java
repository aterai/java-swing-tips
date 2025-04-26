// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultTableModel model = makeModel();
    JTable table = new AdjustRowHeightTable(model);
    JScrollPane scroll = makeScrollPane(table);
    JButton button = new JButton("add");
    button.addActionListener(e -> model.addRow(new Object[] {"", 0, false}));
    add(scroll);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTableModel makeModel() {
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

  private JScrollPane makeScrollPane(Component comp) {
    return new JScrollPane(comp) {
      private transient ComponentListener listener;

      @Override public void updateUI() {
        removeComponentListener(listener);
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        listener = new ComponentAdapter() {
          @Override public void componentResized(ComponentEvent e) {
            Component c = e.getComponent();
            if (c instanceof JScrollPane) {
              ((JScrollPane) c).getViewport().getView().revalidate();
            }
          }
        };
        addComponentListener(listener);
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
  private int prevCount = -1;

  protected AdjustRowHeightTable(TableModel model) {
    super(model);
  }

  @Override public void doLayout() {
    super.doLayout();
    Class<JViewport> clz = JViewport.class;
    Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
        .filter(clz::isInstance)
        .map(clz::cast)
        .ifPresent(this::updateRowsHeight);
  }

  private void updateRowsHeight(JViewport viewport) {
    int height = viewport.getExtentSize().height;
    int rowCount = getModel().getRowCount();
    int defaultRowHeight = height / rowCount;
    if ((height != prevHeight || rowCount != prevCount) && defaultRowHeight > 0) {
      int remainder = height % rowCount;
      for (int i = 0; i < rowCount; i++) {
        int a = Math.min(1, Math.max(0, remainder--));
        setRowHeight(i, defaultRowHeight + a);
      }
    }
    prevHeight = height;
    prevCount = rowCount;
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
