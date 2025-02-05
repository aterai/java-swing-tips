// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
  private static final String TEXT = "The quick brown fox jumps over the lazy dog.";
  private final JTextArea textArea = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    textArea.setEditable(false);

    StyleContext style = new StyleContext();
    StyledDocument doc = new DefaultStyledDocument(style);
    try {
      doc.insertString(0, TEXT + "\n" + TEXT, null);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
    MutableAttributeSet attr1 = new SimpleAttributeSet();
    attr1.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    attr1.addAttribute(StyleConstants.Foreground, Color.RED);
    doc.setCharacterAttributes(4, 11, attr1, false);

    MutableAttributeSet attr2 = new SimpleAttributeSet();
    attr2.addAttribute(StyleConstants.Underline, Boolean.TRUE);
    doc.setCharacterAttributes(10, 20, attr2, false);

    JTextPane textPane = new JTextPane(doc);
    textPane.addCaretListener(e -> {
      if (e.getDot() == e.getMark()) {
        AttributeSet a = doc.getCharacterElement(e.getDot()).getAttributes();
        append("isBold: " + StyleConstants.isBold(a));
        append("isUnderline: " + StyleConstants.isUnderline(a));
        append("Foreground: " + StyleConstants.getForeground(a));
        append("FontFamily: " + StyleConstants.getFontFamily(a));
        append("FontSize: " + StyleConstants.getFontSize(a));
        append("Font: " + style.getFont(a));
        append("----");
      }
    });

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setResizeWeight(.5);
    sp.setTopComponent(new JScrollPane(textPane));
    sp.setBottomComponent(new JScrollPane(textArea));
    add(sp);
    setPreferredSize(new Dimension(320, 240));
  }

  private void append(String str) {
    textArea.append(str + "\n");
    textArea.setCaretPosition(textArea.getDocument().getLength());
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
