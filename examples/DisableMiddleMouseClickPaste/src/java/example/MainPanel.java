// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    String txt = "The quick brown fox jumps over the lazy dog.";
    String repeat = String.join("\n", Collections.nCopies(3, txt));
    String txt1 = "Default:";
    JTextArea textArea1 = new JTextArea(txt1 + "\n" + repeat);
    String txt2 = "Disable middle mouseClicked paste:";
    JTextArea textArea2 = new JTextArea(txt2 + "\n" + repeat) {
      @Override public void updateUI() {
        setCaret(null);
        super.updateUI();
        Caret oldCaret = getCaret();
        int blinkRate = oldCaret.getBlinkRate();
        Caret caret = new DisableMiddleClickPasteCaret();
        caret.setBlinkRate(blinkRate);
        setCaret(caret);
      }
    };
    add(new JScrollPane(textArea1));
    add(new JScrollPane(textArea2));
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

class DisableMiddleClickPasteCaret extends DefaultCaret {
  @Override public void mouseClicked(MouseEvent e) {
    // System.out.println(e);
    if (SwingUtilities.isMiddleMouseButton(e)) {
      e.consume();
    }
    super.mouseClicked(e);
  }
}
