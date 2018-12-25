// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

public final class MainPanel extends JPanel {

  private MainPanel() {
    super(new BorderLayout());

    JSpinner spinner = new JSpinner(new SpinnerNumberModel(100, 1, 2000, 1));
    JTextArea textArea = new JTextArea(String.join("\n", Collections.nCopies(2000, "aaaaaaaaaaaaa")));
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setRowHeaderView(new LineNumberView(textArea));

    JButton button = new JButton("Goto Line");
    button.addActionListener(e -> {
      Document doc = textArea.getDocument();
      Element root = doc.getDefaultRootElement();
      int i = Math.max(1, Math.min(root.getElementCount(), (Integer) spinner.getValue()));
      try {
        Element elem = root.getElement(i - 1);
        Rectangle rect = textArea.modelToView(elem.getStartOffset());
        Rectangle vr = scroll.getViewport().getViewRect();
        rect.setSize(10, vr.height);
        textArea.scrollRectToVisible(rect);
        textArea.setCaretPosition(elem.getStartOffset());
        // textArea.requestFocus();
      } catch (BadLocationException ex) {
        Toolkit.getDefaultToolkit().beep();
      }
    });
    // frame.getRootPane().setDefaultButton(button);
    EventQueue.invokeLater(() -> getRootPane().setDefaultButton(button));

    JPanel p = new JPanel(new BorderLayout());
    p.add(spinner);
    p.add(button, BorderLayout.EAST);
    add(p, BorderLayout.NORTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class LineNumberView extends JComponent {
  private static final int MARGIN = 5;
  private final JTextArea textArea;
  private final FontMetrics fontMetrics;
  // private final int topInset;
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
    // topInset = textArea.getInsets().top;

    textArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        repaint();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        repaint();
      }

      @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
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
    // Document doc = textArea.getDocument();
    // Element root = doc.getDefaultRootElement();
    // int lineCount = root.getElementIndex(doc.getLength());
    int lineCount = textArea.getLineCount();
    int maxDigits = Math.max(3, Objects.toString(lineCount).length());
    Insets i = getBorder().getBorderInsets(this);
    return maxDigits * fontMetrics.stringWidth("0") + i.left + i.right;
    // return 48;
  }

  private int getLineAtPoint(int y) {
    Element root = textArea.getDocument().getDefaultRootElement();
    int pos = textArea.viewToModel(new Point(0, y));
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
    int rmg = getBorder().getBorderInsets(this).right;
    for (int i = start; i <= end; i++) {
      String text = Objects.toString(i + 1);
      int x = getComponentWidth() - rmg - fontMetrics.stringWidth(text);
      y += fontAscent;
      g.drawString(text, x, y);
      y += fontDescent + fontLeading;
    }
  }
}
