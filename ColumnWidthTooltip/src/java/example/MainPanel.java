// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"aaa", 12, true}, {"bbb", 5, false},
      {"CCC", 92, true}, {"DDD", 0, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };

    JTable table = new JTable(model) {
      private transient MouseInputListener handler;
      @Override public void updateUI() {
        getTableHeader().removeMouseListener(handler);
        getTableHeader().removeMouseMotionListener(handler);
        super.updateUI();
        handler = new ColumnWidthResizeHandler();
        getTableHeader().addMouseListener(handler);
        getTableHeader().addMouseMotionListener(handler);
      }
    };
    add(new JScrollPane(table));
    // JWindow window = new JWindow();
    // JToolTip tip = new JToolTip();
    // window.add(tip);
    // JTable table2 = new JTable(model);
    // table2.getTableHeader().addPropertyChangeListener(e -> {
    //   // System.out.println(e.getPropertyName());
    //   JTableHeader h = (JTableHeader) e.getSource();
    //   TableColumn column = h.getResizingColumn();
    //   if (column != null) {
    //     tip.setTipText(String.format("Width: %dpx", column.getWidth()));
    //     Point pt = h.getHeaderRect(column.getModelIndex()).getLocation();
    //     SwingUtilities.convertPointToScreen(pt, h);
    //     window.pack();
    //     window.setLocation(pt);
    //     window.setVisible(true);
    //   } else {
    //     window.setVisible(false);
    //   }
    // });
    // add(new JScrollPane(table2));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
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

class ColumnWidthResizeHandler extends MouseInputAdapter {
  private final JWindow window = new JWindow();
  private final JToolTip tip = new JToolTip();
  private String prev = "";

  private Point getToolTipLocation(MouseEvent e) {
    Point p = e.getPoint();
    Component c = e.getComponent();
    SwingUtilities.convertPointToScreen(p, c);
    p.translate(0, -tip.getPreferredSize().height);
    return p;
  }

  private static TableColumn getResizingColumn(MouseEvent e) {
    Component c = e.getComponent();
    if (c instanceof JTableHeader) {
      return ((JTableHeader) c).getResizingColumn();
    }
    return null;
  }

  private void updateTooltipText(MouseEvent e) {
    TableColumn column = getResizingColumn(e);
    if (column != null) {
      String txt = String.format("Width: %dpx", column.getWidth());
      tip.setTipText(txt);
      if (prev.length() != txt.length()) {
        window.pack();
      }
      window.setLocation(getToolTipLocation(e));
      prev = txt;
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    if (!window.isVisible() && getResizingColumn(e) != null) {
      window.add(tip);
      window.setAlwaysOnTop(true);
      window.setVisible(true);
    }
    updateTooltipText(e);
  }

  @Override public void mouseReleased(MouseEvent e) {
    window.setVisible(false);
  }
}
