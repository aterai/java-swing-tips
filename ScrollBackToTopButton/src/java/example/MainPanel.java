// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

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

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();
    textArea.setText(String.join("\n", Collections.nCopies(2000, "1111111111111")));

    JScrollPane scroll1 = new JScrollPane(textArea);
    scroll1.setRowHeaderView(new LineNumberView(textArea));
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    textArea.setEditable(false);

    JTable table = new JTable(500, 3);
    JScrollPane scroll2 = new JScrollPane(table);
    SwingUtilities.invokeLater(() -> table.scrollRectToVisible(table.getCellRect(500, 0, true)));

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("JTextArea", new JLayer<>(scroll1, new ScrollBackToTopLayerUI()));
    tabbedPane.addTab("JTable", new JLayer<>(scroll2, new ScrollBackToTopLayerUI()));
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
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

class ScrollBackToTopLayerUI extends LayerUI<JScrollPane> {
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

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
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

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l.getView());
    mousePt.setLocation(p);
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

class LineNumberView extends JComponent {
  private static final int MARGIN = 5;
  private final JTextArea textArea;
  private final FontMetrics fontMetrics;
  private final int fontAscent;
  private final int fontHeight;
  private final int fontDescent;
  private final int fontLeading;

  protected LineNumberView(JTextArea textArea) {
    super();
    this.textArea = textArea;
    Font font = textArea.getFont();
    fontMetrics = getFontMetrics(font);
    fontHeight = fontMetrics.getHeight();
    fontAscent = fontMetrics.getAscent();
    fontDescent = fontMetrics.getDescent();
    fontLeading = fontMetrics.getLeading();

    textArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        repaint();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        repaint();
      }

      @Override public void changedUpdate(DocumentEvent e) {
        /* not needed */
      }
    });
    textArea.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        revalidate();
        repaint();
      }
    });
    Insets i = textArea.getInsets();
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
        BorderFactory.createEmptyBorder(i.top, MARGIN, i.bottom, MARGIN - 1)));
    setOpaque(true);
    setBackground(Color.WHITE);
    setFont(font);
  }

  private int getComponentWidth() {
    int lineCount = textArea.getLineCount();
    int maxDigits = Math.max(3, Objects.toString(lineCount).length());
    Insets i = getInsets();
    return maxDigits * fontMetrics.stringWidth("0") + i.left + i.right;
  }

  private int getLineAtPoint(int y) {
    Element root = textArea.getDocument().getDefaultRootElement();
    int pos = textArea.viewToModel(new Point(0, y));
    // Java 9: int pos = textArea.viewToModel2D(new Point(0, y));
    return root.getElementIndex(pos);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(getComponentWidth(), textArea.getHeight());
  }

  @Override protected void paintComponent(Graphics g) {
    g.setColor(getBackground());
    Rectangle clip = g.getClipBounds();
    g.fillRect(clip.x, clip.y, clip.width, clip.height);

    g.setColor(getForeground());
    int base = clip.y;
    int start = getLineAtPoint(base);
    int end = getLineAtPoint(base + clip.height);
    int y = start * fontHeight;
    int rmg = getInsets().right;
    for (int i = start; i <= end; i++) {
      String text = Objects.toString(i + 1);
      int x = getComponentWidth() - rmg - fontMetrics.stringWidth(text);
      y += fontAscent;
      g.drawString(text, x, y);
      y += fontDescent + fontLeading;
    }
  }
}
