// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
    ListModel<String> model = makeModel();
    add(makeScrollPane(makeList(model)));
    add(new JLayer<>(makeScrollPane(makeList(model)), new StickyLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<String> makeModel() {
    DefaultListModel<String> model = new DefaultListModel<>();
    for (int i = 0; i < 100; i++) {
      String indent = i % 10 == 0 ? "" : "    ";
      LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
      model.addElement(String.format("%s%04d: %s", indent, i, now));
    }
    return model;
  }

  private static Component makeList(ListModel<String> m) {
    JList<String> list = new JList<>(m);
    list.setFixedCellHeight(32);
    return list;
  }

  private static JScrollPane makeScrollPane(Component c) {
    return new JScrollPane(c) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(4);
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
  private final JPanel renderer = new JPanel();
  private int currentHeaderIdx = -1;
  private int nextHeaderIdx = -1;

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

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseMotionEvent(e, l);
    Component c = l.getView().getViewport().getView();
    if (e.getID() == MouseEvent.MOUSE_DRAGGED && c instanceof JList) {
      update((JList<?>) c);
    }
  }

  @Override protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseWheelEvent(e, l);
    Component c = l.getView().getViewport().getView();
    if (c instanceof JList) {
      update((JList<?>) c);
    }
  }

  private void update(JList<?> list) {
    int idx = list.getFirstVisibleIndex();
    if (idx >= 0) {
      currentHeaderIdx = getHeaderIndex1(list, idx);
      nextHeaderIdx = getNextHeaderIndex1(list, idx);
    } else {
      currentHeaderIdx = -1;
      nextHeaderIdx = -1;
    }
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    JList<?> list = getList(c);
    if (list != null && currentHeaderIdx >= 0) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Rectangle headerRect = scroll.getViewport().getBounds();
      headerRect.height = list.getFixedCellHeight();
      Graphics2D g2 = (Graphics2D) g.create();
      int firstVisibleIdx = list.getFirstVisibleIndex();
      if (firstVisibleIdx + 1 == nextHeaderIdx) {
        Dimension d = headerRect.getSize();
        Component c1 = getComponent(list, currentHeaderIdx);
        Rectangle r1 = getHeaderRect(list, firstVisibleIdx, c, d);
        SwingUtilities.paintComponent(g2, c1, renderer, r1);
        Component c2 = getComponent(list, nextHeaderIdx);
        Rectangle r2 = getHeaderRect(list, nextHeaderIdx, c, d);
        SwingUtilities.paintComponent(g2, c2, renderer, r2);
      } else {
        int idx = firstVisibleIdx == nextHeaderIdx ? nextHeaderIdx : currentHeaderIdx;
        Component c1 = getComponent(list, idx);
        SwingUtilities.paintComponent(g2, c1, renderer, headerRect);
      }
      g2.dispose();
    }
  }

  private static JList<?> getList(JComponent layer) {
    JList<?> list = null;
    if (layer instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) layer).getView();
      Component view = scroll.getViewport().getView();
      if (view instanceof JList) {
        list = (JList<?>) view;
      }
    }
    return list;
  }

  private static int getHeaderIndex1(JList<?> list, int start) {
    return list.getNextMatch("0", start, Position.Bias.Backward);
  }

  private static int getNextHeaderIndex1(JList<?> list, int start) {
    return list.getNextMatch("0", start, Position.Bias.Forward);
  }

  private static Rectangle getHeaderRect(JList<?> list, int i, Component dst, Dimension d) {
    Rectangle r = SwingUtilities.convertRectangle(list, list.getCellBounds(i, i), dst);
    r.setSize(d);
    return r;
  }

  private static <E> Component getComponent(JList<E> list, int idx) {
    E value = list.getModel().getElementAt(idx);
    ListCellRenderer<? super E> r = list.getCellRenderer();
    Component c = r.getListCellRendererComponent(list, value, idx, false, false);
    c.setBackground(Color.GRAY);
    c.setForeground(Color.WHITE);
    return c;
  }
}
