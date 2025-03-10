// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;

public final class MainPanel extends JPanel {
  public static final char SHARP = '#';

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea(makeText());
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setRowHeaderView(new LineNumberView(textArea));
    JButton button = new JButton("count commented lines: startsWith(\"#\")");
    button.addActionListener(e -> {
      String msg = "commented lines: " + getCount1(textArea);
      String title = "title";
      JOptionPane.showMessageDialog(scroll, msg, title, JOptionPane.INFORMATION_MESSAGE);
    });
    EventQueue.invokeLater(() -> getRootPane().setDefaultButton(button));
    add(button, BorderLayout.NORTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeText() {
    StringBuilder sb = new StringBuilder();
    String txt = "1111111111111111\n";
    String comment = "#comment\n";
    IntStream.range(0, 200).forEach(i -> {
      sb.append(txt);
      if (i % 16 == 0) {
        sb.append(comment);
      }
    });
    return sb.toString();
  }

  private static int getCount1(JTextArea textArea) {
    int count = 0;
    StringTokenizer st = new StringTokenizer(textArea.getText(), "\n");
    while (st.hasMoreTokens()) {
      // if (st.nextToken().startsWith(SHARP)) {
      // if (st.nextToken().charAt(0) == SHARP) {
      if (st.nextToken().codePointAt(0) == SHARP) {
        count++;
      }
    }
    return count;
  }

  // private static int getCount2(JTextArea textArea) {
  //   // String#split
  //   int count = 0;
  //   for (String line : textArea.getText().split("\\n")) {
  //     if (!line.isEmpty() && line.codePointAt(0) == SHARP) {
  //       count++;
  //     }
  //   }
  //   return count;
  // }

  // private static int getCount3(JTextArea textArea) {
  //   // LineNumberReader
  //   int count = 0;
  //   String txt = textArea.getText();
  //   try (LineNumberReader lnr = new LineNumberReader(new StringReader(txt))) {
  //     String line = null;
  //     while ((line = lnr.readLine()) != null) {
  //       if (!line.isEmpty() && line.codePointAt(0) == SHARP) {
  //         count++;
  //       }
  //     }
  //   } catch (java.io.IOException ex) {
  //     ex.printStackTrace();
  //   }
  //   return count;
  // }

  // private static int getCount4(JTextArea textArea) {
  //   // ElementCount
  //   int count = 0;
  //   Document doc = textArea.getDocument();
  //   Element root = doc.getDefaultRootElement();
  //   try {
  //     for (int i = 0; i < root.getElementCount(); i++) {
  //       Element elm = root.getElement(i);
  //       int len = elm.getEndOffset() - elm.getStartOffset();
  //       String line = doc.getText(elm.getStartOffset(), len);
  //       if (line.codePointAt(0) == SHARP) {
  //         count++;
  //       }
  //     }
  //   } catch (BadLocationException ex) {
  //     throw new RuntimeException(ex); // should never happen
  //   }
  //   return count;
  // }

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
