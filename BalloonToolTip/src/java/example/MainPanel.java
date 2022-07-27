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
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
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

    JList<String> list1 = new JList<String>(model) {
      @Override public JToolTip createToolTip() {
        JToolTip tip = new BalloonToolTip();
        tip.setComponent(this);
        return tip;
      }

      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TooltipListCellRenderer<>());
      }
    };

    JList<String> list2 = new JList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TooltipListCellRenderer<>());
      }
    };

    add(makeTitledPanel("BalloonToolTip", list1));
    add(makeTitledPanel("Default JToolTip", list2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
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

class TooltipListCellRenderer<E> implements ListCellRenderer<E> {
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    // Insets i = l.getInsets();
    // Container c = SwingUtilities.getAncestorOfClass(JViewport.class, list);
    // Rectangle rect = c.getBounds();
    if (c instanceof JComponent) {
      Class<JViewport> clz = JViewport.class;
      Rectangle rect = Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, list))
          .filter(clz::isInstance).map(clz::cast)
          // .map(JViewport::getBounds)
          .map(v -> SwingUtilities.calculateInnerArea(v, null))
          .orElseGet(Rectangle::new);
      // rect.width -= i.left + i.right;
      FontMetrics fm = c.getFontMetrics(c.getFont());
      String str = Objects.toString(value, "");
      ((JComponent) c).setToolTipText(fm.stringWidth(str) > rect.width ? str : null);
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
        // Window w = SwingUtilities.getWindowAncestor(c);
        // if (w != null && w.getType() == Window.Type.POPUP) {
        //   // Popup$HeavyWeightWindow
        //   w.setBackground(new Color(0x0, true));
        // }
        Optional.ofNullable(SwingUtilities.getWindowAncestor(c))
            .filter(w -> w.getType() == Window.Type.POPUP)
            .ifPresent(w -> w.setBackground(new Color(0x0, true)));
      }
    };
    addHierarchyListener(listener);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(8, 5, 0, 5));
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = 28;
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    Shape s = makeBalloonShape();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fill(s);
    g2.setColor(getForeground());
    g2.draw(s);
    g2.dispose();
    super.paintComponent(g);
  }

  private Shape makeBalloonShape() {
    Insets i = getInsets();
    float w = getWidth() - 1f;
    float h = getHeight() - 1f;
    float v = i.top * .5f;
    Path2D triangle = new Path2D.Float();
    triangle.moveTo(i.left + v + v, 0f);
    triangle.lineTo(i.left + v, v);
    triangle.lineTo(i.left + v + v + v, v);
    Area area = new Area(new RoundRectangle2D.Float(0f, v, w, h - i.bottom - v, i.top, i.top));
    area.add(new Area(triangle));
    return area;
  }
}
