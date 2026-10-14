// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setRowHeaderView(new LineNumberView(textArea));
    textArea.setText("aaa aaa aaa\nbbb bbb bbb bbb bbb\n\n\n\n\nccc ccc ccc ccc");
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
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

// https://community.oracle.com/thread/1479759 Advice for editor gutter implementation...
// Original author: Alan Moore
// Modified by: TERAI Atsuhiro
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
    // The vertical position of each line number is taken from the JTextArea itself,
    // so only the horizontal padding is required here.
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
        BorderFactory.createEmptyBorder(0, MARGIN, 0, MARGIN - 1)));
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
    Rectangle clip = g2.getClipBounds();
    g2.setColor(textArea.getBackground());
    g2.fill(clip);

    Font font = textArea.getFont();
    g2.setFont(font);
    g2.setColor(getForeground());
    FontMetrics fontMetrics = g2.getFontMetrics(font);
    int ascent = fontMetrics.getAscent();
    int right = getWidth() - getInsets().right;
    Element root = textArea.getDocument().getDefaultRootElement();
    int start = getLineAtPoint(clip.y);
    int end = getLineAtPoint(clip.y + clip.height);
    try {
      for (int i = start; i <= end; i++) {
        // Align the baseline with the first row of the line in the JTextArea,
        // this takes the JTextArea insets and wrapped lines into account.
        int offset = root.getElement(i).getStartOffset();
        Rectangle r = textArea.modelToView(offset);
        // Java 9: Rectangle r = textArea.modelToView2D(offset).getBounds();
        String text = Integer.toString(i + 1);
        int x = right - fontMetrics.stringWidth(text);
        g2.drawString(text, x, r.y + ascent);
      }
    } catch (BadLocationException ex) {
      // should never happen: the start offset of an existing line is always valid
      UIManager.getLookAndFeel().provideErrorFeedback(textArea);
    }
    g2.dispose();
  }
}
