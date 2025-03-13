// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ListModel<String> m = makeModel();
    Component l = makeScrollPane(makeList(m));
    Component r = new JLayer<>(makeScrollPane(makeList(m)), new RolloverLayerUI());
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, l, r);
    split.setResizeWeight(.5);
    add(split);
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

  private static <E> JList<E> makeList(ListModel<E> m) {
    JList<E> list = new JList<E>(m) {
      @Override public String getToolTipText(MouseEvent e) {
        int idx = locationToIndex(e.getPoint());
        E value = getModel().getElementAt(idx);
        return value == null ? super.getToolTipText(e) : Objects.toString(value);
      }
    };
    ListCellRenderer<? super E> renderer = list.getCellRenderer();
    list.setCellRenderer((lst, value, index, isSelected, cellHasFocus) ->
        renderer.getListCellRendererComponent(lst, value, index, isSelected, false));
    list.setComponentPopupMenu(makePopupMenu());
    return list;
  }

  private static JPopupMenu makePopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    popup.add("JMenuItem 1");
    popup.add("JMenuItem 2");
    popup.add("JMenuItem 3");
    popup.addSeparator();
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
    return popup;
  }

  private static JScrollPane makeScrollPane(JComponent c) {
    return new JScrollPane(c) {
      @Override public void updateUI() {
        super.updateUI();
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
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

class RolloverLayerUI extends LayerUI<JScrollPane> {
  private final JPanel renderer = new JPanel();
  private int rolloverIdx = -1;
  private final Point loc = new Point(-100, -100);
  private final JButton button = new JButton(new ThreeDotsIcon()) {
    @Override public void updateUI() {
      super.updateUI();
      setBorderPainted(false);
      setContentAreaFilled(false);
      setFocusPainted(false);
      setFocusable(false);
      setOpaque(false);
      setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }
  };

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseEvent(e, l);
    Component c = e.getComponent();
    if (c instanceof JList) {
      JList<?> list = (JList<?>) c;
      int id = e.getID();
      if (id == MouseEvent.MOUSE_CLICKED && SwingUtilities.isLeftMouseButton(e)) {
        Rectangle r = list.getCellBounds(rolloverIdx, rolloverIdx);
        Dimension d = button.getPreferredSize();
        r.width = l.getView().getViewportBorderBounds().width - d.width;
        JPopupMenu popup = ((JComponent) c).getComponentPopupMenu();
        Point pt = e.getPoint();
        if (popup != null && !r.contains(pt)) {
          popup.show(c, pt.x, pt.y);
        }
      } else if (id == MouseEvent.MOUSE_EXITED && rolloverIdx >= 0) {
        list.repaint(list.getCellBounds(rolloverIdx, rolloverIdx));
        rolloverIdx = -1;
        loc.setLocation(-100, -100);
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    super.processMouseMotionEvent(e, l);
    Component c = e.getComponent();
    if (e.getID() == MouseEvent.MOUSE_MOVED && c instanceof JList) {
      JList<?> list = (JList<?>) c;
      Point pt = e.getPoint();
      loc.setLocation(pt);
      int prev = rolloverIdx;
      rolloverIdx = list.locationToIndex(pt);
      // #30 ThreeDotsMenuButton: If you scroll too fast, multiple layers will be displayed
      if (rolloverIdx >= 0) {
        Rectangle r = list.getCellBounds(rolloverIdx, rolloverIdx);
        r.width = l.getView().getViewportBorderBounds().width;
        r.grow(0, r.height);
        Rectangle rr = prev >= 0 ? r.union(list.getCellBounds(prev, prev)) : r;
        list.repaint(rr);
      }
    }
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    JList<?> list = getList(c);
    if (list != null && rolloverIdx >= 0) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Graphics2D g2 = (Graphics2D) g.create();
      Rectangle cellBounds = list.getCellBounds(rolloverIdx, rolloverIdx);
      Component rc = getRendererComponent(list, rolloverIdx);
      Dimension d = button.getPreferredSize();
      cellBounds.width = scroll.getViewportBorderBounds().width - d.width;
      boolean buttonRollover = !cellBounds.contains(loc);
      button.getModel().setRollover(buttonRollover);
      Rectangle rect = SwingUtilities.convertRectangle(list, cellBounds, c);
      SwingUtilities.paintComponent(g2, rc, renderer, rect);
      rect.x += rect.width;
      rect.width = d.width;
      g2.setPaint(rc.getBackground());
      g2.fill(rect);
      SwingUtilities.paintComponent(g2, button, renderer, rect);
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

  private static <E> Component getRendererComponent(JList<E> list, int idx) {
    E value = list.getModel().getElementAt(idx);
    ListCellRenderer<? super E> r = list.getCellRenderer();
    boolean isSelected = list.isSelectedIndex(idx);
    boolean cellHasFocus = list.getSelectionModel().getLeadSelectionIndex() == idx;
    Component c = r.getListCellRendererComponent(list, value, idx, isSelected, cellHasFocus);
    if (!isSelected) {
      c.setBackground(Color.GRAY);
      c.setForeground(Color.WHITE);
    }
    return c;
  }
}

class ThreeDotsIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof JButton) {
      JButton button = (JButton) c;
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Rectangle r = SwingUtilities.calculateInnerArea(button, null);
      boolean rollover = button.getModel().isRollover();
      if (rollover) {
        g2.setPaint(Color.DARK_GRAY);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 2, 2);
      }
      g2.translate(x, y);
      g2.setPaint(rollover ? Color.WHITE : Color.LIGHT_GRAY);
      int count = 3;
      int diff = 4;
      int firstColumn = (r.width - count * diff) / 2;
      int firstRow = getIconHeight() / 2;
      for (int i = 0; i < count; i++) {
        int column = firstColumn + i * diff;
        g2.fillRect(column, firstRow, 2, 2);
      }
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return 20;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}
