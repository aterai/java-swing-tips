// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.text.Element;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    HighlightCursorTextArea textArea = new HighlightCursorTextArea();
    textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    textArea.setText("MouseOver Painter Test\n\n**********************");
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.getViewport().setBackground(Color.WHITE);
    add(scroll);
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

class HighlightCursorTextArea extends JTextArea {
  private static final Color LINE_COLOR = new Color(0xFA_FA_DC);
  private int rollOverRowIndex = -1;
  private transient MouseInputListener rolloverHandler;

  protected HighlightCursorTextArea() {
    super();
  }

  // public HighlightCursorTextArea(Document doc) {
  //   super(doc);
  // }

  // public HighlightCursorTextArea(Document doc, String text, int rows, int columns) {
  //   super(doc, text, rows, columns);
  // }

  // public HighlightCursorTextArea(int rows, int columns) {
  //   super(rows, columns);
  // }

  // public HighlightCursorTextArea(String text) {
  //   super(text);
  // }

  // public HighlightCursorTextArea(String text, int rows, int columns) {
  //   super(text, rows, columns);
  // }

  @Override public void updateUI() {
    removeMouseMotionListener(rolloverHandler);
    removeMouseListener(rolloverHandler);
    super.updateUI();
    setOpaque(false);
    setBackground(new Color(0x0, true)); // Nimbus
    rolloverHandler = new RollOverListener();
    addMouseMotionListener(rolloverHandler);
    addMouseListener(rolloverHandler);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    r.height = g2.getFontMetrics().getHeight();
    r.y += rollOverRowIndex * r.height;
    g2.setPaint(LINE_COLOR);
    g2.fill(r);
    g2.dispose();
    super.paintComponent(g);
  }

  private final class RollOverListener extends MouseInputAdapter {
    @Override public void mouseExited(MouseEvent e) {
      rollOverRowIndex = -1;
      e.getComponent().repaint();
    }

    @Override public void mouseMoved(MouseEvent e) {
      int row = getLineAtPoint(e.getPoint());
      if (row != rollOverRowIndex) {
        rollOverRowIndex = row;
        e.getComponent().repaint();
      }
    }

    private int getLineAtPoint(Point pt) {
      Element root = getDocument().getDefaultRootElement();
      return root.getElementIndex(viewToModel(pt));
      // Java 9: return root.getElementIndex(viewToModel2D(pt));
    }
  }
}
