// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    JList<String> list1 = new JList<String>(createModel()) {
      @Override public JToolTip createToolTip() {
        JToolTip tip = new BalloonToolTip();
        tip.setComponent(this);
        return tip;
      }

      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new ToolTipListCellRenderer<>());
      }
    };
    add(createTitledPanel("BalloonToolTip", list1));

    JList<String> list2 = new JList<String>(createModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new ToolTipListCellRenderer<>());
      }
    };
    add(createTitledPanel("Default JToolTip", list2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultListModel<String> createModel() {
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("ABC DEF GHI JKL MNO PQR STU VWX YZ");
    model.addElement("111");
    model.addElement("111222");
    model.addElement("111222333");
    model.addElement("1234567890 abc def ghi jkl mno pqr stu vwx yz");
    model.addElement("bbb1");
    model.addElement("bbb12");
    model.addElement("1234567890-+*/=ABC DEF GHI JKL MNO PQR STU VWX YZ");
    model.addElement("bbb123");
    return model;
  }

  private static Component createTitledPanel(String title, Component c) {
    JScrollPane scroll = new JScrollPane(c);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(scroll);
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

class ToolTipListCellRenderer<E> implements ListCellRenderer<E> {
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    if (c instanceof JComponent) {
      Class<JViewport> clz = JViewport.class;
      Rectangle rect = Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, list))
          .map(v -> SwingUtilities.calculateInnerArea((JComponent) v, null))
          .orElseGet(Rectangle::new);
      // Show the cell text as a tooltip only if it is clipped by the viewport
      FontMetrics fm = c.getFontMetrics(c.getFont());
      String txt = Optional.ofNullable(value)
          .map(Object::toString)
          .filter(str -> fm.stringWidth(str) > rect.width)
          .orElseGet(list::getToolTipText);
      ((JComponent) c).setToolTipText(txt);
    }
    return c;
  }
}

class BalloonToolTip extends JToolTip {
  private transient HierarchyListener listener;

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        Optional.ofNullable(SwingUtilities.getWindowAncestor(c))
            .filter(w -> w.getType() == Window.Type.POPUP)
            .filter(w -> Optional.ofNullable(w.getGraphicsConfiguration())
                .map(GraphicsConfiguration::isTranslucencyCapable).orElse(false))
            .ifPresent(w -> w.setBackground(new Color(0x0, true)));
      }
    };
    addHierarchyListener(listener);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(8, 5, 0, 5));
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    // Keep the default height, but grow it for larger fonts
    d.height = Math.max(d.height, 28);
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    Shape s = createBalloonShape(4d, 6d);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fill(s);
    g2.setColor(getForeground());
    g2.draw(s);
    g2.dispose();
    super.paintComponent(g);
  }

  private Shape createBalloonShape(double triHeight, double arc) {
    // The tail (triangle) starts at the left inset and points upward
    double x = getInsets().left;
    Path2D triangle = new Path2D.Double();
    triangle.moveTo(x + triHeight, triHeight);
    triangle.lineTo(x + triHeight * 2d, 0d);
    triangle.lineTo(x + triHeight * 3d, triHeight);
    triangle.closePath();
    double w = getWidth() - 1d;
    double h = getHeight() - triHeight - 1d;
    Area area = new Area(new RoundRectangle2D.Double(0d, triHeight, w, h, arc, arc));
    area.add(new Area(triangle));
    return area;
  }
}
