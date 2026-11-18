// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    desktop.setDesktopManager(new DragDesktopManager(desktop));
    JInternalFrame frame1 = createFrame("Frame1");
    desktop.add(frame1);
    frame1.setLocation(30, 10);
    frame1.setVisible(true);
    JInternalFrame frame2 = createFrame("Frame2");
    desktop.add(frame2);
    frame2.setLocation(50, 30);
    frame2.setVisible(true);
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JInternalFrame createFrame(String title) {
    JInternalFrame frame = new JInternalFrame(title, true, true, true, true) {
      @Override protected void paintComponent(Graphics g) {
        // if (isDragging) { // JInternalFrame#isDragging: package private
        JDesktopPane desktop = getDesktopPane();
        DesktopManager m = desktop == null ? null : desktop.getDesktopManager();
        if (m instanceof DragDesktopManager && ((DragDesktopManager) m).isDraggingFrame(this)) {
          // The same Graphics is also used to paint the border and children,
          // so the whole frame is drawn semi-transparent
          AlphaComposite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f);
          ((Graphics2D) g).setComposite(comp);
        }
        super.paintComponent(g);
      }
    };
    frame.setSize(200, 100);
    return frame;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

class DragDesktopManager extends DefaultDesktopManager {
  private final JDesktopPane desktop;
  private JComponent draggingFrame;

  protected DragDesktopManager(JDesktopPane desktop) {
    super();
    this.desktop = desktop;
  }

  private void setDraggingFrame(JComponent f) {
    draggingFrame = f;
  }

  public boolean isDraggingFrame(JComponent f) {
    return f != null && Objects.equals(draggingFrame, f);
  }

  @Override public void beginDraggingFrame(JComponent f) {
    setDraggingFrame(f);
    super.beginDraggingFrame(f);
  }

  @Override public void endDraggingFrame(JComponent f) {
    setDraggingFrame(null);
    super.endDraggingFrame(f);
    f.repaint();
  }

  @Override public void beginResizingFrame(JComponent f, int direction) {
    setDraggingFrame(f);
    desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    super.beginResizingFrame(f, direction);
  }

  @Override public void endResizingFrame(JComponent f) {
    setDraggingFrame(null);
    desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
    super.endResizingFrame(f);
    // The bounds may not change (e.g. just clicking the border), so repaint
    // explicitly to clear the semi-transparent state
    f.repaint();
  }
}
