// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new JTextArea()));
    add(new StatusBar(), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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

class StatusBar extends JPanel {
  protected StatusBar() {
    super(new BorderLayout());
    add(new BottomRightCornerLabel(), BorderLayout.EAST);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  // @Override public void updateUI() {
  //   super.updateUI();
  //   setOpaque(false);
  // }

  // @Override public boolean isOpaque() {
  //   return false;
  // }
}

class BottomRightCornerLabel extends JLabel {
  private transient MouseInputListener handler;

  protected BottomRightCornerLabel() {
    super(new BottomRightCornerIcon());
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    super.updateUI();
    handler = new ResizeWindowListener();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
  }
}

class ResizeWindowListener extends MouseInputAdapter {
  private final Rectangle rect = new Rectangle();
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    Component p = SwingUtilities.getRoot(e.getComponent());
    if (p instanceof Window) {
      startPt.setLocation(e.getPoint());
      rect.setBounds(p.getBounds());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component p = SwingUtilities.getRoot(e.getComponent());
    if (!rect.isEmpty() && p instanceof Window) {
      Point pt = e.getPoint();
      rect.width += pt.x - startPt.x;
      rect.height += pt.y - startPt.y;
      p.setBounds(rect);
    }
  }
}

// https://web.archive.org/web/20050609021916/http://today.java.net/pub/a/today/2005/06/07/pixelpushing.html
class BottomRightCornerIcon implements Icon {
  private static final Color SQUARE_COLOR = new Color(160, 160, 160, 160);

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    int diff = 3;
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(getIconWidth() - diff * 3 - 1, getIconHeight() - diff * 3 - 1);

    int firstRow = 0;
    int secondRow = firstRow + diff;
    int thirdRow = secondRow + diff;

    int firstColumn = 0;
    drawSquare(g2, firstColumn, thirdRow);

    int secondColumn = firstColumn + diff;
    drawSquare(g2, secondColumn, secondRow);
    drawSquare(g2, secondColumn, thirdRow);

    int thirdColumn = secondColumn + diff;
    drawSquare(g2, thirdColumn, firstRow);
    drawSquare(g2, thirdColumn, secondRow);
    drawSquare(g2, thirdColumn, thirdRow);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 20;
  }

  private void drawSquare(Graphics g, int x, int y) {
    g.setColor(SQUARE_COLOR);
    g.fillRect(x, y, 2, 2);
  }
}
