// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String txt = "The quick brown fox jumps over the lazy dog\n";
    JTextArea textArea = new JTextArea(txt) {
      @Override public String getToolTipText(MouseEvent e) {
        // Java 9: int pos = viewToModel2D(e.getPoint());
        int pos = viewToModel(e.getPoint());
        String tipText = super.getToolTipText(e);
        try {
          int start = Utilities.getWordStart(this, pos);
          int end = Utilities.getWordEnd(this, pos);
          String word = getText(start, end - start);
          // Java 11: if (!word.isBlank()) {
          if (!checkTrimEmpty(word)) {
            tipText = String.format("%s(%d-%d)", word, start, end);
          }
        } catch (BadLocationException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
        return tipText;
      }
    };
    textArea.setLineWrap(true);
    ToolTipManager.sharedInstance().registerComponent(textArea);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static boolean checkTrimEmpty(String str) {
    return IntStream.range(0, str.length())
        .mapToObj(str::charAt)
        .allMatch(Character::isWhitespace);
    // for (int i = 0; i < str.length(); i++) {
    //   if (!Character.isWhitespace(str.charAt(i))) {
    //     return false;
    //   }
    // }
    // return true;
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
