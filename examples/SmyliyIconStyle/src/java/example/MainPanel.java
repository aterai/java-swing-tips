// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String faceMark = ":)"; // "\uD83D\uDE10";

    JTextPane textPane = new JTextPane();
    textPane.setEditorKit(new StyledEditorKit());

    StyledDocument doc = textPane.getStyledDocument();
    doc.addDocumentListener(new FaceMarkDocumentListener(faceMark));
    Style face = doc.addStyle(faceMark, doc.getStyle(StyleContext.DEFAULT_STYLE));
    StyleConstants.setIcon(face, new FaceIcon());
    // StyleConstants.setForeground(face, Color.RED);

    // textPane.setText("123 \uD83D\uDE42 456 :) 789 :-) 000\n");
    textPane.setText("123 🙂 456 :) 789 :-) 000\n");

    add(new JScrollPane(textPane));
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

class FaceMarkDocumentListener implements DocumentListener {
  private final String faceMark;

  protected FaceMarkDocumentListener(String faceMark) {
    this.faceMark = faceMark;
  }

  @Override public void changedUpdate(DocumentEvent e) {
    /* not needed */
  }

  @Override public void insertUpdate(DocumentEvent e) {
    update((DefaultStyledDocument) e.getDocument(), e.getOffset());
  }

  @Override public void removeUpdate(DocumentEvent e) {
    update((DefaultStyledDocument) e.getDocument(), e.getOffset());
  }

  private void update(StyledDocument doc, int offset) {
    Element elm = doc.getCharacterElement(offset);
    EventQueue.invokeLater(() -> {
      try {
        updateFaceStyle(doc, elm);
      } catch (BadLocationException ex) {
        // should never happen
        RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
        wrap.initCause(ex);
        throw wrap;
      }
    });
  }

  private void updateFaceStyle(StyledDocument doc, Element elm) throws BadLocationException {
    int start = elm.getStartOffset();
    int end = elm.getEndOffset();
    String text = doc.getText(start, end - start);
    int pos = text.indexOf(faceMark);
    while (pos > -1) {
      Style face = doc.getStyle(faceMark);
      doc.setCharacterAttributes(start + pos, faceMark.length(), face, false);
      pos = text.indexOf(faceMark, pos + faceMark.length());
    }
  }
}

class FaceIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setColor(Color.RED);
    g2.drawOval(1, 1, 14, 14);
    g2.drawLine(5, 10, 6, 10);
    g2.drawLine(7, 11, 9, 11);
    g2.drawLine(10, 10, 11, 10);
    g2.drawOval(4, 5, 1, 1);
    g2.drawOval(10, 5, 1, 1);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
