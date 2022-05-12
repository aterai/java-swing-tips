// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));
    DefaultListModel<String> model = new DefaultListModel<>();
    for (int i = 0; i < 8; i++) {
      model.addElement(i + ": 11111");
      model.addElement(i + ": 22222");
      model.addElement(i + ": 33333");
      model.addElement(i + ": 44444");
      model.addElement(i + ": 55555");
      model.addElement(i + ": 66666");
      model.addElement(i + ": 77777");
      model.addElement(i + ": 88888");
      model.addElement(i + ": 99999");
      model.addElement(i + ": 00000");
    }

    JList<String> list0 = new JList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TooltipListCellRenderer<>());
      }
    };

    JList<String> list1 = new JList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TooltipListCellRenderer<>());
      }
    };
    JScrollPane scroll1 = new JScrollPane(list1);
    scroll1.addMouseWheelListener(e -> {
      JScrollPane scrollPane = (JScrollPane) e.getComponent();
      Component view = scrollPane.getViewport().getView();
      MouseEvent event = SwingUtilities.convertMouseEvent(scrollPane, e, view);
      ToolTipManager.sharedInstance().mouseMoved(event);
      // Tooltips, the mouse wheel and JScrollPane oracle-tech
      // https://community.oracle.com/tech/developers/discussion/1353509/tooltips-the-mouse-wheel-and-jscrollpane
      // Point p = SwingUti.getMousePosition();
      // if (p != null) {
      //   MouseEvent event = new MouseEvent(
      //       e.getComponent(),
      //       MouseEvent.MOUSE_MOVED,
      //       e.getWhen(),
      //       e.getModifiersEx() | e.getModifiers(),
      //       p.x,
      //       p.y,
      //       e.getClickCount(),
      //       e.isPopupTrigger()
      //   );
      //   ToolTipManager.sharedInstance().mouseMoved(event);
      // }
    });

    JList<String> list2 = new TooltipList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TooltipListCellRenderer<>());
      }
    };

    add(makeTitledPanel("Default", new JScrollPane(list0)));
    add(makeTitledPanel("MouseWheelListener", scroll1));
    add(makeTitledPanel("getToolTipLocation", new JScrollPane(list2)));
    setPreferredSize(new Dimension(320, 240));
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

  @Override public String getToolTipText(MouseEvent e) {
    Point p0 = e.getPoint();
    Point p1 = getMousePosition();
    if (p1 != null && !p1.equals(p0)) {
      int i = locationToIndex(p1);
      Rectangle cellBounds = getCellBounds(i, i);
      if (cellBounds != null && cellBounds.contains(p1)) {
        MouseEvent event = new MouseEvent(
            e.getComponent(),
            MouseEvent.MOUSE_MOVED,
            e.getWhen(),
            // since Java 9, MouseEvent#getModifiers() has been deprecated
            e.getModifiersEx() | e.getModifiers(),
            p1.x,
            p1.y,
            e.getClickCount(),
            e.isPopupTrigger()
        );
        return super.getToolTipText(event);
      }
    }
    return super.getToolTipText(e);
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    Point p0 = e.getPoint();
    Point p1 = getMousePosition();
    if (p1 != null && !p1.equals(p0)) {
      int i = locationToIndex(p1);
      Rectangle cellBounds = getCellBounds(i, i);
      if (cellBounds != null && cellBounds.contains(p1)) {
        return new Point(p1.x, p1.y + cellBounds.height);
      }
    }
    return null;
  }
}

class TooltipListCellRenderer<E> implements ListCellRenderer<E> {
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    if (c instanceof JComponent && value != null) {
      ((JComponent) c).setToolTipText(value.toString());
    }
    return c;
  }
}
