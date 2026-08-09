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
import java.util.logging.Logger;
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
    // textArea.setEditable(false);

    JTable table = new JTable(500, 3);
    JScrollPane scroll2 = new JScrollPane(table);
    SwingUtilities.invokeLater(() -> {
      int max = table.getRowCount() - 1;
      table.scrollRectToVisible(table.getCellRect(max, 0, true));
    });

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("JTextArea", new JLayer<>(scroll1, new ScrollBackToTopLayerUI<>()));
    tabbedPane.addTab("JTable", new JLayer<>(scroll2, new ScrollBackToTopLayerUI<>()));
    add(tabbedPane);
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
    float centerX = getIconWidth() / 2f;
    float centerY = getIconHeight() / 2f;
    float arrowHalfWidth = centerX / 3f;
    float arrowHalfHeight = centerY / 6f;
    g2.setStroke(new BasicStroke(centerX / 2f));
    // Chevron (up-arrow) shape
    Path2D arrow = new Path2D.Float();
    arrow.moveTo(centerX - arrowHalfWidth, centerY + arrowHalfHeight);
    arrow.lineTo(centerX, centerY - arrowHalfHeight);
    arrow.lineTo(centerX + arrowHalfWidth, centerY + arrowHalfHeight);
    g2.draw(arrow);
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
  // Dummy parent used only as the owner for SwingUtilities#paintComponent
  private final Container rubberStamp = new JPanel();
  private final Point mousePoint = new Point();
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

  @Override public void updateUI(JLayer<? extends V> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(button);
  }

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
        button.getModel().setRollover(buttonRect.contains(mousePoint));
        SwingUtilities.paintComponent(g, button, rubberStamp, buttonRect);
      }
    }
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> l = (JLayer<?>) c;
      l.getGlassPane().setCursor(Cursor.getDefaultCursor());
      l.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
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
    Rectangle viewRect = scroll.getViewport().getViewRect();
    Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), scroll);
    mousePoint.setLocation(pt);
    int eventId = e.getID();
    if (eventId == MouseEvent.MOUSE_CLICKED) {
      if (buttonRect.contains(mousePoint)) {
        scrollBackToTop(l.getView());
      }
    } else if (eventId == MouseEvent.MOUSE_PRESSED
        && viewRect.y > 0 && buttonRect.contains(mousePoint)) {
      e.consume();
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
    Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l.getView());
    mousePoint.setLocation(pt);
    l.getGlassPane().setVisible(buttonRect.contains(mousePoint));
    l.repaint(buttonRect);
  }

  private void scrollBackToTop(JScrollPane scroll) {
    JComponent view = (JComponent) scroll.getViewport().getView();
    Rectangle target = scroll.getViewport().getViewRect();
    new Timer(20, e -> {
      Timer animator = (Timer) e.getSource();
      if (0 < target.y && animator.isRunning()) {
        target.y -= Math.max(1, target.y / 2);
        view.scrollRectToVisible(target);
      } else {
        animator.stop();
      }
    }).start();
  }
}

class LineNumberView extends JPanel {
  private static final int MARGIN = 5;
  private final JTextArea textArea;

  protected LineNumberView(JTextArea textArea) {
    super();
    this.textArea = textArea;
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
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(true);
    EventQueue.invokeLater(() -> {
      Insets textAreaMargin = textArea.getMargin();
      setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
          BorderFactory.createEmptyBorder(
              textAreaMargin.top, MARGIN, textAreaMargin.bottom, MARGIN - 1)));
      setBackground(textArea.getBackground());
    });
  }

  private int getComponentWidth(FontMetrics fontMetrics) {
    int lineCount = textArea.getLineCount();
    int maxDigits = Math.max(3, Integer.toString(lineCount).length());
    Insets insets = getInsets();
    return maxDigits * fontMetrics.stringWidth("0") + insets.left + insets.right;
  }

  private int getLineAtPoint(int y) {
    Element root = textArea.getDocument().getDefaultRootElement();
    int pos = textArea.viewToModel(new Point(0, y));
    // Java 9: int pos = textArea.viewToModel2D(new Point(0, y));
    return root.getElementIndex(pos);
  }

  @Override public Dimension getPreferredSize() {
    FontMetrics fontMetrics = textArea.getFontMetrics(textArea.getFont());
    return new Dimension(getComponentWidth(fontMetrics), textArea.getHeight());
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setColor(textArea.getBackground());
    Rectangle clip = g2.getClipBounds();
    g2.fillRect(clip.x, clip.y, clip.width, clip.height);

    Font font = textArea.getFont();
    g2.setFont(font);
    g2.setColor(getForeground());
    int startLine = getLineAtPoint(clip.y);
    int endLine = getLineAtPoint(clip.y + clip.height);
    FontMetrics fontMetrics = g2.getFontMetrics(font);
    int fontAscent = fontMetrics.getAscent();
    int fontDescent = fontMetrics.getDescent();
    int fontLeading = fontMetrics.getLeading();
    int y = startLine * fontMetrics.getHeight();
    int rightMargin = getInsets().right;
    for (int line = startLine; line <= endLine; line++) {
      String text = Integer.toString(line + 1);
      int x = getComponentWidth(fontMetrics) - rightMargin - fontMetrics.stringWidth(text);
      y += fontAscent;
      g2.drawString(text, x, y);
      y += fontDescent + fontLeading;
    }
    g2.dispose();
  }
}
