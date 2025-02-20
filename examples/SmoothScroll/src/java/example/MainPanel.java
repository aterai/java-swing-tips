// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

public final class MainPanel extends JPanel {
  private final SpinnerNumberModel model = new SpinnerNumberModel(100, 1, 2000, 1);
  private final JTextArea textArea = new JTextArea();
  private final JScrollPane scroll = new JScrollPane(textArea);

  private MainPanel() {
    super(new BorderLayout());
    textArea.setText(String.join("\n", Collections.nCopies(2000, "1111111111111")));

    scroll.setRowHeaderView(new LineNumberView(textArea));
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    textArea.setEditable(false);

    JButton button = new JButton("Goto Line");
    button.addActionListener(e -> startScroll());
    EventQueue.invokeLater(() -> getRootPane().setDefaultButton(button));

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JSpinner(model));
    panel.add(button, BorderLayout.EAST);

    add(panel, BorderLayout.NORTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private void startScroll() {
    Document doc = textArea.getDocument();
    Element root = doc.getDefaultRootElement();
    // int ln = Math.max(1, Math.min(root.getElementCount(), model.getNumber().intValue()));
    int ln = model.getNumber().intValue();
    try {
      int offset = root.getElement(ln - 1).getStartOffset();
      Rectangle dst = textArea.modelToView(offset);
      // Java 9: Rectangle dst = textArea.modelToView2D(elem.getStartOffset()).getBounds();
      Rectangle cur = scroll.getViewport().getViewRect();
      new Timer(20, e -> {
        Object src = e.getSource();
        if (src instanceof Timer) {
          smoothScroll((Timer) src, dst, cur, offset);
        }
      }).start();
    } catch (BadLocationException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(textArea);
    }
  }

  private void smoothScroll(Timer animator, Rectangle dst, Rectangle cur, int offset) {
    if (dst.y < cur.y && animator.isRunning()) {
      int d = Math.max(1, (cur.y - dst.y) / 2);
      cur.y = cur.y - d;
      textArea.scrollRectToVisible(cur);
    } else if (dst.y > cur.y && animator.isRunning()) {
      int d = Math.max(1, (dst.y - cur.y) / 2);
      cur.y = cur.y + d;
      textArea.scrollRectToVisible(cur);
    } else {
      textArea.setCaretPosition(offset);
      animator.stop();
    }
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
      // Insets i = textArea.getInsets();
      Insets i = textArea.getMargin();
      setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
          BorderFactory.createEmptyBorder(i.top, MARGIN, i.bottom, MARGIN - 1)));
      setBackground(textArea.getBackground());
    });
  }

  private int getComponentWidth(FontMetrics fontMetrics) {
    int lineCount = textArea.getLineCount();
    int maxDigits = Math.max(3, Integer.toString(lineCount).length());
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
    FontMetrics fontMetrics = g2.getFontMetrics(font);
    int fontAscent = fontMetrics.getAscent();
    int fontDescent = fontMetrics.getDescent();
    int fontLeading = fontMetrics.getLeading();

    g2.setColor(getForeground());
    int base = clip.y;
    int start = getLineAtPoint(base);
    int end = getLineAtPoint(base + clip.height);
    int y = start * fontMetrics.getHeight();
    int rmg = getInsets().right;
    for (int i = start; i <= end; i++) {
      String text = Integer.toString(i + 1);
      int x = getComponentWidth(fontMetrics) - rmg - fontMetrics.stringWidth(text);
      y += fontAscent;
      g2.drawString(text, x, y);
      y += fontDescent + fontLeading;
    }
    g2.dispose();
  }
}
