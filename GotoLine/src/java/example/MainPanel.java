// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

public final class MainPanel extends JPanel {
  private static final int MIN = 1;
  private static final int MAX = 2000;

  private MainPanel() {
    super(new BorderLayout());
    SpinnerNumberModel model = new SpinnerNumberModel(100, MIN, MAX, 1);
    String text = String.join("\n", Collections.nCopies(MAX, "1234567890"));
    JTextArea textArea = new JTextArea(text);
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setRowHeaderView(new LineNumberView(textArea));

    JButton button = new JButton("Goto Line");
    button.addActionListener(e -> {
      Document doc = textArea.getDocument();
      Element root = doc.getDefaultRootElement();
      // int i = Math.max(MIN, Math.min(root.getElementCount(), model.getNumber().intValue()));
      int i = model.getNumber().intValue();
      try {
        Element elem = root.getElement(i - 1);
        Rectangle rect = textArea.modelToView(elem.getStartOffset());
        // Java 9: Rectangle rect = textArea.modelToView2D(elem.getStartOffset()).getBounds();
        Rectangle vr = scroll.getViewport().getViewRect();
        rect.setSize(10, vr.height);
        textArea.scrollRectToVisible(rect);
        textArea.setCaretPosition(elem.getStartOffset());
        // textArea.requestFocus();
      } catch (BadLocationException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(textArea);
      }
    });
    // frame.getRootPane().setDefaultButton(button);
    EventQueue.invokeLater(() -> getRootPane().setDefaultButton(button));

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JSpinner(model));
    p.add(button, BorderLayout.EAST);
    add(p, BorderLayout.NORTH);
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
