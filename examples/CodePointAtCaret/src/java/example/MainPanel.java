// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String u1F60x = "😀😁😂😃😄😅😆😇😈😉😊😋😌😍😎😏";
    String u1F61x = "😐😑😒😓😔😕😖😗😘😙😚😛😜😝😞😟";
    String u1F62x = "😠😡😢😣😤😥😦😧😨😩😪😫😬😭😮😯";
    String u1F63x = "😰😱😲😳😴😵😶😷😸😹😺😻😼😽😾😿";
    String u1F64x = "🙀🙁🙂🙃🙄🙅🙆🙇🙈🙉🙊🙋🙌🙍🙎🙏";

    JTextField label = new JTextField();
    label.setEditable(false);
    label.setFont(label.getFont().deriveFont(32f));

    String str = String.join("\n", u1F60x, u1F61x, u1F62x, u1F63x, u1F64x);
    JTextArea textArea = new JTextArea(str);
    textArea.addCaretListener(e -> {
      try {
        int dot = e.getDot();
        int mark = e.getMark();
        if (dot - mark == 0) {
          Document doc = textArea.getDocument();
          String txt = doc.getText(dot, 1);
          int code = txt.codePointAt(0);
          if (Character.isHighSurrogate((char) code)) {
            txt = doc.getText(dot, 2);
            code = txt.codePointAt(0);
            // code = Character.toCodePoint(
            //     (char) code, (char) doc.getText(dot + 1, 1).codePointAt(0));
            // txt = new String(Character.toChars(code));
          }
          label.setText(String.format("%s: U+%04X", txt, code));
        } else {
          label.setText("");
        }
      } catch (BadLocationException ex) {
        // should never happen
        RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
        wrap.initCause(ex);
        throw wrap;
      }
    });

    add(new JScrollPane(textArea));
    add(label, BorderLayout.SOUTH);
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
