// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));

    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    model.addElement("aaaa");
    model.addElement("aaaabbb");
    model.addElement("aaaabbbcc");
    model.addElement("1234567890abcdefghijklmnopqrstuvwxyz");
    model.addElement("bbb1");
    model.addElement("bbb12");
    model.addElement("1234567890-+*/=ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    model.addElement("bbb123");

    JList<String> list1 = new TooltipList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TooltipListCellRenderer<>());
      }
    };

    JList<String> list2 = new CellRendererTooltipList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TooltipListCellRenderer<>());
      }
    };

    JList<String> list3 = new JList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TooltipListCellRenderer<>());
      }
    };

    LinkedHashMap<String, Component> map = new LinkedHashMap<>();
    map.put("CellBounds", list1);
    map.put("ListCellRenderer", list2);
    map.put("Default location", list3);
    map.forEach((title, c) -> add(makeTitledPanel(title, c)));
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

class TooltipList<E> extends JList<E> {
  protected TooltipList(ListModel<E> m) {
    super(m);
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    Point p = e.getPoint();
    ListCellRenderer<? super E> r = getCellRenderer();
    int i = locationToIndex(p);
    Rectangle cellBounds = getCellBounds(i, i);
    if (i >= 0 && Objects.nonNull(r) && Objects.nonNull(cellBounds) && cellBounds.contains(p.x, p.y)) {
      ListSelectionModel lsm = getSelectionModel();
      boolean hasFocus = hasFocus() && lsm.getLeadSelectionIndex() == i;
      E value = getModel().getElementAt(i);
      Component renderer = r.getListCellRendererComponent(this, value, i, lsm.isSelectedIndex(i), hasFocus);
      if (renderer instanceof JComponent && Objects.nonNull(((JComponent) renderer).getToolTipText())) {
        return cellBounds.getLocation();
      }
    }
    return null;
  }
}

class CellRendererTooltipList<E> extends JList<E> {
  protected final JLabel label = new JLabel();

  protected CellRendererTooltipList(ListModel<E> m) {
    super(m);
    // TEST: label.setBorder(BorderFactory.createLineBorder(Color.RED, 10));
    label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    Point p = e.getPoint();
    int i = locationToIndex(p);
    ListCellRenderer<? super E> r = getCellRenderer();
    Rectangle cellBounds = getCellBounds(i, i);
    if (i >= 0 && Objects.nonNull(r) && Objects.nonNull(cellBounds) && cellBounds.contains(p.x, p.y)) {
      ListSelectionModel lsm = getSelectionModel();
      E str = getModel().getElementAt(i);
      boolean hasFocus = hasFocus() && lsm.getLeadSelectionIndex() == i;
      Component renderer = r.getListCellRendererComponent(this, str, i, lsm.isSelectedIndex(i), hasFocus);
      if (renderer instanceof JComponent && Objects.nonNull(((JComponent) renderer).getToolTipText())) {
        Point pt = cellBounds.getLocation();
        Insets ins = label.getInsets();
        pt.translate(-ins.left, -ins.top);
        label.setIcon(new RendererIcon(renderer, cellBounds));
        return pt;
      }
    }
    return null;
  }

  @Override public JToolTip createToolTip() {
    JToolTip tip = new JToolTip() {
      @Override public Dimension getPreferredSize() {
        Insets i = getInsets();
        Dimension d = label.getPreferredSize();
        return new Dimension(d.width + i.left + i.right, d.height + i.top + i.bottom);
      }
    };
    tip.removeAll();
    tip.setBorder(BorderFactory.createEmptyBorder());
    tip.setLayout(new BorderLayout());
    tip.setComponent(this);
    tip.add(label);
    return tip;
  }
}

class TooltipListCellRenderer<E> implements ListCellRenderer<E> {
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    Insets i = l.getInsets();
    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, list);
    Rectangle rect = c.getBounds();
    rect.width -= i.left + i.right;
    FontMetrics fm = l.getFontMetrics(l.getFont());
    String str = Objects.toString(value, "");
    l.setToolTipText(fm.stringWidth(str) > rect.width ? str : null);
    return l;
  }
}

class RendererIcon implements Icon {
  private final Component renderer;
  private final Rectangle rect;

  protected RendererIcon(Component renderer, Rectangle rect) {
    this.renderer = renderer;
    this.rect = rect;
    rect.setLocation(0, 0);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof Container) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(x, y);
      SwingUtilities.paintComponent(g2, renderer, (Container) c, rect);
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return renderer.getPreferredSize().width;
  }

  @Override public int getIconHeight() {
    return renderer.getPreferredSize().height;
  }
}
