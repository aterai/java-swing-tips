// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));
    DefaultListModel<String> model = createModel();
    JScrollPane scroll1 = new JScrollPane(createList(model));
    scroll1.addMouseWheelListener(e -> {
      JScrollPane scrollPane = (JScrollPane) e.getComponent();
      Component view = scrollPane.getViewport().getView();
      MouseEvent event = SwingUtilities.convertMouseEvent(scrollPane, e, view);
      ToolTipManager.sharedInstance().mouseMoved(event);
    });
    // Tooltips, the mouse wheel and JScrollPane oracle-tech
    // https://community.oracle.com/tech/developers/discussion/1353509/tooltips-the-mouse-wheel-and-jscrollpane

    JList<String> list2 = new ToolTipList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new ToolTipListCellRenderer<>());
      }
    };

    add(createTitledPanel("Default", new JScrollPane(createList(model))));
    add(createTitledPanel("MouseWheelListener", scroll1));
    add(createTitledPanel("getToolTipLocation", new JScrollPane(list2)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JList<String> createList(ListModel<String> model) {
    return new JList<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new ToolTipListCellRenderer<>());
      }
    };
  }

  private static DefaultListModel<String> createModel() {
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
    return model;
  }

  private static Component createTitledPanel(String title, Component c) {
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

class ToolTipList<E> extends JList<E> {
  protected ToolTipList(ListModel<E> m) {
    super(m);
  }

  @Override public String getToolTipText(MouseEvent e) {
    MouseEvent event = getToolTipCellPoint(e)
        .map(p -> new MouseEvent(
            e.getComponent(), MouseEvent.MOUSE_MOVED, e.getWhen(),
            e.getModifiersEx(),
            p.x, p.y, e.getClickCount(), e.isPopupTrigger())
        ).orElse(e);
    return super.getToolTipText(event);
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    // Show the tooltip just below the cell under the current mouse cursor,
    // or return null to use the default location
    return getToolTipCellPoint(e)
        .map(p -> {
          int i = locationToIndex(p);
          return new Point(p.x, p.y + getCellBounds(i, i).height);
        })
        .orElse(null);
  }

  // The location of MouseEvent may be stale after scrolling by the mouse wheel,
  // so use the current mouse position if it is over a cell
  private Optional<Point> getToolTipCellPoint(MouseEvent e) {
    return Optional.ofNullable(getMousePosition())
        .filter(p -> !p.equals(e.getPoint()))
        .filter(p -> {
          int i = locationToIndex(p);
          Rectangle cellBounds = getCellBounds(i, i);
          return cellBounds != null && cellBounds.contains(p);
        });
  }
}

class ToolTipListCellRenderer<E> implements ListCellRenderer<E> {
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
