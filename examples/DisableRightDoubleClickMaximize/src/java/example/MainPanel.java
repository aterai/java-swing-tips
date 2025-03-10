// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsInternalFrameUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop0 = makeDesktopPane();
    desktop0.add(createFrame("Default", 0));

    JInternalFrame f = createFrame("JPopupMenu", 0);
    JPopupMenu popup = new JPopupMenu();
    popup.add("test1");
    popup.add("test2");
    ((BasicInternalFrameUI) f.getUI()).getNorthPane().setComponentPopupMenu(popup);
    JDesktopPane desktop1 = makeDesktopPane();
    desktop1.add(f);

    JDesktopPane desktop2 = makeDesktopPane();
    desktop2.add(createFrame2());

    JDesktopPane desktop3 = makeDesktopPane();
    desktop3.add(createFrame("JDesktopPane", 1));
    desktop3.add(createFrame("JLayer", 0));

    JTabbedPane tabs = new JTabbedPane();
    tabs.add("Default", desktop0);
    tabs.add("JPopupMenu", desktop1);
    tabs.add("WindowsInternalFrameUI", desktop2);
    tabs.add("JLayer", new JLayer<>(desktop3, new DesktopLayerUI()));
    // tabs.setComponentPopupMenu(new TabbedPanePopupMenu());
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JDesktopPane makeDesktopPane() {
    JDesktopPane desktop = new JDesktopPane();
    desktop.setBackground(Color.LIGHT_GRAY);
    return desktop;
  }

  private static JInternalFrame createFrame(String t, int i) {
    JInternalFrame f = new JInternalFrame(t, true, true, true, true);
    f.setSize(200, 100);
    f.setLocation(5 + 40 * i, 5 + 50 * i);
    EventQueue.invokeLater(() -> f.setVisible(true));
    return f;
  }

  private static JInternalFrame createFrame2() {
    JInternalFrame f = new JInternalFrame("WindowsInternalFrameUI", true, true, true, true) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new WindowsInternalFrameUI(this) {
          @Override protected MouseInputAdapter createBorderListener(JInternalFrame w) {
            return new BorderListener() {
              @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                  super.mouseClicked(e);
                }
              }

              @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                  super.mousePressed(e);
                }
              }
            };
          }
        });
      }
    };
    f.setSize(200, 100);
    f.setLocation(5 + 40, 5 + 50);
    EventQueue.invokeLater(() -> f.setVisible(true));
    return f;
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

class DesktopLayerUI extends LayerUI<JDesktopPane> {
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

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JDesktopPane> l) {
    if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() >= 2) {
      Component c = e.getComponent();
      Container p = SwingUtilities.getAncestorOfClass(BasicInternalFrameTitlePane.class, c);
      boolean i1 = c instanceof BasicInternalFrameTitlePane;
      boolean i2 = p instanceof BasicInternalFrameTitlePane;
      boolean i3 = c instanceof JInternalFrame.JDesktopIcon;
      int id = e.getID();
      boolean b1 = id == MouseEvent.MOUSE_CLICKED && (i1 || i2);
      boolean b2 = id == MouseEvent.MOUSE_PRESSED && i3;
      if (b1 || b2) {
        e.consume();
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JDesktopPane> l) {
    boolean b = e.getComponent() instanceof JInternalFrame;
    boolean isRight = SwingUtilities.isRightMouseButton(e);
    if (b && isRight && e.getID() == MouseEvent.MOUSE_DRAGGED) {
      e.consume();
    }
  }
}
