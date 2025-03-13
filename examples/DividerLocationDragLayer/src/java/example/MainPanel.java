// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, makeBox(), makeBox());
    split.setDividerSize(1);
    split.setResizeWeight(.5);

    JCheckBox check = new JCheckBox("VERTICAL");
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      split.setOrientation(b ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT);
    });

    add(check, BorderLayout.NORTH);
    add(new JLayer<>(split, new DividerLocationDragLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeBox() {
    JTree tree = new JTree();
    tree.setVisibleRowCount(3);

    Box box = Box.createVerticalBox();
    box.add(new JLabel("1111111111111111111111"));
    box.add(Box.createVerticalStrut(5));
    box.add(new JCheckBox("22222222222"));
    box.add(Box.createVerticalStrut(5));
    box.add(new JScrollPane(tree));
    box.add(Box.createVerticalStrut(5));
    box.add(new JButton("333333"));
    box.add(Box.createVerticalGlue());
    box.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

    JScrollPane sp = new JScrollPane(box);
    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setViewportBorder(BorderFactory.createEmptyBorder());
    sp.setBorder(BorderFactory.createEmptyBorder());
    return sp;
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

class DividerLocationDragLayerUI extends LayerUI<JSplitPane> {
  // @see https://stackoverflow.com/questions/37462651/jsplitpane-small-border-but-big-grab-hitbox
  private int dividerLocation;
  private final Point startPt = new Point();

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

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JSplitPane> l) {
    JSplitPane splitPane = l.getView();
    Component c = e.getComponent();
    if (isDraggableComponent(splitPane, c) && e.getID() == MouseEvent.MOUSE_PRESSED) {
      startPt.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), splitPane));
      dividerLocation = splitPane.getDividerLocation();
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JSplitPane> l) {
    JSplitPane splitPane = l.getView();
    Component c = e.getComponent();
    if (isDraggableComponent(splitPane, c) && e.getID() == MouseEvent.MOUSE_DRAGGED) {
      Point pt = SwingUtilities.convertPoint(c, e.getPoint(), splitPane);
      boolean isHorizontal = splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;
      int delta = isHorizontal ? pt.x - startPt.x : pt.y - startPt.y;
      splitPane.setDividerLocation(Math.max(0, dividerLocation + delta));
    }
  }

  private static boolean isDraggableComponent(JSplitPane p, Component c) {
    return Objects.equals(p, c) || Objects.equals(p, SwingUtilities.getUnwrappedParent(c));
  }
}
