// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    model.addElement("111");
    model.addElement("111222");
    model.addElement("11122233");
    model.addElement("123456789012345678901234567890");
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

    Map<String, Component> map = Collections.synchronizedMap(new LinkedHashMap<>());
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

class TooltipList<E> extends JList<E> {
  protected TooltipList(ListModel<E> m) {
    super(m);
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    Point p = e.getPoint();
    ListCellRenderer<? super E> r = getCellRenderer();
    int i = locationToIndex(p);
    Rectangle cellBounds = getCellBounds(i, i);
    Point pt = null;
    if (r != null && cellBounds != null && cellBounds.contains(p)) {
      ListSelectionModel lsm = getSelectionModel();
      boolean hasFocus = hasFocus() && lsm.getLeadSelectionIndex() == i;
      E value = getModel().getElementAt(i);
      Component renderer = r.getListCellRendererComponent(
          this, value, i, lsm.isSelectedIndex(i), hasFocus);
      if (renderer instanceof JComponent && ((JComponent) renderer).getToolTipText() != null) {
        pt = cellBounds.getLocation();
      }
    }
    return pt;
  }
}

class CellRendererTooltipList<E> extends JList<E> {
  private final JLabel label = new JLabel();

  protected CellRendererTooltipList(ListModel<E> m) {
    super(m);
    // TEST: label.setBorder(BorderFactory.createLineBorder(Color.RED, 10));
    label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
  }

  // @Override public void updateUI() {
  //   super.updateUI();
  //   SwingUtilities.updateComponentTreeUI(label); // XXX: Nimbus?
  // }

  @Override public Point getToolTipLocation(MouseEvent e) {
    Point p = e.getPoint();
    int i = locationToIndex(p);
    ListCellRenderer<? super E> r = getCellRenderer();
    Rectangle cellBounds = getCellBounds(i, i);
    Point pt = null;
    if (r != null && cellBounds != null && cellBounds.contains(p)) {
      ListSelectionModel lsm = getSelectionModel();
      E str = getModel().getElementAt(i);
      boolean hasFocus = hasFocus() && lsm.getLeadSelectionIndex() == i;
      Component renderer = r.getListCellRendererComponent(
          this, str, i, lsm.isSelectedIndex(i), hasFocus);
      if (renderer instanceof JComponent && ((JComponent) renderer).getToolTipText() != null) {
        pt = cellBounds.getLocation();
        Insets ins = label.getInsets();
        pt.translate(-ins.left, -ins.top);
        label.setIcon(new RendererIcon(renderer, cellBounds));
      }
    }
    return pt;
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
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    if (c instanceof JComponent) {
      FontMetrics fm = c.getFontMetrics(c.getFont());
      String str = Objects.toString(value, "");
      int w = SwingUtilities.getAncestorOfClass(JViewport.class, list).getBounds().width;
      ((JComponent) c).setToolTipText(fm.stringWidth(str) > w ? str : list.getToolTipText());
    }
    return c;
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
