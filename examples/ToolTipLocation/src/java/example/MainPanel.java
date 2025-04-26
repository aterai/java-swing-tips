// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public final class MainPanel extends JPanel {
  private transient MouseInputListener handler;

  private MainPanel() {
    super();
    add(new JLabel("mouseDragged: Show JToolTip"));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    removeMouseMotionListener(handler);
    removeMouseListener(handler);
    super.updateUI();
    handler = new ToolTipLocationHandler();
    addMouseMotionListener(handler);
    addMouseListener(handler);
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

class ToolTipLocationHandler extends MouseInputAdapter {
  private final JWindow window = new JWindow();
  private final JToolTip tip = new JToolTip();
  private final PopupFactory factory = PopupFactory.getSharedInstance();
  private Popup popup;
  private String prev = "";

  private Point getToolTipLocation(MouseEvent e) {
    Point p = e.getPoint();
    Component c = e.getComponent();
    SwingUtilities.convertPointToScreen(p, c);
    p.translate(0, -tip.getPreferredSize().height);
    return p;
  }

  private void updateTipText(MouseEvent e) {
    Point pt = e.getPoint();
    String txt = String.format("Window(x, y)=(%d, %d)", pt.x, pt.y);
    tip.setTipText(txt);
    Point p = getToolTipLocation(e);
    if (SwingUtilities.isLeftMouseButton(e)) {
      if (prev.length() != txt.length()) {
        window.pack();
      }
      window.setLocation(p);
      window.setAlwaysOnTop(true);
    } else {
      if (Objects.nonNull(popup)) {
        popup.hide();
      }
      popup = factory.getPopup(e.getComponent(), tip, p.x, p.y);
      Container c = tip.getTopLevelAncestor();
      if (c instanceof JWindow && ((JWindow) c).getType() != Window.Type.POPUP) {
        // Popup$LightWeightWindow
        popup.show();
      }
    }
    prev = txt;
  }

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      // window.getContentPane().removeAll();
      window.add(tip);
      updateTipText(e);
      window.setVisible(true);
    } else {
      updateTipText(e);
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    updateTipText(e);
  }

  @Override public void mouseReleased(MouseEvent e) {
    if (Objects.nonNull(popup)) {
      popup.hide();
    }
    window.setVisible(false);
  }
}
