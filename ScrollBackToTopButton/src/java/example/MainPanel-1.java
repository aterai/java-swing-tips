// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

// package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.Collections;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.LayerUI;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

public final class MainPanel {
  private Component makeUI() {
    JTextArea textArea = new JTextArea();
    textArea.setText(String.join("\n\n", Collections.nCopies(2000, "1234567890")));
    return new JLayer<>(new JScrollPane(textArea), new ScrollBackToTopLayerUI<>());
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(() -> {
      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.getContentPane().add(new MainPanel().makeUI());
      frame.setSize(320, 240);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}

class ScrollBackToTopIcon implements Icon {
  private final Color rolloverColor = new Color(0xAA_FF_AF_64, true);
  private final Color arrowColor = new Color(0xAA_64_64_64, true);

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover()) {
      g2.setPaint(rolloverColor);
    } else {
      g2.setPaint(arrowColor);
    }
    float w2 = getIconWidth() / 2f;
    float h2 = getIconHeight() / 2f;
    float tw = w2 / 3f;
    float th = h2 / 6f;
    g2.setStroke(new BasicStroke(w2 / 2f));
    Path2D p = new Path2D.Float();
    p.moveTo(w2 - tw, h2 + th);
    p.lineTo(w2, h2 - th);
    p.lineTo(w2 + tw, h2 + th);
    g2.draw(p);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}

class ScrollBackToTopLayerUI<V extends JScrollPane> extends LayerUI<V> {
  private static final int GAP = 5;
  private final Container rubberStamp = new JPanel();
  private final Point mousePt = new Point();
  private final JButton button = new JButton(new ScrollBackToTopIcon()) {
    @Override public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder());
      setFocusPainted(false);
      setBorderPainted(false);
      setContentAreaFilled(false);
      setRolloverEnabled(false);
    }
  };
  private final Rectangle buttonRect = new Rectangle(button.getPreferredSize());

  private void updateButtonRect(JScrollPane scroll) {
    JViewport viewport = scroll.getViewport();
    int x = viewport.getX() + viewport.getWidth() - buttonRect.width - GAP;
    int y = viewport.getY() + viewport.getHeight() - buttonRect.height - GAP;
    buttonRect.setLocation(x, y);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      updateButtonRect(scroll);
      if (scroll.getViewport().getViewRect().y > 0) {
        button.getModel().setRollover(buttonRect.contains(mousePt));
        SwingUtilities.paintComponent(g, button, rubberStamp, buttonRect);
      }
    }
  }

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

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {
    JScrollPane scroll = l.getView();
    Rectangle r = scroll.getViewport().getViewRect();
    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), scroll);
    mousePt.setLocation(p);
    int id = e.getID();
    if (id == MouseEvent.MOUSE_CLICKED) {
      if (buttonRect.contains(mousePt)) {
        scrollBackToTop(l.getView());
      }
    } else if (id == MouseEvent.MOUSE_PRESSED && r.y > 0 && buttonRect.contains(mousePt)) {
      e.consume();
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l.getView());
    mousePt.setLocation(p);
    Component c = e.getComponent();
    if (!buttonRect.contains(mousePt) && c instanceof JTextComponent) {
      c.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    } else {
      c.setCursor(Cursor.getDefaultCursor());
    }
    l.repaint(buttonRect);
  }

  private void scrollBackToTop(JScrollPane scroll) {
    JComponent c = (JComponent) scroll.getViewport().getView();
    Rectangle current = scroll.getViewport().getViewRect();
    new Timer(20, e -> {
      Timer animator = (Timer) e.getSource();
      if (0 < current.y && animator.isRunning()) {
        current.y -= Math.max(1, current.y / 2);
        c.scrollRectToVisible(current);
      } else {
        animator.stop();
      }
    }).start();
  }
}
