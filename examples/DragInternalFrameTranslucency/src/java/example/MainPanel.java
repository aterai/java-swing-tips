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
    JInternalFrame frame1 = createFrame(desktop, "Frame1");
    desktop.add(frame1);
    frame1.setLocation(30, 10);
    frame1.setVisible(true);
    JInternalFrame frame2 = createFrame(desktop, "Frame2");
    desktop.add(frame2);
    frame2.setLocation(50, 30);
    frame2.setVisible(true);
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private JInternalFrame createFrame(JDesktopPane desktop, String title) {
    JInternalFrame frame = new JInternalFrame(title, true, true, true, true) {
      @Override protected void paintComponent(Graphics g) {
        // if (isDragging) { // JInternalFrame#isDragging: package private
        DesktopManager m = desktop.getDesktopManager();
        if (m instanceof DragDesktopManager && ((DragDesktopManager) m).isDraggingFrame(this)) {
          ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
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

  public DragDesktopManager(JDesktopPane desktop) {
    super();
    this.desktop = desktop;
  }

  public void setDraggingFrame(JComponent f) {
    draggingFrame = f;
  }

  public boolean isDraggingFrame(JInternalFrame f) {
    return Objects.equals(draggingFrame, f);
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
  }
}
