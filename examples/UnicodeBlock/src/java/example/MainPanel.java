// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField label = new JTextField();
    label.setEditable(false);
    label.setFont(label.getFont().deriveFont(32f));
    JTextField labelUnicodeBlock = new JTextField();
    label.setEditable(false);

    JTextArea textArea = new JTextArea("😀😁😂てすとテストＴＥＳＴtest試験、𠮟┷→");
    textArea.addCaretListener(e -> {
      try {
        int loc = Math.min(e.getDot(), e.getMark());
        Document doc = textArea.getDocument();
        String txt = doc.getText(loc, 1);
        int code = txt.codePointAt(0);
        if (Character.isHighSurrogate((char) code)) {
          txt = doc.getText(loc, 2);
          code = txt.codePointAt(0);
        }
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(code);
        label.setText(String.format("%s: U+%04X", txt, code));
        labelUnicodeBlock.setText(Objects.toString(unicodeBlock));
      } catch (BadLocationException ex) {
        // should never happen
        RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
        wrap.initCause(ex);
        throw wrap;
      }
    });

    Box box = Box.createVerticalBox();
    box.add(label);
    box.add(labelUnicodeBlock);

    add(new JScrollPane(textArea));
    add(box, BorderLayout.SOUTH);
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
