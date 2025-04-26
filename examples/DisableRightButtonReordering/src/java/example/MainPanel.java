// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.JTableHeader;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    String help1 = "Default";
    add(makeTitledPanel(help1, new JScrollPane(makeTable())));

    String help2 = "Disable right mouse button reordering";
    JScrollPane scroll = new JScrollPane(makeTable());
    LayerUI<JScrollPane> layerUI = new DisableRightButtonSwapLayerUI();
    add(makeTitledPanel(help2, new JLayer<>(scroll, layerUI)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable() {
    JTable table = new JTable(4, 3);
    table.setAutoCreateRowSorter(true);
    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component c, int x, int y) {
        if (c instanceof JTableHeader) {
          JTableHeader header = (JTableHeader) c;
          header.setDraggedColumn(null);
          header.repaint();
          header.getTable().repaint();
          super.show(c, x, y);
        }
      }
    };
    popup.add("Item 1");
    popup.add("Item 2");
    popup.add("Item 3");
    table.getTableHeader().setComponentPopupMenu(popup);
    return table;
  }

  private static Component makeTitledPanel(String title, Component c) {
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

class DisableRightButtonSwapLayerUI extends LayerUI<JScrollPane> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    boolean isDrag = e.getID() == MouseEvent.MOUSE_DRAGGED;
    boolean isRight = SwingUtilities.isRightMouseButton(e);
    if (e.getComponent() instanceof JTableHeader && isDrag && isRight) {
      e.consume();
    }
  }
}
