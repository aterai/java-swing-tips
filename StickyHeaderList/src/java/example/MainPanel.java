// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.text.Position;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    UIManager.put("ScrollBar.minimumThumbSize", new Dimension(12, 20));
    UIManager.put("List.lockToPositionOnScroll", Boolean.FALSE);
    add(new JScrollPane(makeList()));
    add(makeStickyHeaderScrollPane(makeList()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeList() {
    DefaultListModel<String> m = new DefaultListModel<>();
    for (int i = 0; i < 100; i++) {
      String indent = i % 10 == 0 ? "" : "    ";
      LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
      m.addElement(String.format("%s%04d: %s", indent, i, now));
    }
    JList<String> list = new JList<>(m);
    list.setFixedCellHeight(32);
    return list;
  }

  private static Component makeStickyHeaderScrollPane(Component c) {
    JScrollPane scroll = new JScrollPane(c) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(2);
      }
    };
    LayerUI<JScrollPane> layer = new StickyLayerUI();
    return new JLayer<>(scroll, layer);
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

class StickyLayerUI extends LayerUI<JScrollPane> {
  private final JPanel panel = new JPanel();

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_WHEEL_EVENT_MASK
          | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @SuppressWarnings("unchecked")
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      JViewport viewport = scroll.getViewport();
      JList<String> list = (JList<String>) viewport.getView();
      int cellHeight = list.getFixedCellHeight();
      Rectangle viewRect = viewport.getViewRect();
      Point vp = SwingUtilities.convertPoint(viewport, 0, 0, c);
      Point pt1 = SwingUtilities.convertPoint(c, vp, list);
      int idx1 = list.locationToIndex(pt1);
      // Point pt2 = SwingUtilities.convertPoint(c, new Point(vp.x, vp.y + cellHeight), list);
      // int idx2 = list.locationToIndex(pt2);
      Rectangle header1 = new Rectangle(vp.x, vp.y, viewRect.width, cellHeight);
      if (idx1 >= 0) {
        Graphics2D g2 = (Graphics2D) g.create();
        int headerIndex1 = getHeaderIndex1(list, idx1);
        // int headerIndex2 = (idx2 / 5) * 5;
        Component c1 = getComponent(list, headerIndex1);
        int nhi = getNextHeaderIndex1(list, idx1);
        Point nextPt = list.getCellBounds(nhi, nhi).getLocation();
        if (header1.contains(SwingUtilities.convertPoint(list, nextPt, c))) {
          Dimension d = header1.getSize();
          SwingUtilities.paintComponent(g2, c1, panel, getHeaderRect(list, idx1, c, d));
          Component cn = getComponent(list, nhi);
          SwingUtilities.paintComponent(g2, cn, panel, getHeaderRect(list, nhi, c, d));
        } else {
          SwingUtilities.paintComponent(g2, c1, panel, header1);
        }
        // if (headerIndex1 != headerIndex2) {
        //   Component c2 = getComponent(list, headerIndex2);
        //   c2.setBackground(Color.LIGHT_GRAY);
        //   SwingUtilities.paintComponent(g2, c2, p, header2);
        // }
        g2.dispose();
      }
    }
  }

  private static int getHeaderIndex1(JList<String> list, int start) {
    return list.getNextMatch("0", start, Position.Bias.Backward);
  }

  private static int getNextHeaderIndex1(JList<String> list, int start) {
    return list.getNextMatch("0", start, Position.Bias.Forward);
  }

  private static Rectangle getHeaderRect(JList<?> list, int i, Component dst, Dimension d) {
    Rectangle r = SwingUtilities.convertRectangle(list, list.getCellBounds(i, i), dst);
    r.setSize(d);
    return r;
  }

  private static <E> Component getComponent(JList<E> list, int idx) {
    E value = list.getModel().getElementAt(idx);
    ListCellRenderer<? super E> renderer = list.getCellRenderer();
    Component c = renderer.getListCellRendererComponent(list, value, idx, false, false);
    c.setBackground(Color.GRAY);
    c.setForeground(Color.WHITE);
    return c;
  }
}
