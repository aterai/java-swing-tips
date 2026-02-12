// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenuBar menuBar = new JMenuBar();
    String[] titles = {"File", "Edit", "Code", "Analyze", "Refactor", "Help"};
    for (String title : titles) {
      menuBar.add(makeMenu(title));
    }
    JLayer<JMenuBar> menuLayer = new JLayer<>(menuBar, new MenuDragLayerUI());
    add(menuLayer, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenu makeMenu(String title) {
    JMenu menu = new JMenu(title);
    menu.add("MenuItem 1");
    menu.add("MenuItem 2");
    menu.add("MenuItem 3");
    return menu;
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

class MenuDragLayerUI extends LayerUI<JMenuBar> {
  private static final int DRAG_THRESHOLD = 8;
  private JMenu draggingMenu;
  private JWindow ghostWindow;
  private JLabel ghostLabel;
  private Point startPt;
  private boolean isDragging;
  private int targetIndex = -1;
  private int dividerX = -1;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
    ghostWindow = new JWindow();
    ghostWindow.setOpacity(.7f);
    ghostLabel = new JLabel();
    ghostLabel.setOpaque(false);
    ghostLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.GRAY),
        BorderFactory.createEmptyBorder(2, 5, 2, 5)
    ));
    ghostLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    ghostWindow.add(ghostLabel);
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JMenuBar> l) {
    JMenuBar bar = l.getView();
    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), bar);
    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
      Component c = bar.getComponentAt(p);
      if (c instanceof JMenu) {
        draggingMenu = (JMenu) c;
        startPt = p;
      }
    } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
      if (isDragging && draggingMenu != null) {
        finalizeDrop(bar);
      }
      resetDragState(l);
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JMenuBar> l) {
    if (draggingMenu != null) {
      JMenuBar bar = l.getView();
      Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), bar);
      if (!isDragging && startPt != null && startPt.distance(p) > DRAG_THRESHOLD) {
        initiateDrag(e);
      }
      if (isDragging) {
        updateDragFeedback(e, bar, p);
        l.repaint();
        e.consume();
      }
    }
  }

  private void initiateDrag(MouseEvent e) {
    isDragging = true;
    MenuSelectionManager.defaultManager().clearSelectedPath();
    draggingMenu.setEnabled(false);
    ghostLabel.setText(draggingMenu.getText());
    ghostLabel.setFont(draggingMenu.getFont());
    ghostWindow.pack();
    updateGhostLocation(e);
    ghostWindow.setVisible(true);
  }

  private void updateDragFeedback(MouseEvent e, JMenuBar bar, Point p) {
    updateGhostLocation(e);
    Component[] menus = bar.getComponents();
    targetIndex = 0;
    dividerX = menus.length > 0 ? menus[0].getX() : 0;
    for (int i = 0; i < menus.length; i++) {
      Component m = menus[i];
      if (Objects.equals(m, draggingMenu)) {
        continue;
      }
      int midX = m.getX() + m.getWidth() / 2;
      if (p.x < midX) {
        targetIndex = i;
        dividerX = m.getX();
        break;
      } else {
        targetIndex = i + 1;
        dividerX = m.getX() + m.getWidth();
      }
    }
  }

  private void finalizeDrop(JMenuBar bar) {
    int currentIdx = -1;
    for (int i = 0; i < bar.getComponentCount(); i++) {
      if (Objects.equals(bar.getComponent(i), draggingMenu)) {
        currentIdx = i;
        break;
      }
    }

    int finalIdx = targetIndex;
    if (currentIdx != -1 && currentIdx < targetIndex) {
      finalIdx--;
    }

    bar.add(draggingMenu, Math.max(0, finalIdx));
    draggingMenu.setEnabled(true);
  }

  @SuppressWarnings("PMD.NullAssignment")
  private void resetDragState(JLayer<? extends JMenuBar> l) {
    if (draggingMenu != null) {
      draggingMenu.setEnabled(true);
    }
    ghostWindow.setVisible(false);
    draggingMenu = null;
    isDragging = false;
    targetIndex = -1;
    dividerX = -1;
    l.getView().revalidate();
    l.repaint();
  }

  private void updateGhostLocation(MouseEvent e) {
    Point screenPt = e.getLocationOnScreen();
    ghostWindow.setLocation(screenPt.x + 10, screenPt.y + 10);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (isDragging && dividerX != -1) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(UIManager.getColor("List.dropLineColor"));
      g2.setStroke(new BasicStroke(2f));
      g2.drawLine(dividerX, 0, dividerX, c.getHeight());
      g2.dispose();
    }
  }
}
