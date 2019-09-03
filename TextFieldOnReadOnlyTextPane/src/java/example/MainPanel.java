// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextPane textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setMargin(new Insets(0, 10, 0, 0));
    insertQuestion(textPane, "111 / 37 = ");
    insertQuestion(textPane, "222 / 37 = ");
    insertQuestion(textPane, "333 / 37 = ");
    insertQuestion(textPane, "444 / 37 = ");
    insertQuestion(textPane, "555 / 37 = ");
    insertQuestion(textPane, "666 / 37 = ");
    insertQuestion(textPane, "777 / 37 = ");
    insertQuestion(textPane, "888 / 37 = ");
    insertQuestion(textPane, "999 / 37 = ");

    add(new JScrollPane(textPane));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void insertQuestion(JTextPane textPane, String str) {
    Document doc = textPane.getDocument();
    try {
      doc.insertString(doc.getLength(), str, null);

      int pos = doc.getLength();
      System.out.println(pos);
      JTextField field = new JTextField(4) {
        @Override public Dimension getMaximumSize() {
          return getPreferredSize();
        }
      };
      field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
      field.addFocusListener(new FocusAdapter() {
        @Override public void focusGained(FocusEvent e) {
          try {
            Rectangle rect = textPane.modelToView(pos);
            rect.grow(0, 4);
            rect.setSize(field.getSize());
            // System.out.println(rect);
            // System.out.println(field.getLocation());
            textPane.scrollRectToVisible(rect);
          } catch (BadLocationException ex) {
            // should never happen
            RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
            wrap.initCause(ex);
            throw wrap;
          }
        }
      });
      Dimension d = field.getPreferredSize();
      int baseline = field.getBaseline(d.width, d.height);
      field.setAlignmentY(baseline / (float) d.height);

      // MutableAttributeSet a = new SimpleAttributeSet();
      MutableAttributeSet a = textPane.getStyle(StyleContext.DEFAULT_STYLE);
      StyleConstants.setLineSpacing(a, 1.5f);
      textPane.setParagraphAttributes(a, true);

      textPane.insertComponent(field);
      doc.insertString(doc.getLength(), "\n", null);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
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
