// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));
    add(makeTitledPanel("Default", new JList<>(makeModel())));
    add(makeTitledPanel("MouseEvent", new SingleMouseClickSelectList<>(makeModel())));
    add(makeTitledPanel("SelectionInterval", new SingleClickSelectList<>(makeModel())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultListModel<String> makeModel() {
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("111111111");
    model.addElement("22222222222222");
    model.addElement("333333333");
    model.addElement("44444444");
    model.addElement("5555555555");
    return model;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(new JScrollPane(c));
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

class SingleMouseClickSelectList<E> extends JList<E> {
  protected SingleMouseClickSelectList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setForeground(null);
    setBackground(null);
    setSelectionForeground(null);
    setSelectionBackground(null);
    super.updateUI();
  }

  @Override protected void processMouseMotionEvent(MouseEvent e) {
    super.processMouseMotionEvent(convertMouseEvent(e));
  }

  @Override protected void processMouseEvent(MouseEvent e) {
    if (e.getID() == MouseEvent.MOUSE_ENTERED || e.getID() == MouseEvent.MOUSE_EXITED) {
      super.processMouseEvent(e);
    } else {
      if (getCellBounds(0, getModel().getSize() - 1).contains(e.getPoint())) {
        super.processMouseEvent(convertMouseEvent(e));
      } else {
        e.consume();
        requestFocusInWindow();
      }
    }
  }

  private MouseEvent convertMouseEvent(MouseEvent e) {
    // JList where mouse click acts like ctrl-mouse click
    // https://community.oracle.com/thread/1351452
    return new MouseEvent(
        e.getComponent(),
        e.getID(), e.getWhen(),
        // e.getModifiers() | InputEvent.CTRL_DOWN_MASK,
        // select multiple objects in OS X: Command + click
        // pointed out by nsby
        e.getModifiersEx() | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
        // Java 10: ... | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
        e.getX(), e.getY(),
        e.getXOnScreen(), e.getYOnScreen(),
        e.getClickCount(),
        e.isPopupTrigger(),
        e.getButton());
  }
}

class SingleClickSelectList<E> extends JList<E> {
  protected boolean isDragging;
  protected boolean isInsideDragging;
  protected boolean startOutside;
  protected int startIndex = -1;
  private transient SelectionHandler listener;

  protected SingleClickSelectList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    setForeground(null);
    setBackground(null);
    setSelectionForeground(null);
    setSelectionBackground(null);
    super.updateUI();
    listener = new SelectionHandler();
    addMouseListener(listener);
    addMouseMotionListener(listener);
  }

  @Override public void setSelectionInterval(int anchor, int lead) {
    if (anchor == lead && lead >= 0) {
      if (isDragging) {
        addSelectionInterval(anchor, anchor);
      } else if (!isInsideDragging) {
        if (isSelectedIndex(anchor)) {
          removeSelectionInterval(anchor, anchor);
        } else {
          addSelectionInterval(anchor, anchor);
        }
        isInsideDragging = true;
      }
    } else {
      super.setSelectionInterval(anchor, lead);
    }
  }

  protected void clearSelectionAndFocus() {
    getSelectionModel().clearSelection();
    getSelectionModel().setAnchorSelectionIndex(-1);
    getSelectionModel().setLeadSelectionIndex(-1);
  }

  protected boolean cellsContains(Point pt) {
    // for (int i = 0; i < getModel().getSize(); i++) {
    //   Rectangle r = getCellBounds(i, i);
    //   if (r != null && r.contains(pt)) {
    //     return true;
    //   }
    // }
    // return false;
    return IntStream.range(0, getModel().getSize())
        .mapToObj(i -> getCellBounds(i, i))
        .anyMatch(r -> r != null && r.contains(pt));
  }

  protected class SelectionHandler extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      // JList<?> list = (JList<?>) e.getComponent();
      startOutside = !cellsContains(e.getPoint());
      startIndex = locationToIndex(e.getPoint());
      if (startOutside) {
        clearSelectionAndFocus();
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      startOutside = false;
      isDragging = false;
      isInsideDragging = false;
      startIndex = -1;
    }

    @Override public void mouseDragged(MouseEvent e) {
      // JList<?> list = (JList<?>) e.getComponent();
      if (!isDragging && startIndex == locationToIndex(e.getPoint())) {
        isInsideDragging = true;
      } else {
        isDragging = true;
        isInsideDragging = false;
      }
      if (cellsContains(e.getPoint())) {
        startOutside = false;
        isDragging = true;
      } else if (startOutside) {
        clearSelectionAndFocus();
      }
    }
  }
}
